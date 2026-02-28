package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateActivityDTO {

	private Integer companyId;

	@NotBlank(message = "Descrição é obrigatória")
	private String description;

	@NotBlank(message = "Nome é obrigatório")
	private String name;

	@NotNull(message = "Pontos são obrigatórios")
	@Positive(message = "Pontos deve ser maior que zero")
	private Integer points;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // Garante que o String "2025-12-17" vire LocalDate
	private LocalDate validFrom;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate validTo;

	private List<Integer> cropTypeIds;

	private List<Integer> farmIds;
	private List<Integer> productionUnitIds;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getFarmIds() {
		return farmIds;
	}

	public void setFarmIds(List<Integer> farmIds) {
		this.farmIds = farmIds;
	}

	public List<Integer> getProductionUnitIds() {
		return productionUnitIds;
	}

	public void setProductionUnitIds(List<Integer> productionUnitIds) {
		this.productionUnitIds = productionUnitIds;
	}

}
