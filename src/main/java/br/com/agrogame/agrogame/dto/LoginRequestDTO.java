package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

	@NotBlank(message = "Identificador (email, CPF ou CNPJ) é obrigatório")
	private String identifier;

	@NotBlank(message = "Senha é obrigatória")
	private String password;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
