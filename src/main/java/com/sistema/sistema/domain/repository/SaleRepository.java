package com.sistema.sistema.domain.repository;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import java.math.BigDecimal;
import java.util.List;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import java.time.LocalDate;

public interface SaleRepository {
    SaleDTO create(SaleCreateRequest request, Long cashRegisterId, Long cashSessionId,
                   BigDecimal subtotal, BigDecimal total);
    SaleDTO getById(Long id);
    List<SaleDTO> getAll();
    PageResponseDTO<SaleDTO> getPage(int page, int size, LocalDate fromDate, LocalDate toDate);
    boolean existsByOrderId(Long orderId);
    boolean isOriginalCashSessionOpen(Long saleId);
    SaleDTO cancel(Long saleId, String reason);
    CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId);
}
