package biz.anhld.anhphuongshop.userservice.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
  @Value("${keycloak.base_url}")
  private String keycloakBaseUrl;

  @Value("${keycloak.realm}")
  private String keycloakRealm;

  @Value("${keycloak.username}")
  private String keycloakUsername;

  @Value("${keycloak.password}")
  private String keycloakPassword;

  @Value("${keycloak.client_id}")
  private String keycloakClientId;

  @Bean
  public Keycloak keycloakInstance() {
    return Keycloak.getInstance(
        keycloakBaseUrl,
        keycloakRealm,
        keycloakUsername,
        keycloakPassword,
        keycloakClientId);
  }

  public RealmRepresentation getRealmRepresentation() {
    return keycloakInstance().realm(keycloakRealm).toRepresentation();
  }
}