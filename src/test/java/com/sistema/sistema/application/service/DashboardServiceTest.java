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

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatedActiveUserReceivesTheGlobalSummary() {
        authenticate(42L, "VIEW_DASHBOARD", true);
        DashboardDTO global = DashboardDTO.builder().build();
        when(repository.getSummary()).thenReturn(global);

        assertSame(global, service.getSummary());
        assertEquals("GLOBAL", global.getScope());
        verify(repository).getSummary();
    }

    @Test
    void endpointDoesNotAcceptParametersToChangeDashboardScope() throws Exception {
        authenticate(42L, "VIEW_DASHBOARD", true);
        when(repository.getSummary()).thenReturn(DashboardDTO.builder().build());
        var mvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mvc.perform(get("/api/dashboard")
                        .param("userId", "999")
                        .param("scope", "PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("GLOBAL"));

        verify(repository).getSummary();
    }

    @Test
    void missingSessionIsRejectedBeforeAnyQuery() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(BusinessException.class, service::getSummary).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void disabledUserIsRejectedBeforeAnyQuery() {
        authenticate(1L, "VIEW_DASHBOARD", false);

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(BusinessException.class, service::getSummary).getStatus());
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
