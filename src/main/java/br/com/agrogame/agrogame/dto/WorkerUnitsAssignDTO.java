package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.NotNull;

public class WorkerUnitsAssignDTO {

	@NotNull
	private Integer productionUnitId;

	public Integer getProductionUnitId() {
		return productionUnitId;
	}

	public void setProductionUnitId(Integer productionUnitId) {
		this.productionUnitId = productionUnitId;
	}
}
