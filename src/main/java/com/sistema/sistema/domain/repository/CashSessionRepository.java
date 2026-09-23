package com.sistema.sistema.domain.repository;

import com.sistema.sistema.domain.model.CashSession;
import com.sistema.sistema.application.dto.response.PageResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface CashSessionRepository {

    CashSession openSession(CashSession cashSession);

    CashSession getCurrentSession();

    boolean existsOpenSession();

    CashSession closeSession(Long id, BigDecimal closingAmount, BigDecimal expectedAmount, BigDecimal difference, String closingComment);

    PageResponseDTO<CashSession> getHistory(int page, int size);

    BigDecimal calculateExpectedAmount(Long sessionId);

}
