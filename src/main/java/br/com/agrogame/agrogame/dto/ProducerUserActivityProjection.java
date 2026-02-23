package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;

public interface ProducerUserActivityProjection {
	Integer getUserActivityId();

	String getUserActivityStatus();

	Integer getUserActivityFarmId();

	Integer getProductionUnitId();

	Integer getActivityId();

	String getActivityName();

	String getDescription();

	Integer getPoints();

	LocalDate getValidFrom();

	LocalDate getValidTo();

	String getActivityStatus();

	String getThumbnailUrl();

	String getThumbnailGsutilUri();
}
