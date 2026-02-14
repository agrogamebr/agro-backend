package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public class BackofficePointsStatementDTO {
	
	private Long id;
	private LocalDateTime date;
	private String description;
	private String farmName;
	private String productionUnitName;
	private String operationType;
	private Integer points;
	private Integer balanceAfter;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}

	public String getProductionUnitName() {
		return productionUnitName;
	}

	public void setProductionUnitName(String productionUnitName) {
		this.productionUnitName = productionUnitName;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
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

}
