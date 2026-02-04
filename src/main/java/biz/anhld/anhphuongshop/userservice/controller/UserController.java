package biz.anhld.anhphuongshop.userservice.controller;

import biz.anhld.anhphuongshop.userservice.dto.RefreshRequest;
import biz.anhld.anhphuongshop.userservice.entity.User;
import biz.anhld.anhphuongshop.userservice.exception.BadRequestException;
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

  @PostMapping("/refresh")
  public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
    return ResponseEntity.ok(userService.refreshToken(refreshRequest));
  }

  public static record UserInfoDto(String preferredUsername,
    String name,
    Long userId,
    List<String> roles
  ) {}

  @GetMapping("/me")
  public UserInfoDto getGretting(JwtAuthenticationToken auth) throws BadRequestException {
    String username = auth.getToken().getClaimAsString(StandardClaimNames.PREFERRED_USERNAME);
    User user = userService.getUserByUsername(username);
    return new UserInfoDto(
            username,
            auth.getToken().getClaimAsString(StandardClaimNames.NAME),
            user.getId(),
            auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
  }

  
}
