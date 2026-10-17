package com.sistema.sistema.application.service;

import com.sistema.sistema.application.dto.response.dashboard.DashboardDTO;
import com.sistema.sistema.domain.repository.DashboardRepository;
import com.sistema.sistema.infrastructure.controller.DashboardController;
import com.sistema.sistema.infrastructure.exception.BusinessException;
import com.sistema.sistema.infrastructure.security.XsUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardServiceTest {

    private final DashboardRepository repository = mock(DashboardRepository.class);
    private final DashboardService service = new DashboardService(repository);
    private final LocalDate fromDate = LocalDate.of(2026, 10, 1);
    private final LocalDate toDate = LocalDate.of(2026, 10, 12);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedActiveUserReceivesTheGlobalSummary() {
        authenticate(42L, "VIEW_DASHBOARD", true);
        DashboardDTO global = DashboardDTO.builder().scope("GLOBAL").build();
        when(repository.getSummary(fromDate, toDate, null, "GLOBAL")).thenReturn(global);

        assertSame(global, service.getSummary(fromDate, toDate, "GLOBAL", null));
        assertEquals("GLOBAL", global.getScope());
        verify(repository).getSummary(fromDate, toDate, null, "GLOBAL");
    }

    @Test
    void globalDashboardPermissionDoesNotGrantTheOwnSalesScope() {
        authenticate(42L, "VIEW_DASHBOARD", true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.getSummary(fromDate, toDate, "MY_SALES", null));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void userWithOwnDashboardPermissionCanOnlyQueryTheirSales() {
        authenticate(42L, "VIEW_OWN_DASHBOARD", true);
        DashboardDTO personal = DashboardDTO.builder()
                .scope("MY_SALES")
                .sellerId(42L)
                .build();
        when(repository.getSummary(fromDate, toDate, 42L, "MY_SALES")).thenReturn(personal);

        assertSame(personal, service.getSummary(fromDate, toDate, "GLOBAL", 999L));
        assertEquals("MY_SALES", personal.getScope());
        assertEquals(42L, personal.getSellerId());
        verify(repository).getSummary(fromDate, toDate, 42L, "MY_SALES");
    }

    @Test
    void endpointAppliesThePersonalScopeForAnOwnDashboardUser() throws Exception {
        authenticate(42L, "VIEW_OWN_DASHBOARD", true);
        DashboardDTO personal = DashboardDTO.builder()
                .scope("MY_SALES")
                .sellerId(42L)
                .build();
        when(repository.getSummary(fromDate, toDate, 42L, "MY_SALES")).thenReturn(personal);
        var mvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mvc.perform(get("/api/dashboard")
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString())
                        .param("scope", "MY_SALES")
                        .param("sellerId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("MY_SALES"))
                .andExpect(jsonPath("$.data.sellerId").value(42));

        verify(repository).getSummary(fromDate, toDate, 42L, "MY_SALES");
    }

    @Test
    void missingSessionIsRejectedBeforeAnyQuery() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(
                        BusinessException.class,
                        () -> service.getSummary(fromDate, toDate, "GLOBAL", null)
                ).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void disabledUserIsRejectedBeforeAnyQuery() {
        authenticate(1L, "VIEW_DASHBOARD", false);

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(
                        BusinessException.class,
                        () -> service.getSummary(fromDate, toDate, "GLOBAL", null)
                ).getStatus());
        verifyNoInteractions(repository);
    }

    private void authenticate(Long id, String authority, boolean active) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
        var principal = new XsUserDetails(id, "tester", "", authorities, active);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities)
        );
    }
}
