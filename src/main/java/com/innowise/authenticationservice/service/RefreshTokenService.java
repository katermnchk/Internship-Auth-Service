package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.RefreshTokenDto;
import com.innowise.authenticationservice.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenService {

  RefreshToken createToken(RefreshTokenDto refreshTokenDto);

  Optional<RefreshToken> validateToken(String token);

  void revokeToken(Long id);

  void revokeAllForUser(Long userId);

}
