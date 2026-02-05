package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;

public class CreateActivityMultipartDTO {
	private String description;
	private String name;
	private Integer points;
	private LocalDate validFrom;
	private LocalDate validTo;
	@NotEmpty
	private List<Integer> cropTypeIds;
	private MultipartFile thumbnail;
	
	private List<Integer> farmIds;
    private List<Integer> productionUnitIds;
    
    private Boolean sendNow = false; 

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}

	public List<Integer> getCropTypeIds() {
		return cropTypeIds;
	}

	public void setCropTypeIds(List<Integer> cropTypeIds) {
		this.cropTypeIds = cropTypeIds;
	}

	public MultipartFile getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(MultipartFile thumbnail) {
		this.thumbnail = thumbnail;
	}

	public List<Integer> getFarmIds() {
		return farmIds;
	}

	public void setFarmIds(List<Integer> farmIds) {
		this.farmIds = farmIds;
	}

	public List<Integer> getProductionUnitIds() {
		return productionUnitIds;
	}

	public void setProductionUnitIds(List<Integer> productionUnitIds) {
		this.productionUnitIds = productionUnitIds;
	}

	public Boolean getSendNow() {
		return sendNow;
	}

	public void setSendNow(Boolean sendNow) {
		this.sendNow = sendNow;
	}

}
