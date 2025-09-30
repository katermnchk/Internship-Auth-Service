package com.innowise.authenticationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDto {

  @NotNull(message = "userId can't be empty")
  private Long userId;

  @NotBlank(message = "New password can't be empty")
  @Size(min = 8, max = 255, message = "Password should contain from 8 to 255 symbols")
  private String newPassword;
}
