package br.com.agrogame.agrogame.dto;

public class WorkerDetailDTO {

	private Integer id;
	private String fullname;
	private String email;
	private String phone;
	private String profilePictureUrl;

	// Campos agregados da query (nomes de fazendas/unidades), opcionais
	private String farmNames; // ex: "Fazenda A, Fazenda B"
	private String unitNames; // ex: "Talhão 1, Talhão 2"

	private String zipcode;
	private String number;

	public WorkerDetailDTO() {
	}

	// Construtor usado pela projeção nativa do WorkerRepository
	public WorkerDetailDTO(Integer id, String fullname, String email, String phone, String profilePictureUrl,
			String unitNames, String farmNames, String zipcode, String number) {
		this.id = id;
		this.fullname = fullname;
		this.email = email;
		this.phone = phone;
		this.profilePictureUrl = profilePictureUrl;
		this.unitNames = unitNames;
		this.farmNames = farmNames;
		this.zipcode = zipcode;
		this.number = number;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getProfilePictureUrl() {
		return profilePictureUrl;
	}

	public void setProfilePictureUrl(String profilePictureUrl) {
		this.profilePictureUrl = profilePictureUrl;
	}

	public String getFarmNames() {
		return farmNames;
	}

	public void setFarmNames(String farmNames) {
		this.farmNames = farmNames;
	}

	public String getUnitNames() {
		return unitNames;
	}

	public void setUnitNames(String unitNames) {
		this.unitNames = unitNames;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
}
