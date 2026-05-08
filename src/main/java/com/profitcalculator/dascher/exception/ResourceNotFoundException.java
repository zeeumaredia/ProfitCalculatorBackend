package com.profitcalculator.dascher.exception;

/**
 * Thrown when a requested entity (Shipment, CostType) does not exist in the database. Caught by
 * GlobalExceptionHandler and mapped to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
