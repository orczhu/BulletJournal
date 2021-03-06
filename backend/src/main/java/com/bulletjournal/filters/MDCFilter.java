package com.bulletjournal.filters;

import com.bulletjournal.config.AuthConfig;
import com.bulletjournal.config.MDCConfig;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCFilter implements Filter {

    @Autowired
    MDCConfig mdcConfig;

    @Autowired
    AuthConfig authConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            final String requestId = extractRequestId(httpRequest);
            final String clientIP = extractClientIp(httpRequest);

            MDC.put(mdcConfig.getDefaultRequestIdKey(), requestId);
            MDC.put(mdcConfig.getDefaultClientIpKey(), clientIP);

            httpResponse.setHeader(mdcConfig.getDefaultRequestIdKey(), requestId);

            chain.doFilter(request, response);
        } finally {
            MDC.remove(mdcConfig.getDefaultRequestIdKey());
            MDC.remove(mdcConfig.getDefaultClientIpKey());
        }
    }

    private String extractRequestId(HttpServletRequest request) {
        final String token;
        String requestId = request.getHeader(mdcConfig.getDefaultRequestIdKey());
        if (!StringUtils.isEmpty(requestId)) {
            token = requestId;
        } else {
            token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        }
        return token;
    }

    private String extractClientIp(final HttpServletRequest request) {
        final String clientIP;
        if (request.getHeader("X-Forwarded-For") != null) {
            clientIP = request.getHeader("X-Forwarded-For").split(",")[0];
        } else {
            clientIP = request.getRemoteAddr();
        }
        return clientIP;
    }

    @Override
    public void destroy() {

    }
}