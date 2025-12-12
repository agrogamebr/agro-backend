package br.com.agrogame.agrogame.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "farm_crops")
public class FarmCrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_type_id", nullable = false)
    private CropType cropType;

    private BigDecimal plantedArea;
    private Integer year;

    private LocalDateTime createdAt;
    private Integer createdBy;
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Farm getFarm() {
		return farm;
	}
	public void setFarm(Farm farm) {
		this.farm = farm;
	}
	public CropType getCropType() {
		return cropType;
	}
	public void setCropType(CropType cropType) {
		this.cropType = cropType;
	}
	public BigDecimal getPlantedArea() {
		return plantedArea;
	}
	public void setPlantedArea(BigDecimal plantedArea) {
		this.plantedArea = plantedArea;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	public Integer getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

}
