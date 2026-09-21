package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.application.dto.request.sale.SaleCreateRequest;
import com.sistema.sistema.application.dto.response.sale.SaleDTO;
import java.util.List;
import com.sistema.sistema.application.dto.response.sale.CashSessionSalesSummaryDTO;

public interface SaleUseCase {
    SaleDTO create(SaleCreateRequest request);
    SaleDTO getById(Long id);
    List<SaleDTO> getAll();
    CashSessionSalesSummaryDTO getCashSessionSummary(Long cashSessionId);
}
