package biz.anhld.anhphuongshop.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import biz.anhld.anhphuongshop.userservice.dto.JwtResponse;
import biz.anhld.anhphuongshop.userservice.dto.LoginRequest;
import biz.anhld.anhphuongshop.userservice.dto.SignupRequest;
import biz.anhld.anhphuongshop.userservice.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/signup")
  public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest signUpRequest) {
    return ResponseEntity.ok(userService.registerUser(signUpRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(
    @Valid @RequestBody LoginRequest loginRequest,
    HttpServletResponse servletResponse
  ) {
    JwtResponse response = userService.authenticateUser(loginRequest);

    Cookie cookie = new Cookie("access_token", response.getToken()); // Store JWT in HttpOnly cookie
    cookie.setHttpOnly(true); // Mitigate XSS attacks
    cookie.setPath("/"); // Cookie is valid for the entire domain
    // cookie.setSecure(true); // Enable this in production with HTTPS
    cookie.setMaxAge(7 * 24 * 60 * 60);
    cookie.setAttribute("SameSite", "None");
  
    servletResponse.addCookie(cookie);
    
    return ResponseEntity.ok(response);
  }

  public static record UserInfoDto(String preferredUsername,
    String name,
    List<String> roles
  ) {}

  @GetMapping("/me")
  public UserInfoDto getGretting(JwtAuthenticationToken auth) {
    return new UserInfoDto(
        auth.getToken().getClaimAsString(StandardClaimNames.PREFERRED_USERNAME),
        auth.getToken().getClaimAsString(StandardClaimNames.NAME),
        auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
  }

  
}
