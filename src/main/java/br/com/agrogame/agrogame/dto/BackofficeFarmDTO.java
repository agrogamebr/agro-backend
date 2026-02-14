package br.com.agrogame.agrogame.dto;

import java.util.List;

public class BackofficeFarmDTO {
	private Integer id;
	private String name;
	private Integer ownerId;
	private List<ProductionUnitDetailDTO> productionUnits;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

	public List<ProductionUnitDetailDTO> getProductionUnits() {
		return productionUnits;
	}

	public void setProductionUnits(List<ProductionUnitDetailDTO> productionUnits) {
		this.productionUnits = productionUnits;
	}

}
