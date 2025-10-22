package com.parkeaya.local_paneladmi.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class AuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String authenticate(String username, String password) {
        String url = "http://localhost:8000/api/admin-login/"; // Django admin login
        Map<String, String> requestBody = Map.of(
            "username", username,
            "password", password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("access"); // JWT
            } else {
                throw new RuntimeException("Login fallido");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Django: " + e.getMessage());
        }
    }
}
