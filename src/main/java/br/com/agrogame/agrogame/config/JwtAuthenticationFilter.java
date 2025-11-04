package br.com.agrogame.agrogame.config;

import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && jwtUtil.isTokenValid(jwt)) {
                String email = jwtUtil.extractEmail(jwt);
                Integer userId = jwtUtil.extractUserId(jwt);
                
                // Buscar usuário com UserType carregado
                User user = userRepository.findByEmail1WithUserType(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                
                // Criar as authorities a partir do user_type.code
                Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(user.getUserType().getCode()));
                
                // Criar autenticação COM as authorities
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Setar no contexto de segurança
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                System.out.println("DEBUG: Usuário autenticado - Email: " + email + ", UserType: " + user.getUserType().getCode());
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Erro ao processar JWT - " + e.getMessage());
            e.printStackTrace();
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
