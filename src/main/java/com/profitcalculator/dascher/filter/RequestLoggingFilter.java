package com.profitcalculator.dascher.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet-level HTTP request/response logger.
 *
 * <p>Logs each inbound request: HTTP method, URI, response status, and end-to-end wall-clock
 * duration. This is the single source of request timing — the service-layer {@link
 * com.profitcalculator.dascher.aspect.LoggingAspect} intentionally does not duplicate it.
 *
 * <p>H2-console and Actuator paths are excluded to avoid log noise.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();
    try {
      chain.doFilter(req, res);
    } finally {
      log.info(
          "{} {} → {} ({}ms)",
          req.getMethod(),
          req.getRequestURI(),
          res.getStatus(),
          System.currentTimeMillis() - start);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest req) {
    String uri = req.getRequestURI();
    return uri.startsWith("/h2-console") || uri.startsWith("/actuator");
  }
}
