package br.com.agrogame.agrogame.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_points_transactions")
public class UserPointsTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_type_id", nullable = false)
	private UserPointTransactionType transactionType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "source_type_id", nullable = false)
	private UserPointTransactionSourceType sourceType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "activity_id")
	private Activity activity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reward_id")
	private Reward reward;

	@Column(name = "points", nullable = false)
	private Integer points;

	@Column(name = "balance_after", nullable = false)
	private Integer balanceAfter;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	public UserPointsTransaction() {
	}

	public UserPointsTransaction(User user, UserPointTransactionType transactionType,
			UserPointTransactionSourceType sourceType, Activity activity, Integer points, Integer balanceAfter,
			Integer createdById) {
		this.user = user;
		this.transactionType = transactionType;
		this.sourceType = sourceType;
		this.activity = activity;
		this.points = points;
		this.balanceAfter = balanceAfter;
	}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public UserPointTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(UserPointTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public UserPointTransactionSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(UserPointTransactionSourceType sourceType) {
		this.sourceType = sourceType;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Reward getReward() {
		return reward;
	}

	public void setReward(Reward reward) {
		this.reward = reward;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public Integer getBalanceAfter() {
		return balanceAfter;
	}

	public void setBalanceAfter(Integer balanceAfter) {
		this.balanceAfter = balanceAfter;
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
