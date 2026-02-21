package br.com.agrogame.agrogame.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ActivityListDTO {
	private Integer id;
	private String name;
	private Integer companyId;
	private String description;
	private Integer points;
	private String status;
	private LocalDate validFrom;
	private LocalDate validTo;
    private LocalDateTime createdAt;
    private Integer createdBy;
    private LocalDateTime updatedAt;
    private Integer updatedBy;

	private String userActivityStatus;
	private Integer userActivityFarmId;

	private String thumbnailUrl;
	private String thumbnailGsutilUri;

	private List<Integer> cropTypeIds;
	private List<Integer> farmIds;
	private List<Integer> productionUnitIds;
	private List<Integer> userActivityIds;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Integer> getUserActivityIds() {
		return userActivityIds;
	}

	public void setUserActivityIds(List<Integer> userActivityIds) {
		this.userActivityIds = userActivityIds;
	}

	public String getUserActivityStatus() {
		return userActivityStatus;
	}

	public void setUserActivityStatus(String userActivityStatus) {
		this.userActivityStatus = userActivityStatus;
	}

	public Integer getUserActivityFarmId() {
		return userActivityFarmId;
	}

	public void setUserActivityFarmId(Integer userActivityFarmId) {
		this.userActivityFarmId = userActivityFarmId;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getThumbnailGsutilUri() {
		return thumbnailGsutilUri;
	}

	public void setThumbnailGsutilUri(String thumbnailGsutilUri) {
		this.thumbnailGsutilUri = thumbnailGsutilUri;
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

}
