package biz.anhld.anhphuongshop.userservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import biz.anhld.anhphuongshop.userservice.dto.JwtResponse;
import biz.anhld.anhphuongshop.userservice.dto.keycloak.TokenResponse;

@Mapper(componentModel = "spring")
public interface JwtMapper {
  
  @Mapping(source = "accessToken", target = "token")
  @Mapping(source = "tokenType", target = "type")
  JwtResponse toJwtResponse(TokenResponse response);

}
