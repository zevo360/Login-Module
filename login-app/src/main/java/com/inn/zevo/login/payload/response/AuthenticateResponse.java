package com.inn.zevo.login.payload.response;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthenticateResponse {

	private String token;
	@NotBlank
	@Size(min = 3, max = 20)
	private String username;
	@NotBlank
	@Size(max = 50)
	@Email
	private String email;
	private List<String> role;
	private String Status;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<String> getRole() {
		return role;
	}
	public void setRole(List<String> role) {
		this.role = role;
	}
	public AuthenticateResponse(@NotBlank @Size(min = 3, max = 20) String username,
			@NotBlank @Size(max = 50) String status) {
		super();
		this.username = username;
		Status = status;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public AuthenticateResponse() {
		
	}
}
