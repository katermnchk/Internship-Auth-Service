package com.innowise.authenticationservice.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(

    @JsonProperty("userId") Long id

) {}