package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.inventorycount.InventoryCountCloseRequest;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountItemEntity;
import com.sistema.sistema.infrastructure.persistence.inventorycount.InventoryCountSessionEntity;
import com.sistema.sistema.infrastructure.persistence.inventorycount.JpaInventoryCountItemRepository;
import com.sistema.sistema.infrastructure.persistence.inventorycount.JpaInventoryCountSessionRepository;
import com.sistema.sistema.infrastructure.persistence.product.JpaProductRepository;
import com.sistema.sistema.infrastructure.persistence.product.ProductEntity;
import com.sistema.sistema.infrastructure.persistence.user.JpaUserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InventoryCountServiceTest {
    private final JpaInventoryCountSessionRepository sessions = mock(JpaInventoryCountSessionRepository.class);
    private final JpaInventoryCountItemRepository items = mock(JpaInventoryCountItemRepository.class);
    private final JpaProductRepository products = mock(JpaProductRepository.class);
    private final JpaUserRepository users = mock(JpaUserRepository.class);
    private final InventoryMovementService movements = mock(InventoryMovementService.class);
    private final InventoryCountService service = new InventoryCountService(sessions, items, products, users, movements);

    @Test
    void reviewUsesCurrentProductStockWhenValidating() {
        var session = InventoryCountSessionEntity.builder().id(7L).status("OPEN").build();
        var item = InventoryCountItemEntity.builder()
                .sessionId(7L)
                .productId(1L)
                .openingStock(5L)
                .resultStatus("PENDING")
                .build();
        when(sessions.findById(7L)).thenReturn(Optional.of(session));
        when(items.findBySessionIdOrderById(7L)).thenReturn(List.of(item));
        when(sessions.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(products.findById(1L)).thenReturn(Optional.of(
                ProductEntity.builder().id(1L).name("Producto").code("P1").totalStock(99L).build()
        ));

        service.review(7L, request(item(1L, 4L)));

        assertEquals(99L, item.getExpectedStock());
        assertEquals(4L, item.getPhysicalStock());
        assertEquals(-95L, item.getDifference());
        assertEquals("SHORTAGE", item.getResultStatus());
        verify(products, times(2)).findById(1L);
    }

    @Test
    void reviewRejectsIncompleteCounts() {
        var session = InventoryCountSessionEntity.builder().id(7L).status("OPEN").build();
        when(sessions.findById(7L)).thenReturn(Optional.of(session));
        when(items.findBySessionIdOrderById(7L)).thenReturn(List.of(
                countItem(7L, 1L, 5L),
                countItem(7L, 2L, 3L)
        ));

        var exception = assertThrows(BusinessException.class,
                () -> service.review(7L, request(item(1L, 5L))));

        assertEquals("Debe registrar el conteo físico de todos los productos.", exception.getMessage());
        verify(sessions, never()).save(any());
    }

    @Test
    void reviewRejectsDuplicateProducts() {
        var session = InventoryCountSessionEntity.builder().id(7L).status("OPEN").build();
        when(sessions.findById(7L)).thenReturn(Optional.of(session));
        when(items.findBySessionIdOrderById(7L)).thenReturn(List.of(
                countItem(7L, 1L, 5L),
                countItem(7L, 2L, 3L)
        ));

        var exception = assertThrows(BusinessException.class,
                () -> service.review(7L, request(item(1L, 5L), item(1L, 5L))));

        assertEquals("Un producto no puede contarse dos veces.", exception.getMessage());
    }

    @Test
    void closeStartsTheNextCountInTheSameOperation() {
        var closingSession = InventoryCountSessionEntity.builder().id(7L).status("OPEN").build();
        var closingItem = countItem(7L, 1L, 5L);
        var product = ProductEntity.builder().id(1L).name("Producto").code("P1").totalStock(5L).build();
        when(sessions.findById(7L)).thenReturn(Optional.of(closingSession));
        when(items.findBySessionIdOrderById(7L)).thenReturn(List.of(closingItem));
        when(products.findById(1L)).thenReturn(Optional.of(product));
        when(products.findByActiveTrueAndDeletedAtIsNullOrderByNameAsc()).thenReturn(List.of(product));
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(sessions.save(any())).thenAnswer(invocation -> {
            var saved = invocation.getArgument(0, InventoryCountSessionEntity.class);
            saved.setId(8L);
            return saved;
        });

        var closeRequest = request(item(1L, 5L));
        closeRequest.setClosingComment("  Conteo de turno finalizado.  ");

        var nextSession = service.close(7L, closeRequest);

        assertEquals("CLOSED", closingSession.getStatus());
        assertEquals("Conteo de turno finalizado.", closingSession.getClosingComment());
        assertEquals("OPEN", nextSession.getStatus());
        assertEquals(8L, nextSession.getId());
        assertEquals("Conteo iniciado automáticamente por el sistema.", nextSession.getOpeningComment());
        verify(sessions).saveAndFlush(closingSession);
        verify(items).saveAll(any());
    }

    private InventoryCountItemEntity countItem(Long sessionId, Long productId, Long openingStock) {
        return InventoryCountItemEntity.builder()
                .sessionId(sessionId)
                .productId(productId)
                .openingStock(openingStock)
                .resultStatus("PENDING")
                .build();
    }

    private InventoryCountCloseRequest request(InventoryCountCloseRequest.Item... entries) {
        var request = new InventoryCountCloseRequest();
        request.setItems(List.of(entries));
        return request;
    }

    private InventoryCountCloseRequest.Item item(Long productId, Long physicalStock) {
        var item = new InventoryCountCloseRequest.Item();
        item.setProductId(productId);
        item.setPhysicalStock(physicalStock);
        return item;
    }
}
