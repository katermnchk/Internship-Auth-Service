package com.innowise.authenticationservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;
import com.innowise.authenticationservice.dto.RefreshTokenDto;
import com.innowise.authenticationservice.entity.RefreshToken;
import com.innowise.authenticationservice.exception.InvalidRefreshTokenException;
import com.innowise.authenticationservice.service.AuthUserService;
import com.innowise.authenticationservice.service.JWTService;
import com.innowise.authenticationservice.service.RefreshTokenService;
import com.innowise.authenticationservice.util.TokenUtil;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;



@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

  @Mock
  private AuthUserService authUserService;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private JWTService jwtService;

  @InjectMocks
  private AuthenticationServiceImpl authenticationService;

  private final Long userId = 1L;
  private final Long internalId = 100L;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authenticationService, "refreshTokenExpiration", 1000L);
  }

  @Test
  void givenValidCredentials_whenLogin_thenReturnTokensAndRevokeOldOnes() {
    AuthRequestDto request = new AuthRequestDto(userId, "test@example.com", "password123");
    AuthResponseDto user = new AuthResponseDto(internalId, userId, "test@example.com");

    when(authUserService.login(request)).thenReturn(user);

    try (MockedStatic<TokenUtil> utilities = mockStatic(TokenUtil.class)) {
      utilities.when(() -> TokenUtil.generateTokens(eq(user), eq(jwtService)))
          .thenReturn(Map.of("accessToken", "access", "refreshToken", "refresh"));

      AuthTokensDto result = authenticationService.login(request);

      assertAll(
          () -> assertEquals("access", result.getAccessToken()),
          () -> assertEquals("refresh", result.getRefreshToken())
      );

      verify(refreshTokenService).revokeAllForUser(userId);
      verify(refreshTokenService).createToken(any(RefreshTokenDto.class));
    }
  }

  @Test
  void givenValidRefreshToken_whenRefresh_thenReturnNewTokens() {
    RefreshToken existingToken = new RefreshToken(
        1L,
        userId,
        "refresh",
        Instant.now().plusSeconds(3600),
        false,
        Instant.now()
    );

    when(refreshTokenService.validateToken("refresh")).thenReturn(Optional.of(existingToken));

    try (MockedStatic<TokenUtil> utilities = mockStatic(TokenUtil.class)) {
      utilities.when(() -> TokenUtil.generateTokens(any(AuthResponseDto.class), eq(jwtService)))
          .thenReturn(Map.of("accessToken", "newAccess", "refreshToken", "newRefresh"));

      AuthTokensDto tokens = authenticationService.refreshToken("refresh");

      assertAll(
          () -> assertEquals("newAccess", tokens.getAccessToken()),
          () -> assertEquals("newRefresh", tokens.getRefreshToken())
      );

      verify(refreshTokenService).revokeToken(existingToken.getId());
      verify(refreshTokenService).createToken(any(RefreshTokenDto.class));
    }
  }

  @Test
  void givenInvalidRefreshToken_whenRefresh_thenThrowException() {
    when(refreshTokenService.validateToken("invalid")).thenReturn(Optional.empty());

    assertThrows(InvalidRefreshTokenException.class,
        () -> authenticationService.refreshToken("invalid"));
  }
}
