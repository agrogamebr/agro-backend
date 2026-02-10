package br.com.agrogame.agrogame.dto;

public interface BackofficeActivityProjection {
	Integer getId();

	String getName();

	Integer getCompanyId();

	String getDescription();

	Integer getPoints();

	String getStatus();

	java.time.LocalDate getValidFrom();

	java.time.LocalDate getValidTo();

	Integer getUserActivityId();

	String getUserActivityStatus();

	Integer getUserId();

	Integer getFarmId();

	Integer getProductionUnitId();

	String getThumbnailUrl();

	String getThumbnailGsutilUri();
}
