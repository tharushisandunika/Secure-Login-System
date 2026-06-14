package com.example.securelogin.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private static class TokenBucket {
        private final double capacity = 10.0; // Allow a burst of up to 10 requests
        private final double refillRate = 0.5; // Refill 0.5 tokens per second (1 token per 2 seconds)
        private double tokens = 10.0;
        private long lastRefillTime = System.currentTimeMillis();

        public synchronized boolean tryConsume() {
            long now = System.currentTimeMillis();
            double elapsedSeconds = (now - lastRefillTime) / 1000.0;
            lastRefillTime = now;
            tokens = Math.min(capacity, tokens + (elapsedSeconds * refillRate));

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private final Map<String, TokenBucket> ipLimiters = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        // Rate limit only critical authentication routes (login, register)
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            String clientIp = getClientIp(httpRequest);
            TokenBucket bucket = ipLimiters.computeIfAbsent(clientIp, k -> new TokenBucket());

            if (!bucket.tryConsume()) {
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"success\":false,\"message\":\"Too many login attempts from this IP. Please wait a moment.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
