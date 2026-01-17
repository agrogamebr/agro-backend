package br.com.agrogame.agrogame.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductionUnitCreateUpdateDTO {

	@NotNull(message = "O ID da fazenda é obrigatório")
	private Integer farmId;

	@NotNull(message = "O tipo de unidade produtiva é obrigatório")
	private Integer productionUnitTypeId;

	@NotBlank(message = "O nome é obrigatório")
	@Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
	private String name;
	
	@NotNull(message = "O tipo de cultura (cropType) é obrigatório")
    private Integer cropTypeId;

	@NotBlank(message = "A descrição é obrigatória")
	@Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
	private String description;

	@NotNull(message = "A área é obrigatória")
	@DecimalMin(value = "0.01", message = "Área deve ser maior que zero")
	private BigDecimal area;

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public Integer getProductionUnitTypeId() {
		return productionUnitTypeId;
	}

	public void setProductionUnitTypeId(Integer productionUnitTypeId) {
		this.productionUnitTypeId = productionUnitTypeId;
	}

	public Integer getCropTypeId() {
		return cropTypeId;
	}

	public void setCropTypeId(Integer cropTypeId) {
		this.cropTypeId = cropTypeId;
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

	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}

}
