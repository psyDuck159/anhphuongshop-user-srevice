package biz.anhld.anhphuongshop.userservice.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;



import biz.anhld.anhphuongshop.userservice.entity.User;
import biz.anhld.anhphuongshop.userservice.dto.LoginRequest;
import biz.anhld.anhphuongshop.userservice.dto.SignupRequest;
import biz.anhld.anhphuongshop.userservice.dto.JwtResponse;
import biz.anhld.anhphuongshop.userservice.dto.MessageResponse;
import biz.anhld.anhphuongshop.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class UserService {
  @Autowired
  private KeycloakService keycloakService;
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

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

    

    String accessToken = keycloakService.getUserAccesToken(loginRequest.getUsername(), loginRequest.getPassword());

    return new JwtResponse(accessToken, user.getId(), user.getUsername(), user.getEmail());
  }

  
}
