package br.com.agrogame.agrogame.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

@Service
public class GcpFileStorageService implements FileStorageService {	

	private final Storage storage;

	@Value("${gcp.storage.bucket-name:agrogame-uploads-dev}")
	private String bucketName;

	public GcpFileStorageService(Storage storage) {
		this.storage = storage;
	}

	@Override
	public String uploadFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Arquivo não pode estar vazio");
		}

		// Validar tipo de arquivo
		String contentType = file.getContentType();
		if (!isAllowedFileType(contentType)) {
			throw new IllegalArgumentException("Tipo de arquivo não permitido: " + contentType);
		}

		// Gerar nome único para o arquivo
		String fileName = generateFileName(file.getOriginalFilename());

		// Fazer upload para GCP
		BlobId blobId = BlobId.of(bucketName, "submissions/" + fileName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

		Blob blob = storage.create(blobInfo, file.getBytes());

		// URL pública padrão do GCS
		String fileUrl = String.format("https://storage.googleapis.com/%s/%s", blob.getBucket(), blob.getName());

		return fileUrl;

	}

	@Override
	public String generateFileHash(MultipartFile file) throws IOException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(file.getBytes());
			return Base64.getEncoder().encodeToString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Erro ao gerar hash do arquivo", e);
		}
	}

	private String generateFileName(String originalFileName) {
		String extension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf('.'))
				: ".bin";
		return UUID.randomUUID().toString() + extension;
	}

	private boolean isAllowedFileType(String contentType) {
		if (contentType == null)
			return false;

		return contentType.equals("application/pdf") || contentType.equals("application/msword")
				|| contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
				|| contentType.equals("image/png") || contentType.equals("image/jpeg")
				|| contentType.equals("image/jpg");
	}
}
