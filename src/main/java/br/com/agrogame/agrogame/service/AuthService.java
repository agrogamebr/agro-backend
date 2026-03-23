package br.com.agrogame.agrogame.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.agrogame.agrogame.dto.LoginRequestDTO;
import br.com.agrogame.agrogame.dto.LoginResponseDTO;
import br.com.agrogame.agrogame.enumerator.EnumCompanyStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserStatus;
import br.com.agrogame.agrogame.enumerator.EnumUserType;
import br.com.agrogame.agrogame.exceptions.AuthenticationException;
import br.com.agrogame.agrogame.exceptions.BusinessException;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.Company;
import br.com.agrogame.agrogame.model.CompanyDocument;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.model.UserDocument;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.CompanyDocumentsRepository;
import br.com.agrogame.agrogame.repository.UserDocumentRepository;
import br.com.agrogame.agrogame.util.JwtUtil;
import br.com.agrogame.auth.util.IdentifierValidator;
import br.com.agrogame.auth.util.IdentifierValidator.IdentifierType;

@Service
public class AuthService {

	@Autowired
	private AuthCredentialRepository authCredentialRepository;

	@Autowired
	private UserDocumentRepository userDocumentRepository;

	@Autowired
	private CompanyDocumentsRepository companyDocumentsRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Value("${auth.max-login-attempts:5}")
	private Integer maxLoginAttempts;

	@Value("${auth.token-expiration-ms:3600000}")
	private Long tokenExpirationMs;

	@Transactional
	public LoginResponseDTO login(LoginRequestDTO dto) {
		String normalizedIdentifier = IdentifierValidator.getNormalizedIfValid(dto.getIdentifier());
		IdentifierType identifierType = IdentifierValidator.detectType(dto.getIdentifier());

		String loginEmail = resolveEmailFromIdentifier(normalizedIdentifier, identifierType);

		AuthCredential credential = authCredentialRepository.findByIdentifier(loginEmail)
				.orElseThrow(() -> new AuthenticationException("INVALID_CREDENTIALS", "Login ou senha inválidos"));

		if (!credential.getIsActive()) {
			throw new AuthenticationException("ACCOUNT_LOCKED",
					"Sua conta foi bloqueada. Entre em contato com o suporte para desbloquear.");
		}

		boolean authenticated = false;
		boolean usingTemporaryPassword = false;

		// 1) Tenta senha normal
		if (passwordEncoder.matches(dto.getPassword(), credential.getPasswordHash())) {
			authenticated = true;
		} else if (credential.getTemporaryPasswordHash() != null && credential.getTemporaryPasswordExpiresAt() != null
				&& credential.getTemporaryPasswordExpiresAt().isAfter(LocalDateTime.now())
				&& passwordEncoder.matches(dto.getPassword(), credential.getTemporaryPasswordHash())) {

			authenticated = true;
			usingTemporaryPassword = true;
		}

		if (!authenticated) {
			incrementFailedLoginAttempts(credential);
			throw new AuthenticationException("INVALID_CREDENTIALS", "Login ou senha inválidos");
		}

		User user = credential.getUser();
		validateUserStatus(user);

		credential.setFailedAttempts(0);
		credential.setLastLoginAt(LocalDateTime.now());
		authCredentialRepository.save(credential);

		String token = jwtUtil.generateToken(user.getEmail1(), user.getId());

		LoginResponseDTO response = new LoginResponseDTO(token, user.getId(), user.getEmail1(), user.getFullName(),
				user.getUserType() != null ? user.getUserType().getName() : null, tokenExpirationMs,
				usingTemporaryPassword);
		response.setForceChangePassword(usingTemporaryPassword);

		return response;
	}

