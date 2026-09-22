package com.sistema.sistema.infrastructure.persistence.dashboard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DashboardDAOImplTest {
    private final EntityManager em = mock(EntityManager.class);
    private final DashboardDAOImpl repository = new DashboardDAOImpl();
    private final List<String> statements = new ArrayList<>();
    private final List<Query> queries = new ArrayList<>();
    private final LocalDate day = LocalDate.of(2026, 9, 22);
    private BigDecimal salesTotal = new BigDecimal("100.00");
    private long salesCount = 3;

    @BeforeEach
    void setupQueries() {
        ReflectionTestUtils.setField(repository, "em", em);
        when(em.createNativeQuery(anyString())).thenAnswer(invocation -> {
            String sql = invocation.getArgument(0);
            statements.add(sql);
            Query query = mock(Query.class);
            queries.add(query);
            when(query.setParameter(anyString(), any())).thenReturn(query);
            if (sql.contains("SUM(s.total)")) {
                when(query.getSingleResult()).thenReturn(new Object[]{salesTotal, salesCount});
            } else if (sql.contains("FROM orders")) {
                when(query.getSingleResult()).thenReturn(2L);
            } else {
                when(query.getResultList()).thenReturn(List.of());
            }
            return query;
        });
    }

    @Test
    void everyPersonalAggregateIsBoundToOwnerAndHalfOpenDay() {
        var summary = repository.getPersonalSummary(42L, day);

        assertEquals("PERSONAL", summary.getScope());
        assertEquals(day, summary.getSummaryDate());
        assertEquals(new BigDecimal("100.00"), summary.getTodaySales());
        assertEquals(3L, summary.getTodaySalesCount());
        assertEquals(new BigDecimal("33.33"), summary.getAverageSale());
        assertEquals(2L, summary.getTodayOrders());
        assertTrue(summary.getWeeklySales().isEmpty());
        assertEquals(4, queries.size());
        for (int index = 0; index < queries.size(); index++) {
            String sql = statements.get(index);
            assertTrue(sql.contains("created_by = :userId"));
            assertTrue(sql.contains("created_at >= :start"));
            assertTrue(sql.contains("created_at < :end"));
            assertTrue(sql.contains("deleted_at IS NULL"));
            if (!sql.contains("FROM orders")) {
                assertTrue(sql.contains("s.status = 'COMPLETED'"));
            }
            verify(queries.get(index)).setParameter("userId", 42L);
            verify(queries.get(index)).setParameter("start", day.atStartOfDay());
            verify(queries.get(index)).setParameter("end", day.plusDays(1).atStartOfDay());
        }
    }

    @Test
    void dayWithoutSalesReturnsZerosAndEmptyCollections() {
        salesTotal = BigDecimal.ZERO;
        salesCount = 0;
        var summary = repository.getPersonalSummary(42L, day);

        assertEquals(BigDecimal.ZERO, summary.getAverageSale());
        assertEquals(BigDecimal.ZERO, summary.getTodaySales());
        assertTrue(summary.getTopProducts().isEmpty());
        assertTrue(summary.getPaymentMethods().isEmpty());
    }

    @Test
    void missingOwnerCannotFallBackToGlobalQueries() {
        assertThrows(NullPointerException.class, () -> repository.getPersonalSummary(null, day));
        verifyNoInteractions(em);
    }
}
