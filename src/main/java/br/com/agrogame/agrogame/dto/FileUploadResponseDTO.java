package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class FileUploadResponseDTO {

	private Integer submissionId;
	private String fileName;
	private String fileUrl;
	private String description;
	private LocalDateTime uploadedAt;

	public FileUploadResponseDTO(Integer submissionId, String fileName, String fileUrl, String description,
			LocalDateTime uploadedAt) {
		this.submissionId = submissionId;
		this.fileName = fileName;
		this.fileUrl = fileUrl;
		this.description = description;
		this.uploadedAt = uploadedAt;
	}

	public void setSubmissionId(Integer submissionId) {
		this.submissionId = submissionId;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public Integer getSubmissionId() {
		return submissionId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public String getDescription() {
		return description;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}
}
