package com.innowise.authenticationservice.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.innowise.authenticationservice.dto.RefreshTokenDto;
import com.innowise.authenticationservice.entity.RefreshToken;
import com.innowise.authenticationservice.repository.RefreshTokenDao;
import com.innowise.authenticationservice.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

  @Mock
  private RefreshTokenDao refreshTokenDao;

  @InjectMocks
  private RefreshTokenServiceImpl refreshTokenService;

  private MockedStatic<PasswordUtil> passwordUtilMock;

  @BeforeEach
  void setUp() {
    if (passwordUtilMock != null) {
      passwordUtilMock.close();
    }
    passwordUtilMock = mockStatic(PasswordUtil.class);
  }

  @AfterEach
  void tearDown() {
    if (passwordUtilMock != null) {
      passwordUtilMock.close();
    }
  }

  @Test
  void givenRefreshTokenDto_whenCreateToken_thenReturnSavedToken() {
    RefreshTokenDto dto = new RefreshTokenDto(1L, "rawToken", 1000L);
    RefreshToken savedToken = new RefreshToken(1L, dto.getUserId(), "hashedToken", Instant.now().plusMillis(1000), false, Instant.now());

    passwordUtilMock.when(() -> PasswordUtil.hashPassword(dto.getToken())).thenReturn("hashedToken");
    when(refreshTokenDao.save(eq(dto.getUserId()), eq("hashedToken"), any())).thenReturn(savedToken);

    RefreshToken result = refreshTokenService.createToken(dto);

    assertAll(
        () -> assertThat(result.getId(), is(savedToken.getId())),
        () -> assertThat(result.getUserId(), is(savedToken.getUserId())),
        () -> assertThat(result.getToken(), is(savedToken.getToken()))
    );

    verify(refreshTokenDao).save(eq(dto.getUserId()), eq("hashedToken"), any());
  }


  @Test
  void givenActiveTokens_whenValidateToken_thenReturnMatchingToken() {
    RefreshToken token1 = new RefreshToken(1L, 1L, "hash1", Instant.now().plusSeconds(60), false, Instant.now());
    RefreshToken token2 = new RefreshToken(2L, 2L, "hash2", Instant.now().plusSeconds(60), false, Instant.now());

    when(refreshTokenDao.getAllActiveTokens()).thenReturn(List.of(token1, token2));
    passwordUtilMock.when(() -> PasswordUtil.checkPassword("raw2", "hash2")).thenReturn(true);

    Optional<RefreshToken> result = refreshTokenService.validateToken("raw2");

    assertAll(
        () -> assertTrue(result.isPresent()),
        () -> assertThat(result.get().getId(), is(2L))
    );
  }

  @Test
  void givenNoMatchingToken_whenValidateToken_thenReturnEmpty() {
    RefreshToken token = new RefreshToken(1L, 1L, "hash1", Instant.now().plusSeconds(60), false, Instant.now());

    when(refreshTokenDao.getAllActiveTokens()).thenReturn(List.of(token));
    passwordUtilMock.when(() -> PasswordUtil.checkPassword("wrongRaw", "hash1")).thenReturn(false);

    Optional<RefreshToken> result = refreshTokenService.validateToken("wrongRaw");

    assertTrue(result.isEmpty());
  }

  @Test
  void givenTokenId_whenRevokeToken_thenDaoMethodCalled() {
    Long tokenId = 5L;
    refreshTokenService.revokeToken(tokenId);
    verify(refreshTokenDao).revokeById(tokenId);
  }

  @Test
  void givenUserId_whenRevokeAllForUser_thenDaoMethodCalled() {
    Long userId = 7L;
    refreshTokenService.revokeAllForUser(userId);
    verify(refreshTokenDao).revokeAllForUser(userId);
  }
}
