package com.parkeaya.local_paneladmi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class DjangoIntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DjangoIntegrationService.class);
    
    @Value("${django.api.base-url:http://localhost:8000/api}")
    private String djangoBaseUrl;
    
    @Value("${panel.local.id:SPRING_BOOT_PANEL_001}")
    private String panelLocalId;
    
    private final RestTemplate restTemplate;
    
    public DjangoIntegrationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Envía solicitud de aprobación al panel general de Django
     */
    public Map<String, Object> sendApprovalRequestToDjango(String token, Map<String, Object> parkingData) {
        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                logger.info("🚀 Enviando solicitud de aprobación a Django para: {} (Intento {}/{})", 
                           parkingData.get("name"), retryCount + 1, maxRetries);
                
                // Validar datos requeridos
                validateParkingData(parkingData);
                
                HttpHeaders headers = createHeaders(token);
                
                Map<String, Object> requestBody = buildApprovalRequestBody(parkingData);
                
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
                
                logger.info("📤 Enviando POST a: {}/approval-requests/", djangoBaseUrl);
                
                ResponseEntity<Map> response = restTemplate.exchange(
                    djangoBaseUrl + "/approval-requests/", 
                    HttpMethod.POST,
                    request, 
                    Map.class
                );
                
                if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = response.getBody();
                    logger.info("✅ Solicitud de aprobación enviada exitosamente. ID: {}", 
                               responseBody != null ? responseBody.get("id") : "N/A");
                    return responseBody != null ? responseBody : Map.of("status", "success");
                } else {
                    throw new RuntimeException("Respuesta inesperada de Django: " + response.getStatusCode());
                }
                
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    logger.error("❌ Error de autenticación con Django - Token inválido");
                    throw new RuntimeException("Token de autenticación inválido o expirado");
                } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    logger.error("❌ Acceso denegado por Django");
                    throw new RuntimeException("No tiene permisos para realizar esta acción");
                } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    logger.error("❌ Datos inválidos enviados a Django: {}", e.getResponseBodyAsString());
                    throw new RuntimeException("Datos del estacionamiento inválidos: " + e.getResponseBodyAsString());
                }
                // Para otros errores de cliente, no reintentar
                throw new RuntimeException("Error del cliente Django: " + e.getMessage());
                
            } catch (HttpServerErrorException | ResourceAccessException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    logger.error("❌ Error después de {} intentos: {}", maxRetries, e.getMessage());
                    if (e instanceof HttpServerErrorException) {
                        throw new RuntimeException("El panel general está experimentando problemas. Intente más tarde.");
                    } else {
                        throw new RuntimeException("No se pudo conectar con el panel general. Verifique la conexión.");
                    }
                }
                logger.warn("⚠️ Reintentando en 2 segundos...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operación interrumpida");
                }
                
            } catch (Exception e) {
                logger.error("❌ Error inesperado enviando solicitud a Django: {}", e.getMessage());
                throw new RuntimeException("Error interno del sistema: " + e.getMessage());
            }
        }
        
        throw new RuntimeException("No se pudo enviar la solicitud después de " + maxRetries + " intentos");
    }
    
    /**
     * Consulta el estado de una solicitud de aprobación
     */
    public Map<String, Object> getApprovalStatus(String token, String requestId) {
        try {
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID de solicitud no puede estar vacío");
            }
            
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.info("📥 Consultando estado de solicitud: {}", requestId);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                djangoBaseUrl + "/approval-requests/" + requestId + "/",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Error al consultar estado: " + response.getStatusCode());
            }
            
        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("⚠️ Solicitud no encontrada en Django: {}", requestId);
            return Map.of(
                "error", "Solicitud no encontrada",
                "estado", "NO_ENCONTRADO"
            );
        } catch (Exception e) {
            logger.error("❌ Error consultando estado de aprobación: {}", e.getMessage());
            return Map.of(
                "error", e.getMessage(),
                "estado", "ERROR_CONSULTA"
            );
        }
    }
    
    /**
     * Obtiene el historial de solicitudes de este panel local
     */
    public List<Map<String, Object>> getApprovalRequests(String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.info("📋 Obteniendo historial de solicitudes del panel local");
            
            ResponseEntity<List> response = restTemplate.exchange(
                djangoBaseUrl + "/approval-requests/",
                HttpMethod.GET,
                entity,
                List.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Error al obtener historial: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo historial de solicitudes: {}", e.getMessage());
            // Retornar lista vacía en lugar de lanzar excepción
            return List.of();
        }
    }

    /**
     * Obtiene el estado más reciente de aprobación para este panel local
     */
    public Map<String, Object> getLatestApprovalStatus(String token) {
        try {
            logger.info("🔍 Buscando estado más reciente de aprobación para panel: {}", panelLocalId);
            
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Obtener todas las solicitudes y filtrar por panel local
            ResponseEntity<List> response = restTemplate.exchange(
                djangoBaseUrl + "/approval-requests/",
                HttpMethod.GET,
                entity,
                List.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> allRequests = response.getBody();
                
                // Buscar la solicitud más reciente de este panel local
                Map<String, Object> latestRequest = null;
                for (Map<String, Object> request : allRequests) {
                    String requestPanelId = (String) request.get("panel_local_id");
                    if (panelLocalId.equals(requestPanelId)) {
                        latestRequest = request;
                        break; // Tomar la primera coincidencia (más reciente)
                    }
                }
                
                if (latestRequest != null) {
                    String status = (String) latestRequest.get("status");
                    Object requestId = latestRequest.get("id");
                    Object fechaSolicitud = latestRequest.get("fecha_solicitud");
                    
                    logger.info("✅ Estado más reciente encontrado: {} - ID: {}", status, requestId);
                    return Map.of(
                        "status", status,
                        "request_id", requestId != null ? requestId : "N/A",
                        "fecha_solicitud", fechaSolicitud != null ? fechaSolicitud : "N/A",
                        "estado", status
                    );
                }
            }
            
            // Si no hay solicitudes, retornar estado por defecto
            logger.info("ℹ️ No se encontraron solicitudes de aprobación para el panel: {}", panelLocalId);
            return Map.of(
                "status", "NOT_REGISTERED",
                "estado", "NOT_REGISTERED",
                "message", "No hay solicitudes de aprobación pendientes"
            );
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo estado de aprobación: {}", e.getMessage());
            // En caso de error, retornar estado de error
            return Map.of(
                "status", "ERROR",
                "estado", "ERROR",
                "error", e.getMessage(),
                "message", "Error al consultar estado de aprobación"
            );
        }
    }
    
    /**
     * Procesa actualizaciones de aprobación recibidas via webhook
     */
    public void processApprovalUpdate(String requestId, String status, String motivo) {
        try {
            logger.info("🔄 Procesando actualización de aprobación: {} - {} - {}", requestId, status, motivo);
            
            Map<String, Object> updateData = Map.of(
                "requestId", requestId,
                "status", status,
                "motivo", motivo != null ? motivo : "",
                "timestamp", System.currentTimeMillis(),
                "panelLocalId", panelLocalId
            );
            
            logger.info("✅ Actualización procesada: {}", updateData);
            
            // Aquí puedes agregar lógica adicional como:
            // - Actualizar base de datos local
            // - Enviar notificaciones
            // - Actualizar estado en tiempo real
            
        } catch (Exception e) {
            logger.error("❌ Error procesando actualización de aprobación: {}", e.getMessage());
        }
    }
    
    /**
     * Valida los datos mínimos requeridos para la solicitud
     */
    private void validateParkingData(Map<String, Object> parkingData) {
        if (parkingData == null) {
            throw new IllegalArgumentException("Los datos del estacionamiento no pueden ser nulos");
        }
        
        if (parkingData.get("name") == null || ((String) parkingData.get("name")).trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del estacionamiento es requerido");
        }
        
        if (parkingData.get("address") == null || ((String) parkingData.get("address")).trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección del estacionamiento es requerida");
        }
        
        if (parkingData.get("totalSpaces") == null) {
            throw new IllegalArgumentException("El total de espacios es requerido");
        }
    }
    
    /**
     * Crea headers comunes para todas las requests
     */
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("X-Panel-Local-Id", panelLocalId);
        headers.set("User-Agent", "Parkea-Local-Panel/" + panelLocalId);
        headers.set("Accept", "application/json");
        return headers;
    }
    
    /**
     * Construye el cuerpo de la solicitud de aprobación
     */
    private Map<String, Object> buildApprovalRequestBody(Map<String, Object> parkingData) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Datos básicos requeridos
        requestBody.put("nombre", parkingData.get("name"));
        requestBody.put("direccion", parkingData.get("address"));
        requestBody.put("total_plazas", parkingData.get("totalSpaces"));
        requestBody.put("tarifa_hora", parkingData.get("hourlyRate"));
        
        // Datos opcionales con valores por defecto
        requestBody.put("descripcion", parkingData.getOrDefault("description", ""));
        requestBody.put("telefono", parkingData.getOrDefault("phone", ""));
        requestBody.put("coordenadas", parkingData.getOrDefault("coordinates", ""));
        requestBody.put("horario_apertura", parkingData.getOrDefault("openingTime", "08:00"));
        requestBody.put("horario_cierre", parkingData.getOrDefault("closingTime", "22:00"));
        requestBody.put("nivel_seguridad", parkingData.getOrDefault("securityLevel", "MEDIO"));
        requestBody.put("servicios", parkingData.getOrDefault("services", List.of("Vigilancia")));
        requestBody.put("notas_aprobacion", parkingData.getOrDefault("additional_notes", ""));
        requestBody.put("notas_administrador", parkingData.getOrDefault("adminNotes", ""));
        
        // Metadatos del panel local
        requestBody.put("panel_local_id", panelLocalId);
        requestBody.put("solicitud_timestamp", System.currentTimeMillis());
        requestBody.put("version_solicitud", "1.0");
        
        return requestBody;
    }
    
    /**
     * Método para verificar la conectividad con Django
     */
    public boolean checkDjangoConnectivity(String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                djangoBaseUrl + "/health/",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo conectar con Django: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene solicitudes por panel local ID
     */
    public List<Map<String, Object>> getApprovalRequestsByPanelLocal(String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            logger.info("📋 Obteniendo solicitudes para panel local: {}", panelLocalId);
            
            // Obtener todas las solicitudes y filtrar localmente
            ResponseEntity<List> response = restTemplate.exchange(
                djangoBaseUrl + "/approval-requests/",
                HttpMethod.GET,
                entity,
                List.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> allRequests = response.getBody();
                
                // Filtrar por panel local ID
                List<Map<String, Object>> filteredRequests = allRequests.stream()
                    .filter(request -> panelLocalId.equals(request.get("panel_local_id")))
                    .toList();
                
                logger.info("✅ Solicitudes filtradas por panel local: {} encontradas", filteredRequests.size());
                return filteredRequests;
            }
            
            return List.of();
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo solicitudes por panel local: {}", e.getMessage());
            return List.of();
        }
    }
}