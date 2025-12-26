package br.com.agrogame.agrogame.service;

import java.nio.channels.Channels;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;

@Service
public class FileDownloadService {

	@Autowired
	private Storage storage; // Bean do Google Cloud Storage

	public FileDownloadDTO downloadFileFromUrl(String storageUrl) {
		// 1. Extrair Bucket e Nome do Arquivo da URL
		String bucketName;
		String fileName;

		if (storageUrl.startsWith("gs://")) {
			// Formato: gs://meu-bucket/pasta/imagem.jpg
			String temp = storageUrl.substring(5); // Remove "gs://"
			int slashIndex = temp.indexOf("/");
			bucketName = temp.substring(0, slashIndex);
			fileName = temp.substring(slashIndex + 1);
		} else if (storageUrl.contains("storage.googleapis.com/")) {
			// Formato: https://storage.googleapis.com/meu-bucket/pasta/imagem.jpg
			String temp = storageUrl.split("storage.googleapis.com/")[1];
			int slashIndex = temp.indexOf("/");
			bucketName = temp.substring(0, slashIndex);
			fileName = temp.substring(slashIndex + 1);
		} else {
			throw new IllegalArgumentException("Formato de URL não suportado. Use gs:// ou storage.googleapis.com");
		}

		// 2. Buscar o Blob no GCP
		Blob blob = storage.get(BlobId.of(bucketName, fileName));

		if (blob == null || !blob.exists()) {
			throw new RuntimeException("Arquivo não encontrado no Storage: " + fileName);
		}

		// 3. Preparar o Stream de leitura
		// Usamos reader para não carregar tudo na memória RAM de uma vez (bom para
		// arquivos grandes)
		InputStreamResource resource = new InputStreamResource(Channels.newInputStream(blob.reader()));

		return new FileDownloadDTO(resource, blob.getContentType(), // Ex: image/jpeg, application/pdf
				blob.getSize(), fileName);
	}

	// Classe interna ou DTO separado para transportar os dados
	public record FileDownloadDTO(InputStreamResource resource, String contentType, Long size, String fileName) {
	}
}
