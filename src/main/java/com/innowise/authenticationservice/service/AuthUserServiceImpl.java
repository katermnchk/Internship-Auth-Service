package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.entity.AuthUser;
import com.innowise.authenticationservice.repository.AuthUserDao;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

  private final AuthUserDao authUserDao;
  private final PasswordEncoder passwordEncoder;

  @Override
  public AuthResponseDto register(AuthRequestDto requestDto) {

    if (authUserDao.existsByEmail(requestDto.getEmail())) {
      throw new RuntimeException("User with email already exists");
    }

    String hashedPassword = passwordEncoder.encode(requestDto.getPassword());
    AuthUser savedUser = authUserDao.save(requestDto.getEmail(), hashedPassword);
    return new AuthResponseDto(savedUser.getId(), savedUser.getEmail());
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
  public void updatePassword(PasswordUpdateDto dto) {
    String hashedPassword = passwordEncoder.encode(dto.getNewPassword());
    authUserDao.updatePassword(dto.getUserId(), hashedPassword);
  }
}
