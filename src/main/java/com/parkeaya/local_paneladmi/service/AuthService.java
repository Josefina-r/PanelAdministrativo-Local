package com.parkeaya.local_paneladmi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.Map;
import java.security.Principal;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Almacenamiento simple en memoria para el token (para desarrollo)
    private String currentUserToken;

    public AuthService(RestTemplateBuilder builder) {
        // Añadimos timeouts cortos para fallar rápido y evitar bloqueos largos
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        // Asegurar que el RestTemplate utilice UTF-8 para texto
        this.restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public String authenticate(String username, String password) {
        String url = "http://localhost:8000/api/users/admin-login/"; 
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        headers.setAccept(java.util.Collections.singletonList(new MediaType("application", "json", StandardCharsets.UTF_8)));

       
        logger.info("Intentando autenticar usuario '{}' contra {}", username, url);
        logger.debug("Request body: {}", requestBody);

        
        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            logger.error("Error serializando el body de autenticación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al construir petición de autenticación", e);
        }

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            logger.info("Intentando autenticar usuario '{}' contra {}", username, url);
            logger.debug("Request body: {}", requestBody);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            logger.info("Respuesta de Django - Status: {}", response.getStatusCode());
            logger.debug("Response headers: {}", response.getHeaders());
            logger.debug("Response body: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Intentar obtener el token de diferentes campos comunes
                Object token = response.getBody().get("access");  
                if (token == null) token = response.getBody().get("token"); 
                if (token == null) token = response.getBody().get("key");   
                
                if (token != null) {
                    logger.info("Autenticación exitosa para usuario '{}'", username);
                    // Guardar el token para uso posterior
                    this.currentUserToken = token.toString();
                    return this.currentUserToken;
                } else {
                    logger.warn("Respuesta 2xx pero sin token en ningún campo conocido. Body: {}", response.getBody());
                    throw new RuntimeException("Respuesta sin token de autenticación");
                }
            } else {
                logger.warn("Login fallido. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Credenciales inválidas");
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("Error HTTP {} al autenticar: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error de autenticación: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al conectar con Django: {}", e.getMessage(), e);
            throw new RuntimeException("Error al conectar con Django: " + e.getMessage());
        }
    }

    /**
     * Obtiene el token del usuario actualmente autenticado
     * Versión simplificada sin OAuth2
     */
    public String getCurrentUserToken(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }

        try {
            logger.debug("Obteniendo token para usuario: {}", principal.getName());
            
            // Opción 1: Usar el token almacenado en memoria (para desarrollo)
            if (this.currentUserToken != null) {
                logger.debug("Token obtenido de almacenamiento en memoria");
                return this.currentUserToken;
            }
            
            // Opción 2: Intentar obtener de SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String) {
                String token = (String) authentication.getCredentials();
                logger.debug("Token obtenido de SecurityContext credentials");
                return token;
            }
            
            // Opción 3: Intentar obtener de los detalles de autenticación
            if (authentication != null && authentication.getDetails() instanceof String) {
                String token = (String) authentication.getDetails();
                logger.debug("Token obtenido de SecurityContext details");
                return token;
            }
            
            // Opción 4: Si el nombre del principal es un token (fallback simple)
            if (principal.getName() != null && principal.getName().length() > 20) {
                logger.debug("Usando nombre del principal como token (fallback)");
                return principal.getName();
            }
            
            // Si no se puede obtener el token, lanzar excepción
            throw new IllegalStateException("No se pudo obtener el token del usuario autenticado. " +
                "El usuario necesita autenticarse primero.");
            
        } catch (Exception e) {
            logger.error("Error al obtener el token del usuario: {}", e.getMessage());
            throw new IllegalStateException("Error al obtener el token: " + e.getMessage(), e);
        }
    }

    /**
     * Almacena el token para el usuario actual (para sesiones simples)
     */
    public void setCurrentUserToken(String token) {
        this.currentUserToken = token;
        logger.debug("Token almacenado en AuthService");
    }

    /**
     * Limpia el token actual (para logout)
     */
    public void clearCurrentUserToken() {
        this.currentUserToken = null;
        logger.debug("Token eliminado de AuthService");
    }

    /**
     * Obtiene el email del usuario actualmente autenticado
     */
    public String getCurrentUserEmail(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }

        try {
            // En una implementación simple, el nombre del principal puede ser el email
            return principal.getName();
            
        } catch (Exception e) {
            throw new IllegalStateException("Error al obtener el email del usuario: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si el usuario actual está autenticado
     */
    public boolean isAuthenticated(Principal principal) {
        return principal != null && this.currentUserToken != null;
    }

    /**
     * Verifica la validez del token actual
     */
    public boolean isTokenValid() {
        return this.currentUserToken != null && !this.currentUserToken.trim().isEmpty();
    }
}