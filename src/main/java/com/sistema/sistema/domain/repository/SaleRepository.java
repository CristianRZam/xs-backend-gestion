package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import java.math.BigDecimal;
import java.util.List;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;

public interface SaleRepository {
    SaleDTO create(SaleCreateRequest request, Long cashRegisterId, Long cashSessionId,
                   BigDecimal subtotal, BigDecimal total);
    SaleDTO getById(Long id);
    List<SaleDTO> getAll();
    boolean existsByOrderId(Long orderId);
    CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId);
}
