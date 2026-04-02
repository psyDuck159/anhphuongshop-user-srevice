package biz.anhld.anhphuongshop.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  interface AuthoritiesConverter extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter(AuthoritiesConverter authoritiesConverter) {
    var authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
      return authoritiesConverter.convert(jwt.getClaims());
    });
    return authenticationConverter;
  }

  @Bean
  AuthoritiesConverter realmRolesAuthoritiesConverter() {
    return claims -> {
      var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
      var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));
      return roles.map(List::stream)
          .orElse(Stream.empty())
          .map(SimpleGrantedAuthority::new)
          .map(GrantedAuthority.class::cast)
          .toList();
    };
  }

  // @Bean
  // public JwtAuthenticationConverter jwtAuthenticationConverter() {
  //   JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
  //   // Không cần prefix "SCOPE_", chúng ta sẽ tự định nghĩa ROLE_
  //   authoritiesConverter.setAuthorityPrefix("");

  //   return new JwtAuthenticationConverter() {
  //     @Override
  //     protected Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
  //       // Lấy các role từ realm_access.roles
  //       Map<String, Object> realmAccess = jwt.getClaim("realm_access");
  //       if (realmAccess == null || !realmAccess.containsKey("roles")) {
  //         return List.of();
  //       }

  //       List<String> roles = (List<String>) realmAccess.get("roles");
  //       return roles.stream()
  //           .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName)) // Thêm tiền tố ROLE_
  //           .collect(Collectors.toList());
  //     }
  //   };
  // }

  @Bean
  SecurityFilterChain resourceServerSecurityFilterChain(
      HttpSecurity http,
      Converter<Jwt, AbstractAuthenticationToken> authenticationConverter
    ) throws Exception {
    http.oauth2ResourceServer(resourceServer -> {
      resourceServer.jwt(jwtDecoder -> {
        jwtDecoder.jwtAuthenticationConverter(authenticationConverter);
      });
    });

    http.sessionManagement(sessions -> {
      sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }).csrf(csrf -> {
      csrf.disable();
    });

    http.authorizeHttpRequests(requests -> {
      requests.requestMatchers("/actuator/**").permitAll();
      requests.requestMatchers("/api/v1/users/me").authenticated();
      requests.requestMatchers("/api/v1/users/signup").permitAll();
      requests.requestMatchers("/api/v1/users/login").permitAll();
      requests.requestMatchers("/api/v1/users/refresh").permitAll();
      requests.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/users").authenticated();
      requests.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/users/{id}").permitAll();
      requests.anyRequest().denyAll();
    });

    return http.build();
  }

}
