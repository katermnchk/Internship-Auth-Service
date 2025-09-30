package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.dto.RefreshTokenDto;
import com.innowise.authenticationservice.entity.RefreshToken;
import com.innowise.authenticationservice.exception.InvalidRefreshTokenException;
import com.innowise.authenticationservice.repository.RefreshTokenDao;
import com.innowise.authenticationservice.service.RefreshTokenService;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final RefreshTokenDao refreshTokenDao;

  @Override
  public RefreshToken createToken(RefreshTokenDto refreshTokenDto) {
    Instant expiresAt = Instant.now().plusMillis(refreshTokenDto.getTimeToLive());
    return refreshTokenDao.save(refreshTokenDto.getUserId(), refreshTokenDto.getToken(), expiresAt);
  }

  @Override
  public Optional<RefreshToken> validateToken(String token) {
    return refreshTokenDao.getActive(token);
  }

  @Override
  public void revokeToken(Long id) {
    refreshTokenDao.revokeById(id);
  }

  @Override
  public void revokeAllForUser(Long userId) {
    refreshTokenDao.revokeAllForUser(userId);
  }
}
