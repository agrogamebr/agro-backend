package br.com.agrogame.agrogame.dto;

import java.time.LocalDateTime;

import br.com.agrogame.agrogame.model.CompanyType;

public class CompanyTypeDTO {
    private Integer id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public CompanyTypeDTO() {}

    public CompanyTypeDTO(CompanyType companyType) {
        this.id = companyType.getId();
        this.code = companyType.getCode();
        this.name = companyType.getName();
        this.description = companyType.getDescription();
        this.isActive = companyType.getIsActive();
        this.createdAt = companyType.getCreatedAt();
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
