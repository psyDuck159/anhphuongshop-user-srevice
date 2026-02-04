package biz.anhld.anhphuongshop.userservice.service;

import biz.anhld.anhphuongshop.userservice.dto.*;
import biz.anhld.anhphuongshop.userservice.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerProperties.Jwt;

import biz.anhld.anhphuongshop.userservice.entity.User;
import biz.anhld.anhphuongshop.userservice.mapper.JwtMapper;
import biz.anhld.anhphuongshop.userservice.dto.keycloak.TokenResponse;
import biz.anhld.anhphuongshop.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserService {
  private final KeycloakService keycloakService;
  private final UserRepository userRepository;
  private final JwtMapper jwtMapper;


  public String registerUser(SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return "Username is already taken!";
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return "Email is already in use!";
    }

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    // Create new user's account
    User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
        encoder.encode(signUpRequest.getPassword()));

    userRepository.save(user);
    String res = keycloakService.createUser(signUpRequest);
    log.info(res);

    return "User registered successfully!";
  }

  public JwtResponse authenticateUser(LoginRequest loginRequest) {
    User user;
    try {
      user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
    } catch (Exception e) {
      return new JwtResponse("User not found!");
    }

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
      return new JwtResponse("User credentials are not valid");
    }

    TokenResponse tokenResponse = keycloakService.getUserAccessToken(loginRequest.getUsername(), loginRequest.getPassword());
    
    if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
      return new JwtResponse("Failed to retrieve access token");
    }

    JwtResponse jwtResponse = jwtMapper.toJwtResponse(tokenResponse);
    jwtResponse.setId(user.getId());
    jwtResponse.setUsername(user.getUsername());
    jwtResponse.setEmail(user.getEmail());
    jwtResponse.setMessage("User logged in successfully");

    return jwtResponse;
  }

  public JwtResponse refreshToken(RefreshRequest refreshRequest) {
    TokenResponse accessToken = keycloakService.getUserAccessToken(refreshRequest.getRefreshToken());
    return jwtMapper.toJwtResponse(accessToken);
  }

  public User getUserByUsername(String username) throws BadRequestException {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new BadRequestException(username + " Not Found"));
  }
}
