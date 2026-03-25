package br.com.agrogame.agrogame.dto;

import java.util.List;

public class BackofficeFarmDTO {
	private Integer id;
	private String name;
	private Integer ownerId;
	private Boolean active;
	private String statusLabel;
	private String city;
	private String state;
	private List<ProductionUnitDetailDTO> productionUnits;
	private List<String> cropTypes;
	private String cropTypesSummary; 

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

	public List<String> getCropTypes() {
		return cropTypes;
	}

	public void setCropTypes(List<String> cropTypes) {
		this.cropTypes = cropTypes;
	}

	public String getCropTypesSummary() {
		return cropTypesSummary;
	}

	public void setCropTypesSummary(String cropTypesSummary) {
		this.cropTypesSummary = cropTypesSummary;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getStatusLabel() {
		return statusLabel;
	}

	public void setStatusLabel(String statusLabel) {
		this.statusLabel = statusLabel;
	}

}
