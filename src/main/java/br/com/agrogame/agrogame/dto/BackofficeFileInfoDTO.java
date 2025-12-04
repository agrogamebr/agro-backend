package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class BackofficeFileInfoDTO {
	private Integer fileId;
	private String fileName;
	private String fileUrl;
	private String gsutilUri;
	private LocalDateTime uploadedAt;

	public BackofficeFileInfoDTO(Integer fileId, String fileName, String fileUrl, String gsutilUri,
			LocalDateTime uploadedAt) {
		this.fileId = fileId;
		this.fileName = fileName;
		this.fileUrl = fileUrl;
		this.gsutilUri = gsutilUri;
		this.uploadedAt = uploadedAt;
	}

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getGsutilUri() {
		return gsutilUri;
	}

	public void setGsutilUri(String gsutilUri) {
		this.gsutilUri = gsutilUri;
	}

	public LocalDateTime getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(LocalDateTime uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

}