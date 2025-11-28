package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.*;

public class ActivityRewardItemDTO {

	@NotNull
	private Long rewardId;

	@NotNull
	@Positive
	private Integer pointsGain;

	public Long getRewardId() {
		return rewardId;
	}

	public void setRewardId(Long rewardId) {
		this.rewardId = rewardId;
	}

	public Integer getPointsGain() {
		return pointsGain;
	}

	public void setPointsGain(Integer pointsGain) {
		this.pointsGain = pointsGain;
	}

}
