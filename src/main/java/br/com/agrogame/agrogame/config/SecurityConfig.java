package br.com.agrogame.agrogame.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException authException) throws IOException, ServletException {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", System.currentTimeMillis());
                body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                body.put("error", "Unauthorized");
                body.put("message", "Token JWT ausente ou inválido");
                body.put("path", request.getServletPath());

                final ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(response.getOutputStream(), body);
            }
        };
    }

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1️⃣ DESABILITAR CSRF (necessário para APIs REST)
                .csrf(csrf -> csrf.disable())

                // 2️⃣ SESSÕES STATELESS (importante para JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3️⃣ EXCEPTION HANDLING (previne 302 redirect)
                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                // 4️⃣ AUTORIZAÇÃO
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/company/validate-email",
                                "/api/company/validate-cnpj",
                                "/api/company/createCompany",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/actuator/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        // Todos os outros precisam de autenticação
                        .anyRequest().authenticated()
                )

                // 5️⃣ DESABILITAR LOGIN FORM (evita redirect)
                .formLogin(form -> form.disable())

                // 6️⃣ DESABILITAR HTTP BASIC (evita redirect)
                .httpBasic(basic -> basic.disable())

                // 7️⃣ ADICIONAR FILTRO JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
	}

	@Bean
	public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token para autenticação. Cole apenas o token sem 'Bearer'")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
	}

}
