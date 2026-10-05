package com.sistema.sistema.infrastructure.persistence.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardDAOImplTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final DashboardDAOImpl repository = new DashboardDAOImpl();
    private final List<String> statements = new ArrayList<>();
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUpQueries() {
        ReflectionTestUtils.setField(repository, "em", entityManager);
        when(entityManager.createNativeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            statements.add(sql);
            Query query = mock(Query.class);

            if (sql.startsWith("SELECT COALESCE(SUM(total),0), COUNT(*) FROM sales")) {
                when(query.getSingleResult()).thenReturn(new Object[]{new BigDecimal("100.00"), 3L});
            } else if (sql.contains("FROM orders")) {
                when(query.getSingleResult()).thenReturn(2L);
            } else if (sql.contains("FROM sale_items")) {
                when(query.getResultList()).thenReturn(
                        Collections.singletonList(new Object[]{7L, "Torta", 4L})
                );
            } else if (sql.contains("FROM payments")) {
                when(query.getResultList()).thenReturn(
                        Collections.singletonList(new Object[]{"CASH", new BigDecimal("70.00")})
                );
            } else if (sql.contains("DATE(created_at),SUM(total)")) {
                when(query.getResultList()).thenReturn(Collections.singletonList(
                        new Object[]{Date.valueOf(today.minusDays(1)), new BigDecimal("50.00")}
                ));
            }
            return query;
        });
    }

    @Test
    void getSummaryBuildsTheGlobalDashboardWithSevenDaySeries() {
        var summary = repository.getSummary();

        assertEquals("GLOBAL", summary.getScope());
        assertNull(summary.getSummaryDate());
        assertEquals(new BigDecimal("100.00"), summary.getTodaySales());
        assertEquals(3L, summary.getTodaySalesCount());
        assertEquals(new BigDecimal("33.33"), summary.getAverageSale());
        assertEquals(2L, summary.getTodayOrders());
        assertEquals(7, summary.getWeeklySales().size());
        assertEquals(today.minusDays(6), summary.getWeeklySales().get(0).getDate());
        assertEquals(today, summary.getWeeklySales().get(6).getDate());
        assertEquals(new BigDecimal("50.00"), summary.getWeeklySales().get(5).getTotal());
        assertEquals("Torta", summary.getTopProducts().get(0).getProductName());
        assertEquals(4L, summary.getTopProducts().get(0).getQuantity());
        assertEquals("CASH", summary.getPaymentMethods().get(0).getMethod());
        assertEquals(new BigDecimal("70.00"), summary.getPaymentMethods().get(0).getTotal());
        assertTrue(statements.stream().noneMatch(sql -> sql.contains("created_by")));
    }

    @Test
    void getSummaryReturnsZeroAverageAndEmptyBreakdownsWhenThereAreNoSales() {
        when(entityManager.createNativeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            Query query = mock(Query.class);
            if (sql.startsWith("SELECT COALESCE(SUM(total),0), COUNT(*) FROM sales")) {
                when(query.getSingleResult()).thenReturn(new Object[]{BigDecimal.ZERO, 0L});
            } else if (sql.contains("FROM orders")) {
                when(query.getSingleResult()).thenReturn(0L);
            } else {
                when(query.getResultList()).thenReturn(List.of());
            }
            return query;
        });

        var summary = repository.getSummary();

        assertEquals(BigDecimal.ZERO, summary.getTodaySales());
        assertEquals(0L, summary.getTodaySalesCount());
        assertEquals(BigDecimal.ZERO, summary.getAverageSale());
        assertEquals(0L, summary.getTodayOrders());
        assertTrue(summary.getTopProducts().isEmpty());
        assertTrue(summary.getPaymentMethods().isEmpty());
        assertEquals(7, summary.getWeeklySales().size());
        assertFalse(summary.getWeeklySales().stream().anyMatch(day -> day.getTotal().signum() != 0));
    }
}
