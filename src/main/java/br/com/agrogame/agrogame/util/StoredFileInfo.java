package br.com.agrogame.agrogame.util;

public class StoredFileInfo {

	private final String fileUrl;
	private final String gsutilUri;

	public StoredFileInfo(String fileUrl, String gsutilUri) {
		this.fileUrl = fileUrl;
		this.gsutilUri = gsutilUri;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public String getGsutilUri() {
		return gsutilUri;
	}
}
