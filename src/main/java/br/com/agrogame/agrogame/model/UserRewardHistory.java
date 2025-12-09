package br.com.agrogame.agrogame.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reward_history")
public class UserRewardHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_reward_id", nullable = false)
	private UserReward userReward;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "old_status_id")
	private UserRewardStatus oldStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "new_status_id", nullable = false)
	private UserRewardStatus newStatus;

	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	private User changedBy;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	public UserRewardHistory() {
	}

	public UserRewardHistory(UserReward userReward, UserRewardStatus newStatus, User createdBy, String notes) {
		this.userReward = userReward;
		this.newStatus = newStatus;
		this.createdBy = createdBy;
		this.changedBy = createdBy;
		this.notes = notes;
		this.changedAt = LocalDateTime.now();
		this.createdAt = LocalDateTime.now();
	}

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (changedAt == null) {
			changedAt = LocalDateTime.now();
		}
	}

	// Getters e setters

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserReward getUserReward() {
		return userReward;
	}

	public void setUserReward(UserReward userReward) {
		this.userReward = userReward;
	}

	public UserRewardStatus getOldStatus() {
		return oldStatus;
	}

	public void setOldStatus(UserRewardStatus oldStatus) {
		this.oldStatus = oldStatus;
	}

	public UserRewardStatus getNewStatus() {
		return newStatus;
	}

	// equivalente ao setUserRewardStatus(grantedStatus)
	public void setNewStatus(UserRewardStatus newStatus) {
		this.newStatus = newStatus;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}

	public void setChangedAt(LocalDateTime changedAt) {
		this.changedAt = changedAt;
	}

	public User getChangedBy() {
		return changedBy;
	}

	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public String getNotes() {
		return notes;
	}

	// equivalente ao setDescription(...)
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
}
