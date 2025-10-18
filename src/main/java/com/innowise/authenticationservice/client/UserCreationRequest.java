package com.innowise.authenticationservice.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record UserCreationRequest(

    @JsonProperty("userEmail") String email,
    @JsonProperty("userName") String name,
    @JsonProperty("userSurname") String surname,
    @JsonProperty("userBirthDate") LocalDate birthDate

) {}
