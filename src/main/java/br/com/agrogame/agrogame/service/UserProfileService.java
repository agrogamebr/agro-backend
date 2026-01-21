package br.com.agrogame.agrogame.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.dto.UpdateUserProfileDTO;
import br.com.agrogame.agrogame.dto.UserProfileResponseDTO;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.repository.UserRepository;

@Service
public class UserProfileService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserDocumentRepository userDocumentRepository;

	@Autowired
	private FileStorageService fileStorageService; // Seu serviço de GCP existente

	// === 1. VISUALIZAR PERFIL ===
	public UserProfileResponseDTO getProfile(String userEmail) {
		User user = findUserByEmail(userEmail);

		UserProfileResponseDTO dto = mapToDTO(user);

		// Busca o documento (CPF/CNPJ) na tabela user_documents e adiciona ao DTO
		userDocumentRepository.findFirstByUserIdAndIsPrimaryTrueAndIsActiveTrue(user.getId())
				.ifPresent(doc -> dto.setDocumentNumber(doc.getDocumentNumber()));

		return dto;
	}

	// === 2. ATUALIZAR DADOS ===
	@Transactional
	public UserProfileResponseDTO updateProfileData(String userEmail, UpdateUserProfileDTO dto) {
		User user = findUserByEmail(userEmail);

		user.setFullName(dto.getFullName());
		user.setEmail1(dto.getEmail());

		// Usa a função auxiliar para salvar apenas números
		user.setPhone(sanitizeDigits(dto.getPhone()));
		user.setZipcode(sanitizeDigits(dto.getZipcode()));

		user.setAddress(dto.getAddress());
		user.setNumber(dto.getNumber());
		user.setCity(dto.getCity());
		user.setState(dto.getState());

		User savedUser = userRepository.save(user);

		// Retornamos o perfil atualizado (incluindo o documento para manter a tela
		// consistente)
		UserProfileResponseDTO responseDTO = mapToDTO(savedUser);
		userDocumentRepository.findFirstByUserIdAndIsPrimaryTrueAndIsActiveTrue(user.getId())
				.ifPresent(doc -> responseDTO.setDocumentNumber(doc.getDocumentNumber()));

		return responseDTO;
	}

	// === 3. ATUALIZAR FOTO ===
	@Transactional
	public String updateProfilePicture(String userEmail, MultipartFile file) throws IOException {
		User user = findUserByEmail(userEmail);

		if (file.isEmpty())
			throw new IllegalArgumentException("Arquivo vazio");

		// Upload para o GCP
		var storedFile = fileStorageService.uploadFile(file);

		// Atualiza a URL no usuário
		user.setProfilePictureUrl(storedFile.getFileUrl());
		userRepository.save(user);

		return user.getProfilePictureUrl();
	}

	// === AUXILIARES ===

	// Remove tudo que não for número (ex: "(11) 999" vira "11999")
	private String sanitizeDigits(String input) {
		if (input == null)
			return null;
		return input.replaceAll("\\D", "");
	}

	private User findUserByEmail(String email) {
		return userRepository.findByEmail1(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
	}

	private UserProfileResponseDTO mapToDTO(User user) {
		UserProfileResponseDTO dto = new UserProfileResponseDTO();
		dto.setFullName(user.getFullName());
		dto.setEmail(user.getEmail1());
		dto.setPhone(user.getPhone());
		dto.setAddress(user.getAddress());
		dto.setNumber(user.getNumber());
		dto.setCity(user.getCity());
		dto.setState(user.getState());
		dto.setZipcode(user.getZipcode());
		dto.setProfilePictureUrl(user.getProfilePictureUrl());
		return dto;
	}
}
