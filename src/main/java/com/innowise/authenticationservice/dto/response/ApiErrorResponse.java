package com.innowise.authenticationservice.dto.response;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class ApiErrorResponse {
  private final List<String> message;
  private final OffsetDateTime timestamp;
  private final int status;
  private final String error;

  public ApiErrorResponse (int status, List<String> message, String error) {
    this.status = status;
    this.message = message;
    this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
    this.error = error;
  }

}
