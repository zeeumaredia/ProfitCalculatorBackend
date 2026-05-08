package com.profitcalculator.dascher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic wrapper for all API responses. Wraps the payload with a human-readable message sourced
 * from messages.properties. The data field is omitted from JSON when null (e.g. on delete
 * responses).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse<T> {

  private String message;
  private T data;

  // Use this when there is a payload to return (create, read, update)
  public static <T> MessageResponse<T> of(String message, T data) {
    return new MessageResponse<>(message, data);
  }

  // Use this for operations with no response body (e.g. delete)
  public static <T> MessageResponse<T> of(String message) {
    return new MessageResponse<>(message, null);
  }
}
