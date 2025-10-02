package com.innowise.authenticationservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.dto.response.ApiResponse;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
class AuthControllerTest extends AbstractIntegrationTest {

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.update("DELETE FROM refresh_tokens");
    jdbcTemplate.update("DELETE FROM auth_users");
  }

  private static final Long TEST_USER_ID = 1L;
  private static final String TEST_USER_EMAIL = "user@example.com";
  private static final String TEST_USER_PASSWORD = "password123";

  private AuthRequestDto defaultAuthRequest() {
    return new AuthRequestDto(TEST_USER_ID, TEST_USER_EMAIL, TEST_USER_PASSWORD);
  }

  private AuthRequestDto authRequest(Long userId, String email, String password) {
    return new AuthRequestDto(userId, email, password);
  }

  @Nested
  class RegisterTests {

    @Test
    void givenValidRequest_whenRegister_thenUserIsCreated() throws Exception {
      AuthRequestDto request = defaultAuthRequest();

      mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpectAll(
              jsonPath("$.status", is(201)),
              jsonPath("$.message", is("User registered successfully")),
              jsonPath("$.data.email", is("user@example.com"))
          );

      assertTrue(authUserDao.existsByEmail(request.getEmail()));
    }

    @Test
    void givenExistingEmail_whenRegister_thenConflict() throws Exception {
      AuthRequestDto request = defaultAuthRequest();

      mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.error", is("Conflict")));
    }

    private static Stream<Arguments> provideInvalidAuthRequests() {
      return Stream.of(
          Arguments.of(new AuthRequestDto(1L, "invalid-email", "password123"), "email: Email should be valid"),
          Arguments.of(new AuthRequestDto(1L, "test@example.com", "short"), "password: Password should contain from 8 to 255 symbols"),
          Arguments.of(new AuthRequestDto(1L, "", "password123"), "email: Email can't be empty"),
          Arguments.of(new AuthRequestDto(1L, "test@example.com", ""), "password: Password can't be empty")
      );
    }

    @ParameterizedTest()
    @MethodSource("provideInvalidAuthRequests")
    void givenInvalidRequest_whenRegister_thenBadRequest(
        AuthRequestDto invalidDto, String expectedError
    ) throws Exception {
      mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(invalidDto)))
          .andExpect(status().isBadRequest())
          .andExpectAll(
              jsonPath("$.error", is("Bad request")),
              jsonPath("$.message", hasItem(expectedError))
          );
    }
  }

  @Nested
  class LoginTests {

    @BeforeEach
    void registerUser() throws Exception {
      mockMvc.perform(post("/api/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(defaultAuthRequest())));
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnTokens() throws Exception {
      AuthRequestDto request = defaultAuthRequest();

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.status", is(200)),
              jsonPath("$.data.accessToken", notNullValue()),
              jsonPath("$.data.refreshToken", notNullValue())
          );
    }

    @Test
    void givenInvalidPassword_whenLogin_thenBadRequest() throws Exception {
      AuthRequestDto request = authRequest(TEST_USER_ID, TEST_USER_EMAIL, "wrong-password");

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void givenNonExistentUser_whenLogin_thenNotFound() throws Exception {
      AuthRequestDto request = authRequest(99L, "notfound@example.com", "password123");

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class AuthenticatedEndpoints {

    private Long testUserId;
    private String accessToken;
    private String refreshToken;
    private final AuthRequestDto testUser = defaultAuthRequest();

    @BeforeEach
    void registerAndLoginUser() throws Exception {
      MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(testUser)))
          .andExpect(status().isCreated())
          .andReturn();

      String registerResponse = registerResult.getResponse().getContentAsString();
      this.testUserId = JsonPath.parse(registerResponse).read("$.data.id", Long.class);

      MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(testUser)))
          .andExpect(status().isOk())
          .andReturn();

      ApiResponse<AuthTokensDto> apiResponse =
          objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<>() {});
      this.accessToken = apiResponse.getData().getAccessToken();
      this.refreshToken = apiResponse.getData().getRefreshToken();
    }

    @Test
    void givenValidRefreshToken_whenRefresh_thenReturnNewTokens() throws Exception {
      mockMvc.perform(post("/api/v1/auth/refresh").param("refreshToken", refreshToken))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.data.accessToken", notNullValue()),
              jsonPath("$.data.refreshToken", notNullValue())
          );
    }

    @Test
    void givenValidPasswordUpdate_whenUpdate_thenPasswordChanges() throws Exception {
      PasswordUpdateDto passwordUpdate =
          new PasswordUpdateDto(this.testUserId, TEST_USER_PASSWORD, "newPassword123");

      mockMvc.perform(patch("/api/v1/auth/password")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(passwordUpdate)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message", is("Password updated successfully")));

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(testUser)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidAccessToken_whenValidate_thenTokenIsValid() throws Exception {
      mockMvc.perform(get("/api/v1/auth/validate").param("accessToken", accessToken))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.data.isValid", is(true)),
              jsonPath("$.data.userId", is(this.testUserId.toString()))
          );
    }

    @Test
    void givenInvalidAccessToken_whenValidate_thenTokenIsInvalid() throws Exception {
      mockMvc.perform(get("/api/v1/auth/validate").param("accessToken", "invalid.token"))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.data.isValid", is(false)),
              jsonPath("$.data.message", is("Token is invalid or expired"))
          );
    }
  }
}