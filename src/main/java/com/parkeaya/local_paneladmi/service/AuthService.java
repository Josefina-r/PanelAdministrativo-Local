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

import java.time.Duration;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        String url = "http://localhost:8000/api/admin-login/"; 
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
                    return token.toString();
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
}
