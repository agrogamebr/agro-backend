package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class SubmitActivityResponseDTO {

	private Integer userActivityId;
	private Integer activityId;
	private Integer farmId;
	private String status;
	private Integer filesCount;
	private LocalDateTime submittedAt;
	private String message;

	public SubmitActivityResponseDTO(Integer userActivityId, Integer activityId, Integer farmId, String status,
			Integer filesCount, LocalDateTime submittedAt, String message) {
		this.userActivityId = userActivityId;
		this.activityId = activityId;
		this.farmId = farmId;
		this.status = status;
		this.filesCount = filesCount;
		this.submittedAt = submittedAt;
		this.message = message;
	}

	// getters
	public Integer getUserActivityId() {
		return userActivityId;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public String getStatus() {
		return status;
	}

	public Integer getFilesCount() {
		return filesCount;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public String getMessage() {
		return message;
	}

	public void setUserActivityId(Integer userActivityId) {
		this.userActivityId = userActivityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setFilesCount(Integer filesCount) {
		this.filesCount = filesCount;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
