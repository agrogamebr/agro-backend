package br.com.agrogame.agrogame.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.util.StoredFileInfo;

public interface FileStorageService {
	StoredFileInfo uploadFile(MultipartFile file) throws IOException;

	String generateFileHash(MultipartFile file) throws IOException;
	
	void deleteFile(String fileUrl) throws IOException;
}
