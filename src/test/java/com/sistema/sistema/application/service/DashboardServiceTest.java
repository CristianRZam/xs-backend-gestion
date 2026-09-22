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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardServiceTest {
    private final DashboardRepository repository = mock(DashboardRepository.class);
    private final DashboardService service = new DashboardService(repository);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void superAdminReceivesGlobalSummary() {
        authenticate(1L, "ROLE_SUPER_ADMIN", true);
        DashboardDTO global = DashboardDTO.builder().build();
        when(repository.getSummary()).thenReturn(global);

        assertSame(global, service.getSummary());
        assertEquals("GLOBAL", global.getScope());
        verify(repository, never()).getPersonalSummary(anyLong(), any());
    }

    @Test
    void ordinaryAdminReceivesOnlyTheirOwnSummary() {
        authenticate(42L, "ROLE_ADMIN", true);
        DashboardDTO personal = DashboardDTO.builder().scope("PERSONAL").build();
        when(repository.getPersonalSummary(eq(42L), any())).thenReturn(personal);

        assertSame(personal, service.getSummary());
        verify(repository).getPersonalSummary(42L, LocalDate.now());
        verify(repository, never()).getSummary();
    }

    @Test
    void requestCannotOverrideOwnerOrScope() throws Exception {
        authenticate(42L, "ROLE_EMPLOYEE", true);
        when(repository.getPersonalSummary(eq(42L), any()))
                .thenReturn(DashboardDTO.builder().scope("PERSONAL").build());
        var mvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mvc.perform(get("/api/dashboard")
                        .param("userId", "999")
                        .param("scope", "GLOBAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scope").value("PERSONAL"));

        verify(repository).getPersonalSummary(eq(42L), any());
        verify(repository, never()).getSummary();
    }

    @Test
    void missingSessionIsRejectedBeforeAnyQuery() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(BusinessException.class, service::getSummary).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void disabledUserIsRejectedBeforeAnyQuery() {
        authenticate(1L, "ROLE_SUPER_ADMIN", false);
        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(BusinessException.class, service::getSummary).getStatus());
        verifyNoInteractions(repository);
    }

    @Test
    void permissionWithoutActiveRoleDoesNotGrantGlobalAccess() {
        authenticate(1L, "VIEW_DASHBOARD", true);
        assertEquals(HttpStatus.FORBIDDEN,
                assertThrows(BusinessException.class, service::getSummary).getStatus());
        verifyNoInteractions(repository);
    }

    private void authenticate(Long id, String role, boolean active) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        var principal = new XsUserDetails(id, "tester", "", authorities, active);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities)
        );
    }
}
