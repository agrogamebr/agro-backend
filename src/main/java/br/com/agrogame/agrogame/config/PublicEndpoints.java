package br.com.agrogame.agrogame.config;

import java.util.Arrays;
import java.util.List;

public class PublicEndpoints {
    
    public static final List<String> ENDPOINTS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/register",
        "/api/company/validate-email",
        "/api/company/validate-cnpj",
        "/api/company/create-company",
        "/api/company/company-types",
        "/api/producer/register",
        "/api/producer/validate-email",
        "/api/producer/validate-document",
        "/api/producer/document-types",
        "/api/producer/companies/active",
        "/swagger-ui/**",
		"/v3/api-docs/**",
		"/swagger-ui.html",
		"/actuator/**",
		"/actuator/health",
		"/actuator/info",
		 "/api/auth/forgot-password"
    );
    
    public static boolean isPublic(String path) {
        return ENDPOINTS.stream().anyMatch(path::startsWith);
    }
}

