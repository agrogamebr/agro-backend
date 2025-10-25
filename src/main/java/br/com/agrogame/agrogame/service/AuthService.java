package br.com.agrogame.agrogame.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.agrogame.agrogame.dto.LoginRequestDTO;
import br.com.agrogame.agrogame.dto.LoginResponseDTO;
import br.com.agrogame.agrogame.model.AuthCredential;
import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.AuthCredentialRepository;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.util.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private AuthCredentialRepository authCredentialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        // Buscar credencial pelo email
        AuthCredential credential = authCredentialRepository
            .findByIdentifier(dto.getEmail())
            .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        // Verificar se está ativo
        if (!credential.getIsActive()) {
            throw new RuntimeException("Usuário inativo");
        }

        // Validar senha
        if (!passwordEncoder.matches(dto.getPassword(), credential.getPasswordHash())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // Atualizar último login
        credential.setLastLoginAt(java.time.LocalDateTime.now());
        authCredentialRepository.save(credential);

        // Buscar User completo pelo ID
        User user = userRepository.findById(credential.getUser().getId())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Gerar token
        String token = jwtUtil.generateToken(user.getEmail1(), user.getId());

        return new LoginResponseDTO(token, user.getId(), user.getEmail1(), user.getFullname());
    }
}

