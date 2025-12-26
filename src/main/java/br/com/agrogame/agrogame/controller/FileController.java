package br.com.agrogame.agrogame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.agrogame.agrogame.service.FileDownloadService;

@RestController
@RequestMapping("/api/files")
public class FileController {

	@Autowired
	private FileDownloadService fileDownloadService;

	@GetMapping("/proxy")
	public ResponseEntity<Resource> downloadFile(@RequestParam("url") String url) {

		var fileData = fileDownloadService.downloadFileFromUrl(url);

		// Define se vai baixar (attachment) ou mostrar na tela (inline)
		// Se for imagem/pdf, geralmente queremos "inline". Se for zip/doc,
		// "attachment".
		String contentDispositionType = "inline";

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(fileData.contentType()))
				.contentLength(fileData.size())
				.header(HttpHeaders.CONTENT_DISPOSITION,
						contentDispositionType + "; filename=\"" + fileData.fileName() + "\"")
				.body(fileData.resource());
	}
}
