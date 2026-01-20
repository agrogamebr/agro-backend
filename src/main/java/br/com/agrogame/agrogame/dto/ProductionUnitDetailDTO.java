package br.com.agrogame.agrogame.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductionUnitDetailDTO {

	private Integer id;
	private Integer farmId;
	private String farmName;
	private Integer productionUnitTypeId;
	private String productionUnitTypeName;
	private String productionUnitTypeCode;
	private String name;
	private String description;
	private BigDecimal area;
	private Boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime deactivatedAt;
	private Integer cropTypeId;
    private String cropTypeName;
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

	public Integer getProductionUnitTypeId() {
		return productionUnitTypeId;
	}

	public void setProductionUnitTypeId(Integer productionUnitTypeId) {
		this.productionUnitTypeId = productionUnitTypeId;
	}

	public String getProductionUnitTypeName() {
		return productionUnitTypeName;
	}

	public void setProductionUnitTypeName(String productionUnitTypeName) {
		this.productionUnitTypeName = productionUnitTypeName;
	}

	public String getProductionUnitTypeCode() {
		return productionUnitTypeCode;
	}

	public void setProductionUnitTypeCode(String productionUnitTypeCode) {
		this.productionUnitTypeCode = productionUnitTypeCode;
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getDeactivatedAt() {
		return deactivatedAt;
	}

	public void setDeactivatedAt(LocalDateTime deactivatedAt) {
		this.deactivatedAt = deactivatedAt;
	}

	public Integer getCropTypeId() {
		return cropTypeId;
	}

	public void setCropTypeId(Integer cropTypeId) {
		this.cropTypeId = cropTypeId;
	}

	public String getCropTypeName() {
		return cropTypeName;
	}

	public void setCropTypeName(String cropTypeName) {
		this.cropTypeName = cropTypeName;
	}

	public String getThumbnailGsUrl() {
		return thumbnailGsUrl;
	}

	public void setThumbnailGsUrl(String thumbnailGsUrl) {
		this.thumbnailGsUrl = thumbnailGsUrl;
	}

}
