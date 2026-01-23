package br.com.agrogame.agrogame.dto;

public class WorkerUpdateDTO {

	private String fullname;
	private String phone;
	private String address;
	private String number;
	private String city;
	private String state;
	private String zipcode;

	private Integer productionUnitId;

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
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

	public Integer getProductionUnitId() {
		return productionUnitId;
	}

	public void setProductionUnitId(Integer productionUnitId) {
		this.productionUnitId = productionUnitId;
	}

}
