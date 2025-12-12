package br.com.agrogame.agrogame.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FarmCreateUpdateDTO {

	@NotBlank
	@Size(max = 100)
	private String name;

	@NotNull
	@DecimalMin("0.1")
	private BigDecimal area;

	@NotEmpty
	private List<Integer> cropTypeIds;

	@NotBlank
	@Size(min = 2, max = 2) // UF
	private String state;

	@NotBlank
	private String city;

	@NotBlank
	private String zipcode;

	private String description;
	private BigDecimal latitude;
	private BigDecimal longitude;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}

	public List<Integer> getCropTypeIds() {
		return cropTypeIds;
	}

	public void setCropTypeIds(List<Integer> cropTypeIds) {
		this.cropTypeIds = cropTypeIds;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

}
