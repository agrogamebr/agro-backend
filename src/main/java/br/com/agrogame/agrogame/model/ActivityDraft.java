package br.com.agrogame.agrogame.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "activity_draft")
public class ActivityDraft {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "activity_id", nullable = false)
	private Activity activity;

	@Column(name = "user_id")
	private Integer userId; // Mapeado como ID simples para facilitar


	@Type(JsonType.class)
	@Column(name = "farm_ids", columnDefinition = "jsonb")
	private List<Integer> farmIds = new ArrayList<>();

	@Type(JsonType.class)
	@Column(name = "production_unit_ids", columnDefinition = "jsonb")
	private List<Integer> productionUnitIds = new ArrayList<>();

	@Type(JsonType.class)
	@Column(name = "crop_type_ids", columnDefinition = "jsonb")
	private List<Integer> cropTypeIds = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "farm_id", insertable = false, updatable = false)
	private Farm farm;

	@ManyToOne
	@JoinColumn(name = "production_unit_id", insertable = false, updatable = false)
	private ProductionUnit productionUnit;

	@ManyToOne
	@JoinColumn(name = "crop_type_id", insertable = false, updatable = false)
	private CropType cropType;

	// --- Auditoria ---
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by")
	private Integer updatedBy;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Farm getFarm() {
		return farm;
	}

	public void setFarm(Farm farm) {
		this.farm = farm;
	}

	public ProductionUnit getProductionUnit() {
		return productionUnit;
	}

	public void setProductionUnit(ProductionUnit productionUnit) {
		this.productionUnit = productionUnit;
	}

	public CropType getCropType() {
		return cropType;
	}

	public void setCropType(CropType cropType) {
		this.cropType = cropType;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
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

	public List<Integer> getCropTypeIds() {
		return cropTypeIds;
	}

	public void setCropTypeIds(List<Integer> cropTypeIds) {
		this.cropTypeIds = cropTypeIds;
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

}
