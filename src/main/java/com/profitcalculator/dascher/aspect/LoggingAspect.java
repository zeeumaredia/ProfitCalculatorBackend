package com.profitcalculator.dascher.aspect;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP aspect for service-layer observability.
 *
 * <p>Logs method entry (with arguments) and exceptions at the service implementation level.
 * Successful return values are logged by type only — never by content (Security Concern).
 *
 * <p>End-to-end HTTP request/response timing is handled separately by {@link
 * com.profitcalculator.dascher.filter.RequestLoggingFilter}, which operates at the servlet level.
 * This aspect intentionally does not duplicate that.
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Around("execution(* com.profitcalculator.dascher.service.impl.*.*(..))")
  public Object logServiceCall(ProceedingJoinPoint pjp) throws Throwable {
    String className = pjp.getTarget().getClass().getSimpleName();
    String method = pjp.getSignature().getName();

    log.debug("→ {}.{}() args={}", className, method, Arrays.toString(pjp.getArgs()));

    try {
      Object result = pjp.proceed();
      log.debug(
          "← {}.{}() returned {}",
          className,
          method,
          result == null ? "null" : result.getClass().getSimpleName());
      return result;
    } catch (Exception ex) {
      // ERROR not WARN — an unhandled exception escaping a service method needs to surface in
      // alerting thresholds, not be treated as recoverable.
      log.error(
          "✗ {}.{}() threw {}: {}",
          className,
          method,
          ex.getClass().getSimpleName(),
          ex.getMessage());
      throw ex;
    }
  }
}
