package com.innowise.authenticationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDto {

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(min = 8, max = 255)
  private String password;

  @NotBlank
  @Pattern(regexp = "^[\\p{L}\\s-]+$")
  private String name;

  @NotBlank
  @Pattern(regexp = "^[\\p{L}\\s-]+$")
  private String surname;

  @Past
  private LocalDate birthDate;
}