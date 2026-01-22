package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WorkerCreateDTO {

	@NotBlank
	@Size(max = 150)
	private String fullname;

	@NotBlank
	@Email
	@Size(max = 150)
	private String email;

	/**
	 * No cadastro é obrigatório. Na edição pode ser null/vazio (não altera).
	 */
	private String password;

	private String phone;

	// Endereço opcional
	private String address;
	private String number;
	private String city;
	private String state;
	private String zipcode;

	/**
	 * Fazenda onde o worker vai trabalhar (contexto do cadastro). Precisa pertencer
	 * ao produtor logado.
	 */
	@NotNull
	private Integer farmId;

	/**
	 * Lista de unidades produtivas dessa fazenda. Pode ser vazia (worker sem
	 * unidade inicialmente).
	 */
	private Integer productionUnitId;

	// getters e setters

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public Integer getFarmId() {
		return farmId;
	}

	public void setFarmId(Integer farmId) {
		this.farmId = farmId;
	}

	public Integer getProductionUnitId() {
		return productionUnitId;
	}

	public void setProductionUnitId(Integer productionUnitId) {
		this.productionUnitId = productionUnitId;
	}

}
