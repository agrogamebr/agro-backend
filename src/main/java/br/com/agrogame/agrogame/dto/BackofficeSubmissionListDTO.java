package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BackofficeSubmissionListDTO {
	private Integer userActivityId;
	private Integer activityId;
	private String activityName;
	private Integer farmId;
	private String farmName;
	private Integer producerId;
	private String producerName;
	private String producerDocument;
	private LocalDateTime submittedAt;
	private Integer filesCount;
	private String status;
	private List<BackofficeFileInfoDTO> files;
	private Integer points;
	private String description;

	public Integer getUserActivityId() {
		return userActivityId;
	}

	public void setUserActivityId(Integer userActivityId) {
		this.userActivityId = userActivityId;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
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

	public Integer getProducerId() {
		return producerId;
	}

	public void setProducerId(Integer producerId) {
		this.producerId = producerId;
	}

	public String getProducerName() {
		return producerName;
	}

	public void setProducerName(String producerName) {
		this.producerName = producerName;
	}

	public String getProducerDocument() {
		return producerDocument;
	}

	public void setProducerDocument(String producerDocument) {
		this.producerDocument = producerDocument;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public Integer getFilesCount() {
		return filesCount;
	}

	public void setFilesCount(Integer filesCount) {
		this.filesCount = filesCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<BackofficeFileInfoDTO> getFiles() {
		return files;
	}

	public void setFiles(List<BackofficeFileInfoDTO> files) {
		this.files = files;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}