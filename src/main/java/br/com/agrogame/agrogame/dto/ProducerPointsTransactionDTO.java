package br.com.agrogame.agrogame.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Transação de pontos do produtor")
public class ProducerPointsTransactionDTO {

	@Schema(description = "ID da transação de pontos", example = "1001")
	private Integer id;

	@Schema(description = "Tipo da transação (earn, spend, adjust)", example = "earn")
	private String transactionType;

	@Schema(description = "Fonte da transação (ex: activity_approval)", example = "activity_approval")
	private String sourceType;

	@Schema(description = "Descrição da atividade (se houver)", example = "Plantio de Soja - Safra 2025")
	private String activityDescription;

	@Schema(description = "Nome da recompensa (se houver)", example = "Brinde Agrogame")
	private String rewardName;

	@Schema(description = "Quantidade de pontos na transação", example = "150")
	private Integer points;

	@Schema(description = "Saldo após a transação", example = "1250")
	private Integer balanceAfter;

	@Schema(description = "Data/hora da transação", example = "2025-12-05T20:15:30")
	private LocalDateTime createdAt;

	public ProducerPointsTransactionDTO() {
	}

	public ProducerPointsTransactionDTO(Integer id, String transactionType, String sourceType,
			String activityDescription, String rewardName, Integer points, Integer balanceAfter,
			LocalDateTime createdAt) {
		this.id = id;
		this.transactionType = transactionType;
		this.sourceType = sourceType;
		this.activityDescription = activityDescription;
		this.rewardName = rewardName;
		this.points = points;
		this.balanceAfter = balanceAfter;
		this.createdAt = createdAt;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getActivityDescription() {
		return activityDescription;
	}

	public void setActivityDescription(String activityDescription) {
		this.activityDescription = activityDescription;
	}

	public String getRewardName() {
		return rewardName;
	}

	public void setRewardName(String rewardName) {
		this.rewardName = rewardName;
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
}
