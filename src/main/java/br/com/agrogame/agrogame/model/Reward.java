package br.com.agrogame.agrogame.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rewards")
public class Reward {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private Integer pointsGain;
	private String description;
	private Integer stock;

	@Column(name = "valid_from")
	private LocalDate validFrom;

	@Column(name = "valid_to")
	private LocalDate validTo;

	private Integer pointsCost;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reward_status_id")
	private RewardStatus rewardStatus;

	private LocalDateTime createdAt;
	private Integer createdBy;
	private LocalDateTime updatedAt;
	private Integer updatedBy;
	private LocalDateTime deactivatedAt;
	private Integer deactivatedBy;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPointsGain() {
		return pointsGain;
	}

	public void setPointsGain(Integer pointsGain) {
		this.pointsGain = pointsGain;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public Integer getPointsCost() {
		return pointsCost;
	}

	public void setPointsCost(Integer pointsCost) {
		this.pointsCost = pointsCost;
	}

	public RewardStatus getRewardStatus() {
		return rewardStatus;
	}

	public void setRewardStatus(RewardStatus rewardStatus) {
		this.rewardStatus = rewardStatus;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getDeactivatedAt() {
		return deactivatedAt;
	}

	public void setDeactivatedAt(LocalDateTime deactivatedAt) {
		this.deactivatedAt = deactivatedAt;
	}

	public Integer getDeactivatedBy() {
		return deactivatedBy;
	}

	public void setDeactivatedBy(Integer deactivatedBy) {
		this.deactivatedBy = deactivatedBy;
	}

}
