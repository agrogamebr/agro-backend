package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.NotNull;

public class SubmitActivityDTO {

	@NotNull(message = "ID da atividade é obrigatório")
	private Integer activityId;

	@NotNull(message = "ID da fazenda é obrigatório")
	private Integer farmId;

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
}
