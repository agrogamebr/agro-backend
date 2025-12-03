package br.com.agrogame.agrogame.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_activity_submission_files")
public class UserActivitySubmissionFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_activity_id", nullable = false)
	private UserActivity userActivity;

	@Column(name = "file_url", nullable = false)
	private String fileUrl;

	@Column(name = "file_hash", nullable = false)
	private String fileHash;

	@Column(name = "gsutil_uri", nullable = false)
	private String gsutilUri;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public UserActivity getUserActivity() {
		return userActivity;
	}

	public void setUserActivity(UserActivity userActivity) {
		this.userActivity = userActivity;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getFileHash() {
		return fileHash;
	}

	public void setFileHash(String fileHash) {
		this.fileHash = fileHash;
	}

	public String getGsutilUri() {
		return gsutilUri;
	}

	public void setGsutilUri(String gsutilUri) {
		this.gsutilUri = gsutilUri;
	}
}