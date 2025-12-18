package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;
import java.util.List;

public class ProducerActivityDTO {

	private Integer activityId;
	private String description;
	private Integer points;
	private LocalDate validFrom;
	private LocalDate validTo;
	private List<String> cropTypes; // culturas vinculadas
	private String status;
	private String companyName;
	private String name;
	private Integer userActivityId;
	private String userActivityStatus;
	private Integer userActivityFarmId;
    private String thumbnailUrl;
    private String thumbnailGsutilUri;

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

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
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

	public List<String> getCropTypes() {
		return cropTypes;
	}

	public void setCropTypes(List<String> cropTypes) {
		this.cropTypes = cropTypes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Integer getUserActivityFarmId() {
		return userActivityFarmId;
	}

	public void setUserActivityFarmId(Integer userActivityFarmId) {
		this.userActivityFarmId = userActivityFarmId;
	}
}
