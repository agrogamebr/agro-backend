package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class CreateActivityMultipartDTO {
	private String description;
	private String name;
	private Integer points;
	private LocalDate validFrom;
	private LocalDate validTo;
	private List<Integer> cropTypeIds;

	// Arquivo (recebido como file)
	private MultipartFile thumbnail;

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

}
