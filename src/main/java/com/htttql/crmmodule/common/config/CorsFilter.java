package com.htttql.crmmodule.common.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * CORS Filter to handle preflight requests and add CORS headers
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // Allow specific origins
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.equals("http://localhost:3000") ||
                origin.equals("http://localhost:4200") ||
                origin.equals("http://localhost:5173") ||
                origin.equals("http://localhost:8080") ||
                origin.equals("http://localhost:3001") ||
                origin.equals("http://localhost:4201"))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        // Allow credentials
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Allow specific methods
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, TRACE, CONNECT");

        // Allow all headers
        response.setHeader("Access-Control-Allow-Headers", "*");

        // Cache preflight response for 1 hour
        response.setHeader("Access-Control-Max-Age", "3600");

        // Handle preflight request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
