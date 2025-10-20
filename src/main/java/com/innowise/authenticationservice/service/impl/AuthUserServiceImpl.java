package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.client.ApiResponseWrapper;
import com.innowise.authenticationservice.client.UserCreationRequest;
import com.innowise.authenticationservice.client.UserResponse;
import com.innowise.authenticationservice.client.UserServiceClient;
import com.innowise.authenticationservice.dto.LoginRequestDto;
import com.innowise.authenticationservice.dto.RegistrationRequestDto;
import com.innowise.authenticationservice.exception.InvalidOldPasswordException;
import com.innowise.authenticationservice.service.RefreshTokenService;
import com.innowise.authenticationservice.util.PasswordUtil;
import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.entity.AuthUser;
import com.innowise.authenticationservice.exception.InvalidPasswordException;
import com.innowise.authenticationservice.exception.UserAlreadyExistsException;
import com.innowise.authenticationservice.exception.UserNotFoundException;
import com.innowise.authenticationservice.repository.AuthUserDao;
import com.innowise.authenticationservice.service.AuthUserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

  private final AuthUserDao authUserDao;
  private final RefreshTokenService refreshTokenService;
  private final UserServiceClient userServiceClient;


  @Override
  @Transactional
  public AuthResponseDto register(RegistrationRequestDto requestDto) {

    if (authUserDao.existsByEmail(requestDto.getEmail())) {
      throw new UserAlreadyExistsException(requestDto.getEmail());
    }

    UserResponse createdUserResponse = null;
    try {
      log.info("Attempting to create user in User Service for email: {}", requestDto.getEmail());
      UserCreationRequest userCreationRequest = new UserCreationRequest(
          requestDto.getEmail(), requestDto.getName(), requestDto.getSurname(), requestDto.getBirthDate()
      );
      ApiResponseWrapper<UserResponse> apiResponse = userServiceClient.createUser(userCreationRequest);
      createdUserResponse = apiResponse.data();

      if (createdUserResponse == null || createdUserResponse.id() == null) {
        throw new IllegalStateException("User Service returned a null user or null ID.");
      }
      log.info("User created in User Service with ID: {}", createdUserResponse.id());

      String hashedPassword = PasswordUtil.hashPassword(requestDto.getPassword());

      AuthUser savedUser = authUserDao.save(createdUserResponse.id(), requestDto.getEmail(), hashedPassword);
      log.info("Credentials saved locally for userId: {}", savedUser.getUserId());

      return new AuthResponseDto(savedUser.getId(), savedUser.getUserId(), savedUser.getEmail());

    } catch (DataAccessException e) {
      log.error("Failed to save credentials to local DB after user was created. Rolling back.", e);
      if (createdUserResponse != null) {
        log.warn("Executing rollback: Deleting user with ID {} from User Service.", createdUserResponse.id());
        try {
          userServiceClient.deleteUser(createdUserResponse.id());
          log.info("Rollback successful. User {} deleted from User Service.", createdUserResponse.id());
        } catch (Exception ex) {
          log.error("Rollback FAILED. User with ID {} exists in User Service but not in Auth Service.", createdUserResponse.id(), ex);
        }
      }
      throw new RuntimeException("User registration failed due to an internal error. The operation has been rolled back.", e);
    } catch (Exception e) {
      log.error("An error occurred during communication with User Service.", e);
      throw new RuntimeException("Failed to create user in the external User Service.", e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public AuthResponseDto login(LoginRequestDto requestDto) {
    AuthUser user = authUserDao.getUserByEmail(requestDto.getEmail())
        .orElseThrow(() -> new UserNotFoundException(requestDto.getEmail()));

    if (!PasswordUtil.checkPassword(requestDto.getPassword(), user.getPasswordHash())) {
      throw new InvalidPasswordException();
    }

    return new AuthResponseDto(user.getId(), user.getUserId(), user.getEmail());
  }

  @Override
  public Optional<AuthResponseDto> getByEmail(String email) {
    return authUserDao.getUserByEmail(email)
        .map(user -> new AuthResponseDto(user.getId(), user.getUserId(), user.getEmail()));
  }

  @Override
  public Optional<AuthResponseDto> getById(Long id) {
    return authUserDao.getUserByUserId(id)
        .map(user -> new AuthResponseDto(user.getId(), user.getUserId(), user.getEmail()));
  }

  @Override
  public boolean existsByEmail(String email) {
    return authUserDao.existsByEmail(email);
  }

  @Override
  @Transactional
  public void updatePassword(PasswordUpdateDto dto) {
    AuthUser user = authUserDao.getUserByUserId(dto.getUserId())
        .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

    if (!PasswordUtil.checkPassword(dto.getOldPassword(), user.getPasswordHash())) {
      throw new InvalidOldPasswordException();
    }

    String hashedPassword = BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt());
    authUserDao.updatePassword(user.getId(), hashedPassword);

    refreshTokenService.revokeAllForUser(user.getUserId());
  }

}
