package br.com.agrogame.agrogame.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_units")
public class ProductionUnit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "farm_id", nullable = false)
	private Farm farm;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "production_unit_type_id", nullable = false)
	private ProductionUnitType productionUnitType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "crop_type_id")
	private CropType cropType;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String description;

	@Column(nullable = false)
	private BigDecimal area;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	// --- CAMPO NOVO DE FOTO ---
	@Column(name = "thumbnail_gs_url")
	private String thumbnailGsUrl;
	// --------------------------

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "created_by")
	private Integer createdBy;

	// --- CAMPOS DE AUDITORIA NOVOS ---
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by")
	private Integer updatedBy;

	@Column(name = "deactivated_at")
	private LocalDateTime deactivatedAt;

	@Column(name = "deactivated_by")
	private Integer deactivatedBy;
	// ---------------------------------

	// Getters e Setters
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

	public ProductionUnitType getProductionUnitType() {
		return productionUnitType;
	}

	public void setProductionUnitType(ProductionUnitType productionUnitType) {
		this.productionUnitType = productionUnitType;
	}

	public CropType getCropType() {
		return cropType;
	}

	public void setCropType(CropType cropType) {
		this.cropType = cropType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getArea() {
		return area;
	}

	public void setArea(BigDecimal area) {
		this.area = area;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getThumbnailGsUrl() {
		return thumbnailGsUrl;
	}

	public void setThumbnailGsUrl(String thumbnailGsUrl) {
		this.thumbnailGsUrl = thumbnailGsUrl;
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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getDeactivatedAt() {
		return deactivatedAt;
	}

	public void setDeactivatedAt(LocalDateTime deactivatedAt) {
		this.deactivatedAt = deactivatedAt;
	}

	public Integer getDeactivatedBy() {
		return deactivatedBy;
	}

	public void setDeactivatedBy(Integer deactivatedBy) {
		this.deactivatedBy = deactivatedBy;
	}
}
