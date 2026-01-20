package br.com.agrogame.agrogame.dto;

import java.math.BigDecimal;
import java.util.List;

public class FarmDetailDTO {

	private Integer id;
	private String name;
	private BigDecimal area;
	private String state;
	private String city;
	private String zipcode;
	private String description;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private boolean active;
	private List<Integer> cropTypeIds;
    private List<ProductionUnitDetailDTO> productionUnits;
	private String thumbnailGsUrl;

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

	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<Integer> getCropTypeIds() {
		return cropTypeIds;
	}

	public void setCropTypeIds(List<Integer> cropTypeIds) {
		this.cropTypeIds = cropTypeIds;
	}

	public String getThumbnailGsUrl() {
		return thumbnailGsUrl;
	}

	public void setThumbnailGsUrl(String thumbnailGsUrl) {
		this.thumbnailGsUrl = thumbnailGsUrl;
	}

	public List<ProductionUnitDetailDTO> getProductionUnits() {
		return productionUnits;
	}

	public void setProductionUnits(List<ProductionUnitDetailDTO> productionUnits) {
		this.productionUnits = productionUnits;
	}

}
