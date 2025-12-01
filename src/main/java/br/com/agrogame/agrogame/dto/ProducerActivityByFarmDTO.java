package br.com.agrogame.agrogame.dto;

import java.util.List;

public class ProducerActivityByFarmDTO {

	private Integer farmId;
	private String farmName;
	private String cropTypeName; // ou lista se quiser mostrar todas as culturas da farm
	private List<ProducerActivityDTO> activities;

	public ProducerActivityByFarmDTO(Integer farmId, String farmName, String cropTypeName) {
		this.farmId = farmId;
		this.farmName = farmName;
		this.cropTypeName = cropTypeName;
		this.activities = new java.util.ArrayList<>();
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

	public String getCropTypeName() {
		return cropTypeName;
	}

	public void setCropTypeName(String cropTypeName) {
		this.cropTypeName = cropTypeName;
	}

	public List<ProducerActivityDTO> getActivities() {
		return activities;
	}

	public void setActivities(List<ProducerActivityDTO> activities) {
		this.activities = activities;
	}
}
