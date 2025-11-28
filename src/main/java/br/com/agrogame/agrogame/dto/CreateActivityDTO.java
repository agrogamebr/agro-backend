package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateActivityDTO {

	@NotNull
	private Integer companyId;

	@NotBlank(message = "Descrição é obrigatória")
	private String description;

	@NotNull(message = "Pontos são obrigatórios")
	@Positive(message = "Pontos deve ser maior que zero")
	private Integer points;

	@NotNull(message = "Data inicial é obrigatória")
	private LocalDate validFrom;

	@NotNull(message = "Data final é obrigatória")
	private LocalDate validTo;

	@NotEmpty(message = "Deve ter pelo menos um tipo de cultura")
	private List<Integer> cropTypeIds; // Lista de IDs

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public List<Integer> getCropTypeIds() {
		return cropTypeIds;
	}

	public void setCropTypeIds(List<Integer> cropTypeIds) {
		this.cropTypeIds = cropTypeIds;
	}

}
