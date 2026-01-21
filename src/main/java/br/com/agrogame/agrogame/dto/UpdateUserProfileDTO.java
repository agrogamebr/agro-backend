package br.com.agrogame.agrogame.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserProfileDTO {

	@NotBlank(message = "Nome completo é obrigatório")
	private String fullName;

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "Formato de e-mail inválido")
	private String email;

	@NotBlank(message = "Telefone é obrigatório")
	private String phone;

	@NotBlank(message = "Endereço é obrigatório")
	private String address;

	private String number;

	@NotBlank(message = "Cidade é obrigatória")
	private String city;

	@NotBlank(message = "Estado é obrigatório")
	private String state;

	@NotBlank(message = "CEP é obrigatório")
	private String zipcode;

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
	
}
