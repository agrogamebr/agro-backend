package br.com.agrogame.agrogame.dto;

import java.math.BigDecimal;

public class ProductionUnitSelectDTO {
	private Integer id;
    private Integer farmId;
    private String farmName;
    private String name;
    private String description;
    private BigDecimal area;
    private String productionUnitTypeCode;
    private String productionUnitTypeName;
	private String thumbnailGsUrl;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
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

	public String getProductionUnitTypeCode() {
		return productionUnitTypeCode;
	}

	public void setProductionUnitTypeCode(String productionUnitTypeCode) {
		this.productionUnitTypeCode = productionUnitTypeCode;
	}

	public String getProductionUnitTypeName() {
		return productionUnitTypeName;
	}

	public void setProductionUnitTypeName(String productionUnitTypeName) {
		this.productionUnitTypeName = productionUnitTypeName;
	}

	public String getThumbnailGsUrl() {
		return thumbnailGsUrl;
	}

	public void setThumbnailGsUrl(String thumbnailGsUrl) {
		this.thumbnailGsUrl = thumbnailGsUrl;
	}
}
