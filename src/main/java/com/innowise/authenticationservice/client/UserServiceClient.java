package com.innowise.authenticationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${services.user.url}")
public interface UserServiceClient {

  @PostMapping("/api/v1/users")
  ApiResponseWrapper<UserResponse> createUser(@RequestBody UserCreationRequest request);

  @DeleteMapping("/api/v1/users/{id}")
  void deleteUser(@PathVariable("id") Long id);
}
