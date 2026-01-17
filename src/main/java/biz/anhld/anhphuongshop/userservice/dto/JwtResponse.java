package biz.anhld.anhphuongshop.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JwtResponse extends MessageResponse {

	private Long id;
	private String username;
	private String email;

	private String type = "Bearer";
	private String token;

	public JwtResponse(String message) {
		super(message);
	}

	public JwtResponse(String accessToken, Long id, String username, String email) {
		super();
		this.token = accessToken;
		this.id = id;
		this.username = username;
		this.email = email;
	}
  
}
