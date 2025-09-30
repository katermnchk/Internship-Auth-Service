package com.innowise.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

  @NotBlank(message = "Email can't be empty")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Password can't be empty")
  @Size(min = 8, max = 255, message = "Password should contain from 8 to 255 symbols")
  private String password;
}
