package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class BackofficeProducerSummaryDTO {

	private Integer userId;
	private String fullName;
	private String cpf;
	private Integer statusId;
	private String statusName;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime approvedAt;
	private String approvedByName;

	public BackofficeProducerSummaryDTO(Integer userId, String fullName, String cpf, Integer statusId,
			String statusName, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime approvedAt,
			String approvedByName) {
		this.userId = userId;
		this.fullName = fullName;
		this.cpf = cpf;
		this.statusId = statusId;
		this.statusName = statusName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.approvedAt = approvedAt;
		this.approvedByName = approvedByName;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public Integer getStatusId() {
		return statusId;
	}

	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getApprovedByName() {
		return approvedByName;
	}

	public void setApprovedByName(String approvedByName) {
		this.approvedByName = approvedByName;
	}

	public LocalDateTime getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(LocalDateTime approvedAt) {
		this.approvedAt = approvedAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
