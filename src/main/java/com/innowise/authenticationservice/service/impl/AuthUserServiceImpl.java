package com.innowise.authenticationservice.service.impl;

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
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

  private final AuthUserDao authUserDao;
  private final RefreshTokenService refreshTokenService;

  @Override
  @Transactional
  public AuthResponseDto register(AuthRequestDto requestDto) {

    if (authUserDao.existsByEmail(requestDto.getEmail())) {
      throw new UserAlreadyExistsException(requestDto.getEmail());
    }

    String hashedPassword = PasswordUtil.hashPassword(requestDto.getPassword());
    AuthUser savedUser = authUserDao.save(requestDto.getUserId() ,requestDto.getEmail(), hashedPassword);
    return new AuthResponseDto(savedUser.getId(), savedUser.getEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public AuthResponseDto login(AuthRequestDto requestDto) {
    AuthUser user = authUserDao.getUserByEmail(requestDto.getEmail())
        .orElseThrow(() -> new UserNotFoundException(requestDto.getEmail()));

    if (!PasswordUtil.checkPassword(requestDto.getPassword(), user.getPasswordHash())) {
      throw new InvalidPasswordException();
    }

    return new AuthResponseDto(user.getId(), user.getEmail());
  }

  @Override
  public Optional<AuthResponseDto> getByEmail(String email) {
    return authUserDao.getUserByEmail(email)
        .map(user -> new AuthResponseDto(user.getId(), user.getEmail()));
  }

  @Override
  public Optional<AuthResponseDto> getById(Long id) {
    return authUserDao.getUserById(id)
        .map(user -> new AuthResponseDto(user.getId(), user.getEmail()));
  }

  @Override
  public boolean existsByEmail(String email) {
    return authUserDao.existsByEmail(email);
  }

  @Override
  @Transactional
  public void updatePassword(PasswordUpdateDto dto) {
    AuthUser user = authUserDao.getUserById(dto.getUserId())
        .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

    if (!PasswordUtil.checkPassword(dto.getOldPassword(), user.getPasswordHash())) {
      throw new InvalidOldPasswordException();
    }

    String hashedPassword = BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt());
    authUserDao.updatePassword(dto.getUserId(), hashedPassword);

    refreshTokenService.revokeAllForUser(user.getId());
  }

}
