package com.sistema.sistema.domain.usecase;

import com.sistema.sistema.domain.model.CashSession;

import java.math.BigDecimal;
import java.util.List;

public interface CashSessionUseCase {

    CashSession openSession(CashSession cashSession);

    CashSession getCurrentSession();

    boolean existsOpenSession();

    CashSession closeSession(Long id, BigDecimal closingAmount, BigDecimal expectedAmount, BigDecimal difference, String closingComment);

    List<CashSession> getHistory();
}