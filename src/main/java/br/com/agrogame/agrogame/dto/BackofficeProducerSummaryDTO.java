package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class BackofficeProducerSummaryDTO {

	private Integer userId;
	private String fullName;
	private String cpf;
	private Integer statusId;
	private String statusName;
	private LocalDateTime createdAt;

	public BackofficeProducerSummaryDTO(Integer userId, String fullName, String cpf, Integer statusId,
			String statusName, LocalDateTime createdAt) {
		this.userId = userId;
		this.fullName = fullName;
		this.cpf = cpf;
		this.statusId = statusId;
		this.statusName = statusName;
		this.createdAt = createdAt;
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

}
