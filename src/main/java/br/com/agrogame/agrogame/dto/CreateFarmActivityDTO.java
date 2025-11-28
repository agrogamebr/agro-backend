package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CreateFarmActivityDTO {

    @NotNull(message = "Farm ID é obrigatório")
    private Integer farmId;

    private Integer cropTypeId; // opcional, nem toda atividade requer cultura específica

    @NotBlank(message = "Tipo de atividade é obrigatório")
    private String activityType; // plantio, colheita, aplicação de insumo, etc

    @NotBlank(message = "Nome da atividade é obrigatório")
    private String name;

    private String description;

    @NotNull(message = "Data agendada é obrigatória")
    private LocalDate scheduledDate;

    @NotNull(message = "Status ID é obrigatório")
    private Integer statusId;

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public Integer getCropTypeId() {
		return cropTypeId;
	}

	public void setCropTypeId(Integer cropTypeId) {
		this.cropTypeId = cropTypeId;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(LocalDate scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

}
