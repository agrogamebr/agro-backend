package br.com.agrogame.agrogame.dto;

import br.com.agrogame.agrogame.model.UserDocumentType;

public class UserDocumentTypeDTO {
	private Integer id;
	private String code;
	private String name;

	public UserDocumentTypeDTO() {
	}

	public UserDocumentTypeDTO(Integer id, String code, String name) {
		this.id = id;
		this.code = code;
		this.name = name;
	}

	public UserDocumentTypeDTO(UserDocumentType entity) {
		this.id = entity.getId();
		this.code = entity.getCode();
		this.name = entity.getName();
	}

	// Getters e setters
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
}
