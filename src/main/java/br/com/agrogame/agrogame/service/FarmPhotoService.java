package br.com.agrogame.agrogame.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.com.agrogame.agrogame.exceptions.AuthenticationException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.ProductionUnit;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.ProductionUnitRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.util.StoredFileInfo;

@Service
public class FarmPhotoService {

	@Autowired
	private FileStorageService fileStorageService; // Serviço de Storage existente do projeto

	@Autowired
	private FarmRepository farmRepository;

	@Autowired
	private ProductionUnitRepository productionUnitRepository;

	@Autowired
	private UserRepository userRepository;

	@Transactional
	public String uploadFarmPhoto(String email, Integer farmId, MultipartFile file) throws IOException {
		User user = getUserOrThrow(email);

		Farm farm = farmRepository.findById(farmId)
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));

		if (!farm.getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para alterar esta fazenda.");
		}

		validateImageFile(file);

		// Upload
		StoredFileInfo stored = fileStorageService.uploadFile(file);

		farm.setThumbnailGsUrl(stored.getGsutilUri());
		farmRepository.save(farm);

		return stored.getGsutilUri();
	}

	@Transactional
	public String uploadUnitPhoto(String email, Integer unitId, MultipartFile file) throws IOException {
		User user = getUserOrThrow(email);

		ProductionUnit unit = productionUnitRepository.findById(unitId)
				.orElseThrow(() -> new ResourceNotFoundException("Unidade produtiva não encontrada"));

		if (!unit.getFarm().getOwner().getId().equals(user.getId())) {
			throw new AuthenticationException("FORBIDDEN", "Você não tem permissão para alterar esta unidade.");
		}

		validateImageFile(file);

		StoredFileInfo stored = fileStorageService.uploadFile(file);

		unit.setThumbnailGsUrl(stored.getGsutilUri());
		productionUnitRepository.save(unit);

		return stored.getGsutilUri();
	}

	private User getUserOrThrow(String email) {
		// Use o método do seu UserRepo que busca com UserType se necessário
		return userRepository.findByEmail1WithUserType(email)
				.orElseThrow(() -> new AuthenticationException("USER_NOT_FOUND", "Usuário não encontrado"));
	}

	private void validateImageFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new BusinessException("O arquivo de imagem está vazio.");
		}
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new BusinessException("Apenas arquivos de imagem (JPG, PNG) são permitidos.");
		}
	}
}
