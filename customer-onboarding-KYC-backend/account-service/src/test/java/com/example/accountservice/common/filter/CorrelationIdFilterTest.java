package com.example.accountservice.common.filter;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class CorrelationIdFilterTest {

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void doFilterInternal_reusesExistingCorrelationIdHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_HEADER, "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> assertThat(MDC.get("correlationId")).isEqualTo("corr-123");

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_HEADER)).isEqualTo("corr-123");
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void doFilterInternal_generatesCorrelationIdWhenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
        };

        filter.doFilterInternal(request, response, chain);

        String header = response.getHeader(CorrelationIdFilter.CORRELATION_HEADER);
        assertThat(header).isNotBlank();
        assertThat(header).matches("[0-9a-fA-F-]{36}");
    }

    @Test
    void doFilterInternal_clearsMdcWhenChainThrows() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        doThrow(new IOException("boom")).when(chain).doFilter(request, response);

        assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
                .isInstanceOf(IOException.class)
                .hasMessage("boom");
        assertThat(MDC.get("correlationId")).isNull();
    }
}
