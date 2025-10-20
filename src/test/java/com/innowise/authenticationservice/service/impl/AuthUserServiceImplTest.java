package com.innowise.authenticationservice.service.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.authenticationservice.client.ApiResponseWrapper;
import com.innowise.authenticationservice.client.UserCreationRequest;
import com.innowise.authenticationservice.client.UserResponse;
import com.innowise.authenticationservice.client.UserServiceClient;
import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.LoginRequestDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.dto.RegistrationRequestDto;
import com.innowise.authenticationservice.entity.AuthUser;
import com.innowise.authenticationservice.exception.InvalidOldPasswordException;
import com.innowise.authenticationservice.exception.InvalidPasswordException;
import com.innowise.authenticationservice.exception.UserAlreadyExistsException;
import com.innowise.authenticationservice.exception.UserNotFoundException;
import com.innowise.authenticationservice.repository.AuthUserDao;
import com.innowise.authenticationservice.service.RefreshTokenService;
import com.innowise.authenticationservice.util.PasswordUtil;
import java.time.Instant;
import java.time.LocalDate;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceImplTest {

  @Mock
  private AuthUserDao authUserDao;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private UserServiceClient userServiceClient;

  @InjectMocks
  private AuthUserServiceImpl authUserService;

  private MockedStatic<PasswordUtil> passwordUtilMock;

  @BeforeEach
  void setUp() {
    passwordUtilMock = mockStatic(PasswordUtil.class);
  }

  @AfterEach
  void tearDown() {
    passwordUtilMock.close();
  }

  @Test
  void givenValidRequest_whenRegister_thenCallUserServiceAndSaveCredentials() {
    long expectedUserId = 123L;
    RegistrationRequestDto request = new RegistrationRequestDto();
    request.setEmail("test@example.com");
    request.setPassword("password123");
    request.setName("John");
    request.setSurname("Doe");
    request.setBirthDate(LocalDate.of(1990, 1, 1));

    AuthUser savedUser =
        new AuthUser(1L, expectedUserId, "test@example.com", "hashedPass", Instant.now(), Instant.now());
    ApiResponseWrapper<UserResponse> userApiResponse =
        new ApiResponseWrapper<>(new UserResponse(expectedUserId));

    when(authUserDao.existsByEmail(request.getEmail())).thenReturn(false);
    when(userServiceClient.createUser(any(UserCreationRequest.class))).thenReturn(userApiResponse);
    passwordUtilMock.when(() -> PasswordUtil.hashPassword(request.getPassword())).thenReturn("hashedPass");
    when(authUserDao.save(expectedUserId, request.getEmail(), "hashedPass")).thenReturn(savedUser);

    AuthResponseDto response = authUserService.register(request);

    assertAll(
        () -> assertThat(response.getUserId(), is(expectedUserId)),
        () -> assertThat(response.getEmail(), is(request.getEmail()))
    );

    verify(userServiceClient).createUser(any(UserCreationRequest.class));
    verify(authUserDao).save(expectedUserId, request.getEmail(), "hashedPass");
  }

  @Test
  void givenExistingEmail_whenRegister_thenThrowUserAlreadyExistsException() {
    RegistrationRequestDto request = new RegistrationRequestDto();
    request.setEmail("test@example.com");

    when(authUserDao.existsByEmail(request.getEmail())).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> authUserService.register(request));

    verify(userServiceClient, never()).createUser(any());
    verify(authUserDao, never()).save(any(), any(), any());
  }

  @Test
  void givenValidCredentials_whenLogin_thenReturnAuthResponse() {
    LoginRequestDto request = new LoginRequestDto("test@example.com", "password123");
    AuthUser user = new AuthUser(100L, 1L, "test@example.com", "hashedPass");

    when(authUserDao.getUserByEmail(request.getEmail())).thenReturn(Optional.of(user));
    passwordUtilMock.when(() -> PasswordUtil.checkPassword(request.getPassword(), user.getPasswordHash()))
        .thenReturn(true);

    AuthResponseDto response = authUserService.login(request);

    assertAll(
        () -> assertThat(response.getId(), is(user.getId())),
        () -> assertThat(response.getUserId(), is(user.getUserId())),
        () -> assertThat(response.getEmail(), is(user.getEmail()))
    );
  }

  @Test
  void givenNonExistingEmail_whenLogin_thenThrowUserNotFoundException() {
    LoginRequestDto request = new LoginRequestDto("unknown@example.com", "password123");

    when(authUserDao.getUserByEmail(request.getEmail())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class,
        () -> authUserService.login(request));
  }

  @Test
  void givenWrongPassword_whenLogin_thenThrowInvalidPasswordException() {
    LoginRequestDto request = new LoginRequestDto("test@example.com", "wrongPass");
    AuthUser user = new AuthUser(100L, 1L, "test@example.com", "hashedPass");

    when(authUserDao.getUserByEmail(request.getEmail())).thenReturn(Optional.of(user));
    passwordUtilMock.when(() -> PasswordUtil.checkPassword(request.getPassword(), user.getPasswordHash()))
        .thenReturn(false);

    assertThrows(InvalidPasswordException.class,
        () -> authUserService.login(request));
  }

  @Test
  void givenExistingUser_whenGetByEmail_thenReturnAuthResponse() {
    AuthUser user = new AuthUser(100L, 1L, "test@example.com", "hashedPass");

    when(authUserDao.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));

    Optional<AuthResponseDto> result = authUserService.getByEmail("test@example.com");

    assertAll(
        () -> assertTrue(result.isPresent()),
        () -> assertThat(result.get().getEmail(), is(user.getEmail())),
        () -> assertThat(result.get().getUserId(), is(user.getUserId()))
    );
  }

  @Test
  void givenNonExistingUser_whenGetByEmail_thenReturnEmpty() {
    when(authUserDao.getUserByEmail("notfound@example.com")).thenReturn(Optional.empty());

    Optional<AuthResponseDto> result = authUserService.getByEmail("notfound@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  void givenExistingUser_whenUpdatePasswordWithCorrectOldPassword_thenPasswordUpdatedAndTokensRevoked() {
    Long userId = 1L;
    Long internalId = 100L;
    PasswordUpdateDto dto = new PasswordUpdateDto(userId, "oldPass", "newPass");
    AuthUser user = new AuthUser(internalId, userId, "test@example.com", "oldHash");

    when(authUserDao.getUserByUserId(userId)).thenReturn(Optional.of(user));
    passwordUtilMock.when(() -> PasswordUtil.checkPassword(dto.getOldPassword(), user.getPasswordHash()))
        .thenReturn(true);

    authUserService.updatePassword(dto);

    verify(authUserDao).updatePassword(eq(internalId), anyString());
    verify(refreshTokenService).revokeAllForUser(userId);
  }

  @Test
  void givenWrongOldPassword_whenUpdatePassword_thenThrowInvalidOldPasswordException() {
    Long userId = 1L;
    Long internalId = 100L;
    PasswordUpdateDto dto = new PasswordUpdateDto(userId, "wrongOld", "newPass");
    AuthUser user = new AuthUser(internalId, userId, "test@example.com", "oldHash");

    when(authUserDao.getUserByUserId(userId)).thenReturn(Optional.of(user));
    passwordUtilMock.when(() -> PasswordUtil.checkPassword(dto.getOldPassword(), user.getPasswordHash()))
        .thenReturn(false);

    assertThrows(InvalidOldPasswordException.class,
        () -> authUserService.updatePassword(dto));

    verify(authUserDao, never()).updatePassword(any(), any());
    verify(refreshTokenService, never()).revokeAllForUser(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"exists@example.com", "user@domain.com"})
  void givenEmails_whenExistsByEmail_thenReturnTrue(String email) {
    when(authUserDao.existsByEmail(email)).thenReturn(true);

    boolean exists = authUserService.existsByEmail(email);

    assertTrue(exists);
  }
}
