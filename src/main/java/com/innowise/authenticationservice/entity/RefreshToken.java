package com.innowise.authenticationservice.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

  private Long id;
  private Long userId;
  private String token;
  private Instant expiresAt;
  private boolean revoked;
  private Instant created_at;

}
