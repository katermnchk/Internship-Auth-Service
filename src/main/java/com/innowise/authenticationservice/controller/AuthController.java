package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthResponseDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;
import com.innowise.authenticationservice.dto.LoginRequestDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.dto.RegistrationRequestDto;
import com.innowise.authenticationservice.dto.response.ApiResponse;
import com.innowise.authenticationservice.service.AuthUserService;
import com.innowise.authenticationservice.service.AuthenticationService;
import com.innowise.authenticationservice.service.JWTService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${app.api.base-path}/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthUserService authUserService;
  private final AuthenticationService authenticationService;
  private final JWTService jwtService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponseDto>> register(
      @Valid @RequestBody RegistrationRequestDto requestDto
  ) {
    AuthResponseDto response = authUserService.register(requestDto);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(new ApiResponse<>(HttpStatus.CREATED.value(),
            "User registered successfully", response));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthTokensDto>> login(
      @Valid @RequestBody LoginRequestDto requestDto
  ) {
    AuthTokensDto tokens = authenticationService.login(requestDto);
    return ResponseEntity
        .ok(new ApiResponse<>(HttpStatus.OK.value(),"Login successful", tokens));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthTokensDto>> refreshToken(
      @RequestParam("refreshToken") String refreshToken
  ) {
    AuthTokensDto tokens = authenticationService.refreshToken(refreshToken);
    return ResponseEntity
        .ok(new ApiResponse<>(HttpStatus.OK.value(),
            "Token refreshed successfully", tokens));
  }

  @PatchMapping("/password")
  public ResponseEntity<ApiResponse<Void>> updatePassword(
      @Valid @RequestBody PasswordUpdateDto dto
  ) {
    authUserService.updatePassword(dto);
    return ResponseEntity
        .ok(new ApiResponse<>(HttpStatus.OK.value(),
            "Password updated successfully", null));
  }

  @GetMapping("/validate")
  public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
      @RequestParam("accessToken") String accessToken
  ) {
    Map<String, Object> result = new HashMap<>();
    Claims claims = jwtService.validateToken(accessToken);

    boolean isValid = claims != null;
    result.put("isValid", isValid);
    if (isValid) {
      result.put("message", "Token is valid");
      result.put("userId", claims.getSubject());
    } else {
      result.put("message", "Token is invalid or expired");
    }

    return ResponseEntity.ok(
        new ApiResponse<>(HttpStatus.OK.value(), "Token validation result", result)
    );
  }


}
