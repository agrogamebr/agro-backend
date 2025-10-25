package br.com.agrogame.agrogame.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private Integer userId;
    private String email;
    private String name;
    
    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, Integer userId, String email, String name) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    
    
}
