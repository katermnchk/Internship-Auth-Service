package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.util.PasswordUtil;
import com.innowise.authenticationservice.dto.RefreshTokenDto;
import com.innowise.authenticationservice.entity.RefreshToken;
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
    String hashedToken = PasswordUtil.hashPassword(refreshTokenDto.getToken()); //I'm not sure that it's a good practice
    Instant expiresAt = Instant.now().plusMillis(refreshTokenDto.getTimeToLive());
    return refreshTokenDao.save(refreshTokenDto.getUserId(), hashedToken, expiresAt);
  }

  @Override
  public Optional<RefreshToken> validateToken(String rawToken) {
    return refreshTokenDao.getAllActiveTokens()
        .stream()
        .filter(rt -> PasswordUtil.checkPassword(rawToken, rt.getToken()))
        .findFirst();
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
