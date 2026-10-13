package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.domain.repository.IventoryMovementRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InventoryMovementServiceTest {

    private final IventoryMovementRepository movementRepository = mock(IventoryMovementRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final InventoryMovementService service = new InventoryMovementService(
            movementRepository,
            productRepository
    );

    @Test
    void wasteCannotConsumeStockReservedByAnActiveOrder() {
        when(productRepository.findById(7L)).thenReturn(ProductDTO.builder()
                .id(7L)
                .totalStock(11L)
                .reservedStock(1L)
                .build());
        InventoryMovementCreateRequest request = InventoryMovementCreateRequest.builder()
                .productId(7L)
                .type("WASTE")
                .quantity(BigDecimal.valueOf(11))
                .reason("Producto dañado")
                .build();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.create(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertTrue(exception.getMessage().contains("reservada(s)"));
        assertTrue(exception.getMessage().contains("10"));
        verify(productRepository).findById(7L);
        verify(productRepository, never()).updateStock(7L, 0L);
        verifyNoInteractions(movementRepository);
    }
}
