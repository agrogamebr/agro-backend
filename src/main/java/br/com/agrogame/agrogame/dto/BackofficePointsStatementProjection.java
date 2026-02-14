package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

public interface BackofficePointsStatementProjection {
	Long getId();

	LocalDateTime getDate();

	String getDescription();

	String getFarmName();

	String getProductionUnitName();

	String getOperationType();

	Integer getPoints();

	Integer getBalanceAfter();
}
