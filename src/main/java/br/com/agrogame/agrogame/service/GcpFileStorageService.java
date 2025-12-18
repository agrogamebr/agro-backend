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

import br.com.agrogame.agrogame.util.StoredFileInfo;

@Service
public class GcpFileStorageService implements FileStorageService {

	private final Storage storage;

	@Value("${gcp.storage.bucket-name:agrogame-uploads-dev}")
	private String bucketName;

	public GcpFileStorageService(Storage storage) {
		this.storage = storage;
	}

	@Override
	public StoredFileInfo uploadFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Arquivo não pode estar vazio");
		}

		String contentType = file.getContentType();
		if (!isAllowedFileType(contentType)) {
			throw new IllegalArgumentException("Tipo de arquivo não permitido: " + contentType);
		}

		String fileName = generateFileName(file.getOriginalFilename());
		String objectName = "submissions/" + fileName;

		BlobId blobId = BlobId.of(bucketName, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();

		Blob blob = storage.create(blobInfo, file.getBytes());

		String fileUrl = String.format("https://storage.googleapis.com/%s/%s", blob.getBucket(), blob.getName());

		String gsutilUri = String.format("gs://%s/%s", blob.getBucket(), blob.getName());

		return new StoredFileInfo(fileUrl, gsutilUri);
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
	
	public void deleteFile(String fileUrl) throws IOException {
        try {
            String blobPath = extractBlobPathFromUrl(fileUrl);
            BlobId blobId = BlobId.of(bucketName, blobPath);
            boolean deleted = storage.delete(blobId);
            
            if (!deleted) {
                throw new IOException("Arquivo não encontrado ou já foi deletado: " + blobPath);
            }
            
            System.out.println("Arquivo deletado com sucesso: " + blobPath);
        } catch (Exception e) {
            throw new IOException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    private String extractBlobPathFromUrl(String publicUrl) {
        // De: https://storage.googleapis.com/bucket-name/path/to/file
        // Para: path/to/file
        String prefix = "https://storage.googleapis.com/";
        if (publicUrl.startsWith(prefix)) {
            String withBucket = publicUrl.substring(prefix.length());
            int slashIndex = withBucket.indexOf('/');
            return withBucket.substring(slashIndex + 1);
        }
        return publicUrl;
    }
}
