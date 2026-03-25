package br.com.agrogame.agrogame.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ação para associação: APPROVED ou REJECTED")
public class AssociateActionDTO {

	@Schema(description = "Ação: 'APPROVED' ou 'REJECTED'", allowableValues = { "APPROVED", "REJECTED" }, required = true)
	private String action;

	// Construtor vazio OBRIGATÓRIO pro Jackson
	public AssociateActionDTO() {
	}

	public AssociateActionDTO(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
