package br.com.agrogame.agrogame.dto;

import br.com.agrogame.agrogame.model.UserPointsTransaction;

public interface TransactionWithUserActivity {
	UserPointsTransaction getTransaction();

	Integer getUserActivityId();

	String getFarmName();
	
	Integer getFarmId();
}
