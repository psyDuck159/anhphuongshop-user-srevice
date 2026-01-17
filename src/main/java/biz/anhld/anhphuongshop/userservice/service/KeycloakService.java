package biz.anhld.anhphuongshop.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;

import biz.anhld.anhphuongshop.userservice.dto.SignupRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KeycloakService {
  @Value("${jwt.issuer_uri}")
  String jwtIssuerUri;

  @Value("${jwt.client_id}")
  String jwtClientId;

  @Value("${jwt.client_secret}")
  String jwtClientSecret;

  @Value("${jwt.grant_type}")
  String jwtGrantType;

  @Value("${jwt.scope}")
  String jwtScope;

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

  @Autowired
  private Keycloak keycloakInstance;


  public String getAdminAccesToken() {
    HttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(jwtIssuerUri);

    List<BasicNameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("grant_type", jwtGrantType));
    params.add(new BasicNameValuePair("client_id", jwtClientId));
    params.add(new BasicNameValuePair("client_secret", jwtClientSecret));
    params.add(new BasicNameValuePair("scope", jwtScope));

    String accessToken = "";
    try {
      httpPost.setEntity(new UrlEncodedFormEntity(params));
      HttpResponse response = httpClient.execute(httpPost);

      // Handle the response
      String responseBody = EntityUtils.toString(response.getEntity());

      accessToken = extractAccessToken(responseBody);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return accessToken;
  }

  public String getUserAccesToken(String username, String password) {
    HttpClient httpClient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(jwtIssuerUri);

    List<BasicNameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("grant_type", "password"));
    params.add(new BasicNameValuePair("client_id", jwtClientId));
    params.add(new BasicNameValuePair("client_secret", jwtClientSecret));
    params.add(new BasicNameValuePair("scope", "openid profile email"));
    params.add(new BasicNameValuePair("username", username));
    params.add(new BasicNameValuePair("password", password));

    String accessToken = "";
    try {
      httpPost.setEntity(new UrlEncodedFormEntity(params));
      HttpResponse response = httpClient.execute(httpPost);

      // Handle the response
      String responseBody = EntityUtils.toString(response.getEntity());

      accessToken = extractAccessToken(responseBody);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return accessToken;
  }

  private static String extractAccessToken(String jsonResponse) {
    // Use Jackson for JSON parsing
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      JsonNode rootNode = objectMapper.readTree(jsonResponse);
      return rootNode.path("access_token").asText();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public String createUser(SignupRequest signUpRequest) {
    // String url = keycloakBaseUrl + "/admin/realms/" + keycloakRealm + "/users";
    // HttpPost httpPost = new HttpPost(url);

    // String adminAccessToken = getAdminAccesToken();
    // httpPost.setHeader("Authorization", "Bearer " + adminAccessToken);

    // httpPost.setHeader("Content-Type", "application/json");

    try {
      UserRepresentation user = new UserRepresentation();
      user.setUsername(signUpRequest.getUsername());
      user.setEmail(signUpRequest.getEmail());
      user.setFirstName(signUpRequest.getFirstName());
      user.setLastName(signUpRequest.getLastName());
      user.setEnabled(true);
      user.setEmailVerified(true);

      CredentialRepresentation credential = new CredentialRepresentation();
      credential.setType("password");
      credential.setValue(signUpRequest.getPassword());
      credential.setTemporary(false);
      user.setCredentials(List.of(credential));
      user.setRequiredActions(Collections.emptyList());
      

      keycloakInstance.realm(keycloakRealm).users().create(user);

      return "User created successfully in Keycloak.";
    } catch (Exception e) {
      log.error("Error creating user in Keycloak: {}", e.getMessage());
      return "Error creating user in Keycloak: " + e.getMessage();
    }
  }

}
