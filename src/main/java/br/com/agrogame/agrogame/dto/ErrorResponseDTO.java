package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class ErrorResponseDTO {
    
    private String error;
    private String message;
    private Integer status;
    private LocalDateTime timestamp;

    public ErrorResponseDTO(String error, String message, Integer status) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

	public ErrorResponseDTO(String error, String message, Integer status, LocalDateTime timestamp) {
		super();
		this.error = error;
		this.message = message;
		this.status = status;
		this.timestamp = timestamp;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
    
    
}
