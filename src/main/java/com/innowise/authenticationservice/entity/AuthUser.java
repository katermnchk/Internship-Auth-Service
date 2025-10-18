package com.innowise.authenticationservice.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

  private Long id;
  private Long userId;
  private String email;
  private String passwordHash;
  private Instant createdAt;
  private Instant updatedAt;

  public AuthUser(Long id, String email, String passwordHash) {
    this.id = id;
    this.email = email;
    this.passwordHash = passwordHash;
  }

}
