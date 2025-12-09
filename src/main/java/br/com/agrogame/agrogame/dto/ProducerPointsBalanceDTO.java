package br.com.agrogame.agrogame.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Saldo de pontos do produtor")
public class ProducerPointsBalanceDTO {

	@Schema(description = "ID do usuário (producer)", example = "15")
	private Integer userId;

	@Schema(description = "Nome do usuário", example = "João da Silva")
	private String userName;

	@Schema(description = "Saldo atual de pontos", example = "1250")
	private Integer currentBalance;

	public ProducerPointsBalanceDTO() {
	}

	public ProducerPointsBalanceDTO(Integer userId, String userName, Integer currentBalance) {
		this.userId = userId;
		this.userName = userName;
		this.currentBalance = currentBalance;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Integer getCurrentBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(Integer currentBalance) {
		this.currentBalance = currentBalance;
	}
}