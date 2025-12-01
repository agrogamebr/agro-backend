package br.com.agrogame.agrogame.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
	String uploadFile(MultipartFile file) throws IOException;

	String generateFileHash(MultipartFile file) throws IOException;
}
