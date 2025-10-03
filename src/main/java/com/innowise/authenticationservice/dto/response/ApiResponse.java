package com.innowise.authenticationservice.dto.response;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.Getter;

@Getter
public class ApiResponse <T> {
  private final String message;
  private final OffsetDateTime timestamp;
  private final int status;
  private final T data;

  public ApiResponse(int status, String message, T data) {
    this.status = status;
    this.message = message;
    this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
    this.data = data;
  }

}

