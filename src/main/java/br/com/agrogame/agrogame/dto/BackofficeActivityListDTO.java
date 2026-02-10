package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;

public class BackofficeActivityListDTO {

	private Integer id;
	private String name;
	private String description;
	private Integer points;

	private String statusCode; // status da Activity (draft/send/cancelled...)
	private LocalDate validFrom;
	private LocalDate validTo;

	private String thumbnailUrl;
	private String thumbnailGsutilUri;

	// UserActivity (execução)
	private Integer userActivityId;
	private String userActivityStatus; // pending/submitted/approved/rejected...

	private Integer producerId;
	private Integer farmId;
	private Integer productionUnitId;

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

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
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

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getThumbnailGsutilUri() {
		return thumbnailGsutilUri;
	}

	public void setThumbnailGsutilUri(String thumbnailGsutilUri) {
		this.thumbnailGsutilUri = thumbnailGsutilUri;
	}

	public Integer getUserActivityId() {
		return userActivityId;
	}

	public void setUserActivityId(Integer userActivityId) {
		this.userActivityId = userActivityId;
	}

	public String getUserActivityStatus() {
		return userActivityStatus;
	}

	public void setUserActivityStatus(String userActivityStatus) {
		this.userActivityStatus = userActivityStatus;
	}

	public Integer getProducerId() {
		return producerId;
	}

	public void setProducerId(Integer producerId) {
		this.producerId = producerId;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public Integer getProductionUnitId() {
		return productionUnitId;
	}

	public void setProductionUnitId(Integer productionUnitId) {
		this.productionUnitId = productionUnitId;
	}

}
