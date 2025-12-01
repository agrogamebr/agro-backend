package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class UploadFileDTO {

	@NotNull(message = "ID da atividade é obrigatório")
	private Integer activityId;

	@NotNull(message = "ID da fazenda é obrigatório")
	private Integer farmId;

	@NotEmpty(message = "Descrição não pode estar vazia")
	private String description;

	// getters e setters
	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
