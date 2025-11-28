package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;

public class FarmActivityResponseDTO {

	private Integer id;
	private Integer farmId;
	private String farmName;
	private Integer cropTypeId;
	private String cropTypeName;
	private String activityType;
	private String name;
	private String description;
	private LocalDate scheduledDate;
	private String status;
	private String createdAt;
	private String message;

	public FarmActivityResponseDTO(Integer id, Integer farmId, String farmName, Integer cropTypeId, String cropTypeName,
			String activityType, String name, String description, LocalDate scheduledDate, String status,
			String createdAt, String message) {
		this.id = id;
		this.farmId = farmId;
		this.farmName = farmName;
		this.cropTypeId = cropTypeId;
		this.cropTypeName = cropTypeName;
		this.activityType = activityType;
		this.name = name;
		this.description = description;
		this.scheduledDate = scheduledDate;
		this.status = status;
		this.createdAt = createdAt;
		this.message = message;
	}

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

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
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

	public LocalDate getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(LocalDate scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
