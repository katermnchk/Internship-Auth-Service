package com.innowise.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {

  @NotBlank(message = "Email can't be empty")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password can't be empty")
  private String password;
}
