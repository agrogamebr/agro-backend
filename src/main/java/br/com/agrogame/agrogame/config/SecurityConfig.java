package br.com.agrogame.agrogame.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
		.csrf(csrf -> csrf.disable())
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authorizeHttpRequests(auth -> auth
				// Endpoints públicos (sem autenticação)
				.requestMatchers(
						"/api/company/validate-email",
						"/api/company/validate-cnpj",
						"/api/company/createCompany",
						"/api/auth/login",
						"/api/auth/register",
						"/swagger-ui/**",
						"/v3/api-docs/**",
						"/swagger-ui.html"
						).permitAll()
				// Todos os outros endpoints exigem autenticação
				.anyRequest().authenticated()
				)
		// Adicionar o filtro JWT ANTES de usar autenticação padrão
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
