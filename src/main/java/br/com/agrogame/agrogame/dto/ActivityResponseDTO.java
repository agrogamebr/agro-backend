package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;

public class ActivityResponseDTO {
	private Integer id;
	private String name;
	private String description;
	private Integer points;
	private String status;
	private LocalDate validFrom;
	private LocalDate validTo;
	private Integer cropTypesCount;
	private Integer rewardId;

	private String thumbnailUrl;
	private String thumbnailGsutilUri;

	private String message;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Integer getCropTypesCount() {
		return cropTypesCount;
	}

	public void setCropTypesCount(Integer cropTypesCount) {
		this.cropTypesCount = cropTypesCount;
	}

	public Integer getRewardId() {
		return rewardId;
	}

	public void setRewardId(Integer rewardId) {
		this.rewardId = rewardId;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
