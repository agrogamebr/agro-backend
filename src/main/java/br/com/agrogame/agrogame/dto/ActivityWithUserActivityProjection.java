package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;

public interface ActivityWithUserActivityProjection {
	Integer getId();

	String getName();

	Integer getCompanyId();

	String getDescription();

	Integer getPoints();

	String getStatus();

	LocalDate getValidFrom();

	LocalDate getValidTo();

	Integer getUserActivityId();

	String getUserActivityStatus();

	Integer getUserActivityFarmId();
}
