package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import java.util.List;
import java.time.LocalDate;
import com.sistema.sistema.application.dto.response.PageResponseDTO;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;
import com.sistema.sistema.application.dto.request.sale.SaleCancellationRequest;

public interface SaleUseCase {
    SaleDTO create(SaleCreateRequest request);
    SaleDTO getById(Long id);
    List<SaleDTO> getAll();
    PageResponseDTO<SaleDTO> getPage(int page, int size, LocalDate fromDate, LocalDate toDate);
    CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId);
    SaleDTO cancel(Long id, SaleCancellationRequest request);
}
