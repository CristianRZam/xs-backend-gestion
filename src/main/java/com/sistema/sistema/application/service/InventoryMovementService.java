package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.request.inventorymovement.InventoryMovementCreateRequest;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDTO;
import com.sistema.sistema.application.dto.response.inventorymovement.InventoryMovementDetailDTO;
import com.sistema.sistema.application.dto.response.product.ProductDTO;
import com.sistema.sistema.domain.repository.IventoryMovementRepository;
import com.sistema.sistema.domain.repository.ProductRepository;
import com.sistema.sistema.domain.usecase.InventoryMovementUseCase;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryMovementService implements InventoryMovementUseCase {


    private final IventoryMovementRepository repository;
    private final ProductRepository productRepository;


    public InventoryMovementService(
            IventoryMovementRepository repository,
            ProductRepository productRepository
    ) {
        this.repository = repository;
        this.productRepository = productRepository;
    }

    @Override
    public List<InventoryMovementDetailDTO> findAll(Long productId) {
        return repository.findAll(productId);
    }

    @Override
    public InventoryMovementDTO findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public InventoryMovementDTO create(
            InventoryMovementCreateRequest request
    ) {

        ProductDTO product = validateProduct(
                request.getProductId()
        );


        BigDecimal previousStock =
                BigDecimal.valueOf(product.getTotalStock());


        BigDecimal currentStock =
                calculateStock(
                        previousStock,
                        request.getType(),
                        request.getQuantity()
                );

        BigDecimal reservedStock = BigDecimal.valueOf(
                product.getReservedStock() != null
                        ? product.getReservedStock()
                        : 0L
        );

        if (currentStock.compareTo(reservedStock) < 0) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "El movimiento dejaría el stock por debajo de la cantidad reservada ("
                            + reservedStock
                            + ")."
            );
        }


        request.setPreviousStock(previousStock);
        request.setCurrentStock(currentStock);



        boolean updated =
                productRepository.updateStock(
                        product.getId(),
                        currentStock.longValue()
                );


        if (!updated) {
            throw new BusinessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible actualizar el stock del producto."
            );
        }


        return repository.create(request);
    }



    // =====================================================
    // Validaciones
    // =====================================================


    private ProductDTO validateProduct(Long productId) {

        ProductDTO product =
                productRepository.findById(productId);


        if (product == null) {

            throw new BusinessException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado."
            );
        }


        return product;
    }



    private BigDecimal calculateStock(BigDecimal stock, String type, BigDecimal quantity) {

        return switch (type) {


            /*
             * Ingreso de mercadería
             * Compra
             * Devolución de venta
             * Cancelación de venta
             */
            case "ENTRY" ->
                    stock.add(quantity);


            /*
             * Venta
             * Merma
             */
            case "SALE", "WASTE" -> {


                BigDecimal result =
                        stock.subtract(quantity);


                if (result.compareTo(BigDecimal.ZERO) < 0) {

                    throw new BusinessException(
                            HttpStatus.BAD_REQUEST,
                            "El stock no puede ser negativo."
                    );
                }


                yield result;
            }



            /*
             * Conteo físico de inventario
             *
             * Ejemplo:
             * Sistema dice 20
             * Conteo real dice 18
             *
             * Nuevo stock = 18
             */
            case "ADJUSTMENT" ->
                    quantity;



            default ->
                    throw new BusinessException(
                            HttpStatus.BAD_REQUEST,
                            "Tipo de movimiento inválido."
                    );
        };

    }

}
