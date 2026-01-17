package biz.anhld.anhphuongshop.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Set;

@Getter
public class SignupRequest {
  @NotBlank(message = "Invalid Username: Empty username")
	@Size(min = 3, max = 30, message = "Invalid username: Must be of 3 - 30 characters")
	private String username;

	@NotBlank(message = "Invalid Email: Empty email")
	@Size(max = 50, message = "Invalid email: Must be max 50 characters")
	@Email(message = "Invalid email")
	private String email;

	private Set<String> role;

	@NotBlank(message = "Invalid Password: Empty password")
	@Size(min=6, max = 40, message = "Invalid password: Must be of 6 - 40 characters")
	private String password;

  @NotBlank(message = "Invalid First Name: Empty first name")
  private String firstName;

  @NotBlank(message = "Invalid Last Name: Empty last name")
  private String lastName;
}
