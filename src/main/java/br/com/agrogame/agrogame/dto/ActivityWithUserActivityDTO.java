package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;

public class ActivityWithUserActivityDTO {
	private Integer activityId;
	private String name;
	private String description;
	private Integer points;
	private LocalDate validFrom;
	private LocalDate validTo;
	private String status;
	private String companyName;
	private Integer userActivityId;
	private String userActivityStatus;
	private Integer userActivityFarmId;

	public ActivityWithUserActivityDTO(ActivityWithUserActivityProjection proj) {
		this.activityId = proj.getId();

		this.name = proj.getName();
		this.description = proj.getDescription();
		this.points = proj.getPoints();
		this.validFrom = proj.getValidFrom();
		this.validTo = proj.getValidTo();
		this.status = proj.getStatus();
		this.userActivityId = proj.getUserActivityId();
		this.userActivityStatus = proj.getUserActivityStatus();
		this.userActivityFarmId = proj.getUserActivityFarmId();
	}

	public Integer getUserActivityFarmId() {
		return userActivityFarmId;
	}

	public void setUserActivityFarmId(Integer userActivityFarmId) {
		this.userActivityFarmId = userActivityFarmId;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
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

}
