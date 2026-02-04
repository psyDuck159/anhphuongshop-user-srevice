package biz.anhld.anhphuongshop.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse extends MessageResponse {

	private Long id;
	private String username;
	private String email;

	private String type = "Bearer";

	private String token;
	private Integer expiresIn;
	private Integer refreshExpiresIn;
	private String refreshToken;
	private String idToken;

	public JwtResponse() {}

	public JwtResponse(String message) {
		super(message);
	}

}