	/**
	 * Resolve o email a partir do identifier normalizado e seu tipo
	 */
	private String resolveEmailFromIdentifier(String normalizedIdentifier, IdentifierType identifierType) {
		switch (identifierType) {
		case EMAIL:
			// Se for email, retorna direto
			return normalizedIdentifier;

		case CPF:
			// Se for CPF, busca em UserDocuments com tipo CPF
			UserDocument cpfDoc = userDocumentRepository
					.findByDocumentNumberAndDocumentType_CodeAndIsActiveTrue(normalizedIdentifier, "CPF")
					.orElseThrow(() -> new AuthenticationException("INVALID_CREDENTIALS", "Login ou senha inválidos"));
			return cpfDoc.getUser().getEmail1();

		case CNPJ:
			// Se for CNPJ, busca em CompanyDocuments com empresa aprovada
			CompanyDocument cnpjDoc = companyDocumentsRepository
					.findByDocumentNumberAndDocumentType_CodeAndCompanyApproved(normalizedIdentifier, "CNPJ")
					.orElseThrow(() -> new AuthenticationException("INVALID_CREDENTIALS", "Login ou senha inválidos"));

			Company company = cnpjDoc.getCompany();

			// Validação extra: verifica se a empresa está aprovada
			if (!EnumCompanyStatus.APPROVED.getId().equals(company.getCompanyStatus().getId())) {
				throw new AuthenticationException("COMPANY_NOT_APPROVED", "Empresa não aprovada para login");
			}

			return company.getEmail1();

		case INVALID:
		default:
			throw new AuthenticationException("INVALID_IDENTIFIER",
					"Identificador inválido. Aceitar email, CPF (11 dígitos) ou CNPJ (14 dígitos).");
		}
	}

	@Transactional
	private void incrementFailedLoginAttempts(AuthCredential credential) {
		Integer currentAttempts = credential.getFailedAttempts() != null ? credential.getFailedAttempts() : 0;
		credential.setFailedAttempts(currentAttempts + 1);

		// Se atingiu o máximo de tentativas, desativa a credencial
		if (credential.getFailedAttempts() >= maxLoginAttempts) {
			credential.setIsActive(false);
		}

		authCredentialRepository.save(credential);
	}

	private void validateUserStatus(User user) {
		if (!user.getUserStatus().getIsActive()) {
			throw new AuthenticationException("USER_INACTIVE",
					"Sua conta foi desativada. Entre em contato com o suporte.");
		}

		if (EnumUserType.EMPLOYEE.equals(user.getUserType())) {
			if ("PENDING".equalsIgnoreCase(user.getUserStatus().getCode())) {
				throw new AuthenticationException("PRODUCER_PENDING",
						"Seu cadastro aguarda aprovação da empresa parceira");
			}

			if ("REJECTED".equalsIgnoreCase(user.getUserStatus().getCode())) {
				throw new AuthenticationException("PRODUCER_REJECTED",
						"Seu cadastro foi recusado. Entre em contato com a empresa parceira");
			}
		}
	}

	@Transactional
	public void changePassword(String email, String oldPassword, String newPassword) {
		AuthCredential cred = authCredentialRepository.findByIdentifier(email)
				.orElseThrow(() -> new BusinessException("Usuário não encontrado"));

		var user = cred.getUser();
		var statusCode = user.getUserStatus().getCode();
		boolean podeRecuperarSenha = EnumUserStatus.ACTIVE.getCode().equals(statusCode)
				|| EnumUserStatus.APPROVED.getCode().equals(statusCode);

		if (!podeRecuperarSenha) {
			// se o usuário não estiver ativo ou aprovado, não permite alterar senha
			return;
		}

		boolean validOld = passwordEncoder.matches(oldPassword, cred.getPasswordHash())
				|| (cred.getTemporaryPasswordHash() != null && cred.getTemporaryPasswordExpiresAt() != null
						&& cred.getTemporaryPasswordExpiresAt().isAfter(LocalDateTime.now())
						&& passwordEncoder.matches(oldPassword, cred.getTemporaryPasswordHash()));

		if (!validOld) {
			throw new BusinessException("Senha atual inválida");
		}

		cred.setPasswordHash(passwordEncoder.encode(newPassword));
		cred.setTemporaryPasswordHash(null);
		cred.setTemporaryPasswordExpiresAt(null);
		authCredentialRepository.save(cred);
	}

	@Transactional
	public void updateToNewPassword(String identifier, String newPassword) {
		AuthCredential credential = authCredentialRepository.findByIdentifier(identifier)
				.orElseThrow(() -> new BusinessException("Credenciais não encontradas para o usuário."));

		// 1) Encripta a nova senha e salva como principal
		credential.setPasswordHash(passwordEncoder.encode(newPassword));

		// 2) Invalida a senha temporária
		credential.setTemporaryPasswordHash(null);
		credential.setTemporaryPasswordExpiresAt(null);

		// 3) Reseta tentativas falhas (opcional)
		credential.setFailedAttempts(0);

		authCredentialRepository.save(credential);
	}

}
