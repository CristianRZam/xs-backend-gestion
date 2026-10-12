package com.sistema.sistema.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sistema.sistema.application.service.AppVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class AppVersionFilterTest {

    @Test
    void doesNotValidateVersionForWebRequestsWithoutMobilePlatformHeader() throws Exception {
        AppVersionService service = mock(AppVersionService.class);
        AppVersionFilter filter = new AppVersionFilter(service, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/product/init");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verifyNoInteractions(service);
    }
}
