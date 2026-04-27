package br.com.agrogame.agrogame.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "worker_farm_assignments")
public class WorkerFarmAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "worker_id", nullable = false)
	private Long workerId;

	@Column(name = "farm_id", nullable = false)
	private Long farmId;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by")
	private Long updatedBy;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		if (this.isActive == null) {
			this.isActive = true;
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public WorkerFarmAssignment() {
	}

	public WorkerFarmAssignment(Long workerId, Long farmId, Boolean isActive, Long updatedBy) {
		this.workerId = workerId;
		this.farmId = farmId;
		this.isActive = isActive;
		this.updatedBy = updatedBy;
	}

	public Long getId() {
		return id;
	}

	public Long getWorkerId() {
		return workerId;
	}

	public void setWorkerId(Long workerId) {
		this.workerId = workerId;
	}

	public Long getFarmId() {
		return farmId;
	}

	public void setFarmId(Long farmId) {
		this.farmId = farmId;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean active) {
		isActive = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}
}