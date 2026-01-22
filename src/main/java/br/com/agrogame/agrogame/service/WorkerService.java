package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.WorkerCreateDTO;
import br.com.agrogame.agrogame.dto.WorkerDetailDTO;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.exceptions.BadRequestException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.exceptions.DuplicateResourceException;
import br.com.agrogame.agrogame.exceptions.ResourceNotFoundException;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.Farm;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserStatus;
import br.com.agrogame.agrogame.model.UserType;
import br.com.agrogame.agrogame.model.WorkerProductionUnitAssignment;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.FarmRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.repository.UserStatusRepository;
import br.com.agrogame.agrogame.repository.UserTypeRepository;
import br.com.agrogame.agrogame.repository.WorkerProductionUnitAssignmentRepository;
import br.com.agrogame.agrogame.repository.WorkerRepository;

@Service
public class WorkerService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserTypeRepository userTypeRepository;

	@Autowired
	private UserStatusRepository userStatusRepository;

	@Autowired
	private WorkerRepository workerRepository;

	@Autowired
	private WorkerProductionUnitAssignmentRepository assignmentRepository;

	@Autowired
	private AuthCredentialRepository authCredentialRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ValidationService validationService;

	@Autowired
	private FarmRepository farmRepository;

	// LISTAR workers do produtor
	@Transactional(readOnly = true)
	public List<WorkerDetailDTO> listWorkers(String producerEmail, Integer farmIdFilter) {
		User producer = findUserByEmailWithType(producerEmail);

		if (!isProducer(producer)) {
			throw new BusinessException("Usuário autenticado não é produtor rural");
		}

		if (farmIdFilter != null) {
			Farm farm = farmRepository.findById(farmIdFilter)
					.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
			if (!farm.getOwner().getId().equals(producer.getId())) {
				throw new BusinessException("Fazenda não pertence ao produtor");
			}
		}

		return workerRepository.findWorkersByProducer(producer.getId(), farmIdFilter);
	}

	@Transactional
	public WorkerDetailDTO registerWorker(String producerEmail, WorkerCreateDTO dto) {
		User producer = findUserByEmailWithType(producerEmail);

		if (!isProducer(producer)) {
			throw new BusinessException("Apenas produtores podem cadastrar workers");
		}

		// 1) Validar formato e unicidade do email
		if (!validationService.isValidEmailFormat(dto.getEmail())) {
			throw new BadRequestException("Formato de e-mail inválido");
		}
		if (validationService.emailAlreadyExists(dto.getEmail())) {
			throw new DuplicateResourceException("E-mail já cadastrado");
		}

		// 2) Validar senha
		validatePassword(dto.getPassword());

		// 3) Validar se a fazenda informada pertence ao produtor logado
		Farm farm = farmRepository.findById(dto.getFarmId())
				.orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada"));
		if (!farm.getOwner().getId().equals(producer.getId())) {
			throw new BusinessException("Fazenda não pertence ao produtor logado");
		}

		// 4) Tipo e status do worker
		UserType workerType = userTypeRepository.findByCode(EnumUserType.WORKER.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de usuário WORKER não encontrado"));

		UserStatus activeStatus = userStatusRepository.findByCode(EnumUserStatus.APPROVED.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status ativo não encontrado"));

		// 5) Montar entidade User
		User worker = new User();
		worker.setFullName(dto.getFullname());
		worker.setEmail1(dto.getEmail());
		worker.setPhone(dto.getPhone());
		worker.setUserType(workerType);
		worker.setUserStatus(activeStatus);
		worker.setCreatedAt(LocalDateTime.now());
		worker.setPointsBalance(0);

		worker.setAddress(dto.getAddress());
		worker.setNumber(dto.getNumber());
		worker.setCity(dto.getCity());
		worker.setState(dto.getState());
		worker.setZipcode(dto.getZipcode());
		worker.setCompany(producer.getCompany());
		
		String[] parts = dto.getFullname().trim().split("\\s+", 2);
		worker.setFirstName(parts[0]);
		worker.setLastName(parts.length > 1 ? parts[1] : "");

		User savedWorker = userRepository.save(worker);

		// 6) credencial em auth_credentials
		AuthCredential credential = new AuthCredential();
		credential.setUser(savedWorker);
		credential.setProvider("email");
		credential.setIdentifier(dto.getEmail());
		credential.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
		credential.setIsActive(true);
		credential.setFailedAttempts(0);
		credential.setCreatedAt(LocalDateTime.now());

		authCredentialRepository.save(credential);

		// 7) vincular unidades produtivas (se vierem)
		if (dto.getProductionUnitId() != null) {
			assignSingleUnitInternalForFarm(producer, dto.getFarmId(), savedWorker.getId(), dto.getProductionUnitId());
		}

		return toDetailDTO(savedWorker);
	}

	// EDITAR worker (produtor ou o próprio worker)
	@Transactional
	public WorkerDetailDTO updateWorker(String loggedEmail, Integer workerId, WorkerCreateDTO dto) {
		User loggedUser = findUserByEmailWithType(loggedEmail);
		User worker = userRepository.findById(workerId)
				.orElseThrow(() -> new ResourceNotFoundException("Worker não encontrado"));

		boolean isSelf = loggedUser.getId().equals(workerId);
		boolean isProducerOwner = isProducer(loggedUser)
				&& workerRepository.existsWorkerBelongsToProducer(loggedEmail, workerId);

		if (!isSelf && !isProducerOwner) {
			throw new BusinessException("Você não tem permissão para editar este worker");
		}

		worker.setFullName(dto.getFullname());
		String[] parts = dto.getFullname().trim().split("\\s+", 2);
		worker.setFirstName(parts[0]);
		worker.setLastName(parts.length > 1 ? parts[1] : "");

		if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(worker.getEmail1())) {
			if (!validationService.isValidEmailFormat(dto.getEmail())) {
				throw new BadRequestException("Formato de e-mail inválido");
			}
			if (validationService.emailAlreadyExists(dto.getEmail())) {
				throw new DuplicateResourceException("E-mail já cadastrado");
			}
			worker.setEmail1(dto.getEmail());

			authCredentialRepository.findByProviderAndIdentifier("email", worker.getEmail1()).ifPresent(cred -> {
				cred.setIdentifier(dto.getEmail());
				cred.setUpdatedAt(LocalDateTime.now());
				authCredentialRepository.save(cred);
			});
		}

		worker.setPhone(dto.getPhone());
		worker.setAddress(dto.getAddress());
		worker.setNumber(dto.getNumber());
		worker.setCity(dto.getCity());
		worker.setState(dto.getState());
		worker.setZipcode(dto.getZipcode());
		worker.setUpdatedAt(LocalDateTime.now());

		if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
			validatePassword(dto.getPassword());
			AuthCredential cred = authCredentialRepository.findByProviderAndIdentifier("email", worker.getEmail1())
					.orElseThrow(() -> new ResourceNotFoundException("Credencial do worker não encontrada"));
			cred.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
			cred.setUpdatedAt(LocalDateTime.now());
			authCredentialRepository.save(cred);
		}

		User saved = userRepository.save(worker);

		if (isProducerOwner && dto.getProductionUnitId() != null) {
			assignUnitInternal(loggedUser, workerId, dto.getProductionUnitId());
		}

		return toDetailDTO(saved);
	}

	// INATIVAR worker
	@Transactional
	public void deactivateWorker(String producerEmail, Integer workerId) {
		User producer = findUserByEmailWithType(producerEmail);

		if (!isProducer(producer)) {
			throw new BusinessException("Apenas produtores podem inativar workers");
		}

		if (!workerRepository.existsWorkerBelongsToProducer(producerEmail, workerId)) {
			throw new BusinessException("Worker não pertence ao produtor");
		}

		User worker = userRepository.findById(workerId)
				.orElseThrow(() -> new ResourceNotFoundException("Worker não encontrado"));

		UserStatus inactiveStatus = userStatusRepository.findByCode(EnumUserStatus.INACTIVE.getCode())
				.orElseThrow(() -> new ResourceNotFoundException("Status INACTIVE não encontrado"));

		worker.setUserStatus(inactiveStatus);
		worker.setUpdatedAt(LocalDateTime.now());
		userRepository.save(worker);

		authCredentialRepository.findByProviderAndIdentifier("email", worker.getEmail1()).ifPresent(cred -> {
			cred.setIsActive(false);
			cred.setUpdatedAt(LocalDateTime.now());
			authCredentialRepository.save(cred);
		});
	}

	// ASSOCIAR unidade produtiva (uso interno)
	@Transactional
	protected void assignUnitInternal(User producer, Integer workerId, Integer productionUnitId) {
		// Se vier null, apenas remove qualquer vínculo existente
		if (productionUnitId == null) {
			assignmentRepository.deleteByWorkerId(workerId);
			return;
		}

		// Valida se a unidade pertence a alguma fazenda do produtor
		Long count = workerRepository.countUnitsByProducer(List.of(productionUnitId), producer.getId());
		if (!count.equals(1L)) {
			throw new BusinessException("Unidade produtiva não pertence às fazendas do produtor");
		}

		// Remove vínculos antigos e cria o novo
		assignmentRepository.deleteByWorkerId(workerId);

		WorkerProductionUnitAssignment ass = new WorkerProductionUnitAssignment();
		ass.setWorkerId(workerId);
		ass.setProductionUnitId(productionUnitId);
		ass.setIsActive(true);
		ass.setCreatedAt(LocalDateTime.now());
		assignmentRepository.save(ass);
	}

	// AUXILIARES

	private User findUserByEmailWithType(String email) {
		return userRepository.findByEmail1(email)
				.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
	}

	private boolean isProducer(User user) {
		return user.getUserType() != null && EnumUserType.PRODUCER.getCode().equals(user.getUserType().getCode());
	}

	private void validatePassword(String password) {
		if (password == null || password.length() < 8) {
			throw new BadRequestException("Senha deve ter pelo menos 8 caracteres");
		}
		if (!password.matches(".*[A-Z].*")) {
			throw new BadRequestException("Senha deve conter pelo menos uma letra maiúscula");
		}
		if (!password.matches(".*[^A-Za-z0-9].*")) {
			throw new BadRequestException("Senha deve conter pelo menos um caractere especial");
		}
	}

	private WorkerDetailDTO toDetailDTO(User worker) {
		WorkerDetailDTO dto = new WorkerDetailDTO();
		dto.setId(worker.getId());
		dto.setFullname(worker.getFullName());
		dto.setEmail(worker.getEmail1());
		dto.setPhone(worker.getPhone());
		dto.setProfilePictureUrl(worker.getProfilePictureUrl());
		return dto;
	}

	@Transactional
	protected void assignSingleUnitInternalForFarm(User producer, Integer farmId, Integer workerId,
			Integer productionUnitId) {
		// valida se a unit pertence ao produtor e à farm
		Long countByProducer = workerRepository.countUnitsByProducer(List.of(productionUnitId), producer.getId());
		if (countByProducer != 1L) {
			throw new BusinessException("Unidade produtiva não pertence às fazendas do produtor");
		}

		Long countByFarm = workerRepository.countUnitsByFarm(List.of(productionUnitId), farmId);
		if (countByFarm != 1L) {
			throw new BusinessException("Unidade produtiva não pertence à fazenda informada");
		}

		assignmentRepository.deleteByWorkerId(workerId);

		WorkerProductionUnitAssignment ass = new WorkerProductionUnitAssignment();
		ass.setWorkerId(workerId);
		ass.setProductionUnitId(productionUnitId);
		ass.setIsActive(true);
		ass.setCreatedAt(LocalDateTime.now());
		assignmentRepository.save(ass);
	}

}
