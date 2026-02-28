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

	private Integer idActivity;

	@Schema(description = "ID da atividade do usuário", example = "55")
	private Integer userActivityId;

	@Schema(description = "Nome da fazenda onde a atividade foi realizada", example = "Fazenda Santa Fé")
	private String farmName;

	@Schema(description = "ID da fazenda", example = "10")
	private Integer farmId;

	@Schema(description = "Nome da unidade produtiva onde a atividade foi realizada", example = "Talhão 1")
	private String productionUnitName;

	@Schema(description = "ID da unidade produtiva", example = "5")
	private Integer productionUnitId;

	public ProducerPointsTransactionDTO(Integer id, String transactionType, String sourceType,
			String activityDescription, String rewardName, Integer points, Integer balanceAfter,
			LocalDateTime createdAt, Integer idActivity, Integer userActivityId, String farmName, Integer farmId,
			String productionUnitName, Integer productionUnitId) {

		this.id = id;
		this.transactionType = transactionType;
		this.sourceType = sourceType;
		this.activityDescription = activityDescription;
		this.rewardName = rewardName;
		this.points = points;
		this.balanceAfter = balanceAfter;
		this.createdAt = createdAt;
		this.idActivity = idActivity;
		this.userActivityId = userActivityId;
		this.farmName = farmName;
		this.farmId = farmId;
		this.productionUnitName = productionUnitName;
		this.productionUnitId = productionUnitId;
	}

	public ProducerPointsTransactionDTO() {
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

	public Integer getIdActivity() {
		return idActivity;
	}

	public void setIdActivity(Integer idActivity) {
		this.idActivity = idActivity;
	}

	public Integer getUserActivityId() {
		return userActivityId;
	}

	public void setUserActivityId(Integer userActivityId) {
		this.userActivityId = userActivityId;
	}

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public String getProductionUnitName() {
		return productionUnitName;
	}

	public void setProductionUnitName(String productionUnitName) {
		this.productionUnitName = productionUnitName;
	}

	public Integer getProductionUnitId() {
		return productionUnitId;
	}

	public void setProductionUnitId(Integer productionUnitId) {
		this.productionUnitId = productionUnitId;
	}
}
