package com.innowise.authenticationservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.innowise.authenticationservice.client.ApiResponseWrapper;
import com.innowise.authenticationservice.client.UserResponse;
import com.innowise.authenticationservice.client.UserServiceClient;
import com.innowise.authenticationservice.dto.AuthRequestDto;
import com.innowise.authenticationservice.dto.AuthTokensDto;
import com.innowise.authenticationservice.dto.LoginRequestDto;
import com.innowise.authenticationservice.dto.PasswordUpdateDto;
import com.innowise.authenticationservice.dto.RegistrationRequestDto;
import com.innowise.authenticationservice.dto.response.ApiResponse;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Stream;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
class AuthControllerTest extends AbstractIntegrationTest {

  @MockitoBean
  private UserServiceClient userServiceClient;

  @BeforeEach
  void cleanDatabase() {
    jdbcTemplate.update("DELETE FROM refresh_tokens");
    jdbcTemplate.update("DELETE FROM auth_users");
  }

  private static final Long TEST_USER_ID = 42L;
  private static final String TEST_USER_EMAIL = "user@example.com";
  private static final String TEST_USER_PASSWORD = "password123";

  private RegistrationRequestDto defaultRegistrationRequest() {
    RegistrationRequestDto dto = new RegistrationRequestDto();
    dto.setEmail(TEST_USER_EMAIL);
    dto.setPassword(TEST_USER_PASSWORD);
    dto.setName("Test");
    dto.setSurname("User");
    dto.setBirthDate(LocalDate.of(2000, 1, 1));
    return dto;
  }

  private LoginRequestDto defaultLoginRequest() {
    return new LoginRequestDto(TEST_USER_EMAIL, TEST_USER_PASSWORD);
  }

  private AuthRequestDto authRequest(Long userId, String email, String password) {
    return new AuthRequestDto(userId, email, password);
  }

  @Nested
  class RegisterTests {

    @Test
    void givenValidRequest_whenRegister_thenUserIsCreated() throws Exception {

      RegistrationRequestDto request = defaultRegistrationRequest();

      ApiResponseWrapper<UserResponse> fakeApiResponse =
          new ApiResponseWrapper<>(new UserResponse(TEST_USER_ID));
      when(userServiceClient.createUser(any())).thenReturn(fakeApiResponse);

      mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpectAll(
              jsonPath("$.status", is(201)),
              jsonPath("$.message", is("User registered successfully")),
              jsonPath("$.data.email", is(request.getEmail())),
              jsonPath("$.data.userId", is(TEST_USER_ID.intValue()))
          );

      assertTrue(authUserDao.existsByEmail(request.getEmail()));
    }

    @Test
    void givenExistingEmail_whenRegister_thenConflict() throws Exception {
      RegistrationRequestDto request = defaultRegistrationRequest();
      when(userServiceClient.createUser(any())).thenReturn(new ApiResponseWrapper<>(new UserResponse(1L)));

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

    private static Stream<Arguments> provideInvalidRegistrationRequests() {
      RegistrationRequestDto valid = new AuthControllerTest().defaultRegistrationRequest();
      return Stream.of(
          Arguments.of(new RegistrationRequestDto(
                  valid.getEmail(),
                  "short",
                  valid.getName(),
                  valid.getSurname(),
                  valid.getBirthDate()
              ),
              "password: size must be between 8 and 255"
          ),
          Arguments.of(new RegistrationRequestDto(
                  "invalid-email",
                  valid.getPassword(),
                  valid.getName(),
                  valid.getSurname(),
                  valid.getBirthDate()
              ),
              "email: must be a well-formed email address"
          ),
          Arguments.of(new RegistrationRequestDto(
              valid.getEmail(),
              valid.getPassword(),
              "",
              valid.getSurname(),
              valid.getBirthDate()
          ), "name: must not be blank")
      );
    }

    @ParameterizedTest()
    @MethodSource("provideInvalidRegistrationRequests")
    void givenInvalidRequest_whenRegister_thenBadRequest(
        RegistrationRequestDto invalidDto, String expectedError
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
      when(userServiceClient.createUser(any()))
          .thenReturn(new ApiResponseWrapper<>(new UserResponse(TEST_USER_ID)));
      mockMvc.perform(post("/api/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(defaultRegistrationRequest())));
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnTokens() throws Exception {
      LoginRequestDto request = defaultLoginRequest();

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

    private Long externalUserId;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void registerAndLoginUser() throws Exception {
      when(userServiceClient.createUser(any()))
          .thenReturn(new ApiResponseWrapper<>(new UserResponse(TEST_USER_ID)));

      MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(defaultRegistrationRequest())))
          .andExpect(status().isCreated())
          .andReturn();

      String registerResponse = registerResult.getResponse().getContentAsString();
          this.externalUserId = JsonPath.parse(registerResponse).read("$.data.userId", Long.class);

      MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(defaultLoginRequest())))
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
          new PasswordUpdateDto(this.externalUserId, TEST_USER_PASSWORD, "newPassword123");

      mockMvc.perform(patch("/api/v1/auth/password")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(passwordUpdate)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message", is("Password updated successfully")));

      mockMvc.perform(post("/api/v1/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(defaultLoginRequest())))
          .andExpect(status().isBadRequest());
    }

    @Test
    void givenValidAccessToken_whenValidate_thenTokenIsValid() throws Exception {
      mockMvc.perform(get("/api/v1/auth/validate").param("accessToken", accessToken))
          .andExpect(status().isOk())
          .andExpectAll(
              jsonPath("$.data.isValid", is(true)),
              jsonPath("$.data.userId", is(this.externalUserId.toString()))
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