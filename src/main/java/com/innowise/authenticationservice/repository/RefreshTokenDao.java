package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.entity.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenDao {

  RefreshToken save(Long userId, String token, Instant expiresAt);

  Optional<RefreshToken> getActive(String token);

  List<RefreshToken> getAllActiveTokens();

  List<RefreshToken> getAllByUser(Long userId);

  void revokeById(Long id);

  void revokeAllForUser(Long userId);

}
