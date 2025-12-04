package br.com.agrogame.agrogame.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import br.com.agrogame.agrogame.model.User;
import br.com.agrogame.agrogame.repository.UserRepository;
import br.com.agrogame.agrogame.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        String requestPath = wrappedRequest.getRequestURI();

        if (PublicEndpoints.isPublic(requestPath)) {
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(wrappedRequest);

            if (jwt != null && jwtUtil.isTokenValid(jwt)) {
                String email = jwtUtil.extractEmail(jwt);

                User user = userRepository.findByEmail1WithUserType(email)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(user.getUserType().getCode()));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(wrappedRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        filterChain.doFilter(wrappedRequest, response);
    }


    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
