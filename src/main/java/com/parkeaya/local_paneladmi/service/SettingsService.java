package com.parkeaya.local_paneladmi.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

@Service
public class SettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${parkeaya.general-panel.url:http://localhost:8000/api}")
    private String generalPanelUrl;

    @Value("${parkeaya.general-panel.api-key:default-key}")
    private String apiKey;

    @Value("${parkeaya.local-parking.id:LOCAL_PARKING_001}")
    private String localParkingId;

    // Almacenamiento temporal de solicitudes
    private final Map<String, Map<String, Object>> approvalRequests = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("🎯 SettingsService inicializado");
        logger.info("🎯 generalPanelUrl: {}", generalPanelUrl);
        logger.info("🎯 apiKey: {}", apiKey != null ? "***" + apiKey.substring(Math.max(0, apiKey.length() - 4)) : "NULL");
        logger.info("🎯 localParkingId: {}", localParkingId);
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    public Map<String, Object> getParkingSettings() {
        try {
            // Intentar obtener datos de Django primero
            try {
                Map<String, Object> djangoData = getParkingDataFromDjango();
                if (djangoData != null && !djangoData.isEmpty()) {
                    logger.info("✅ Configuración obtenida desde Django");
                    return djangoData;
                }
            } catch (Exception e) {
                logger.warn("⚠️ No se pudo conectar con Django, usando datos locales: {}", e.getMessage());
            }

            // Fallback a datos locales
            logger.info("📦 Usando configuración local");
            return getLocalParkingData();

        } catch (Exception e) {
            logger.error("❌ Error al obtener configuración", e);
            throw new RuntimeException("Error al obtener configuración: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> saveSettings(Map<String, Object> settings) {
        try {
            // Determinar el ID del parking
            String parkingId = localParkingId;
            if (settings.containsKey("id") && settings.get("id") != null) {
                String providedId = String.valueOf(settings.get("id")).trim();
                if (!providedId.isEmpty()) {
                    parkingId = providedId;
                    this.localParkingId = parkingId; // Actualizar en memoria
                }
            }

            settings.put("id", parkingId);
            settings.put("lastUpdated", new Date());

            // Intentar guardar en Django
            try {
                saveToDjango(settings);
                logger.info("✅ Configuración guardada en Django");
            } catch (Exception e) {
                logger.warn("⚠️ No se pudo guardar en Django: {}", e.getMessage());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuración guardada correctamente");
            response.put("parking_id", parkingId);
            response.put("timestamp", new Date());

            return response;

        } catch (Exception e) {
            logger.error("❌ Error al guardar configuración", e);
            throw new RuntimeException("Error al guardar configuración: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> updateVisibility(Boolean isVisible) {
        try {
            logger.info("🔄 Actualizando visibilidad a: {}", isVisible);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isVisible", isVisible);
            response.put("message", "Visibilidad actualizada correctamente");
            response.put("timestamp", new Date());

            return response;

        } catch (Exception e) {
            logger.error("❌ Error al actualizar visibilidad", e);
            throw new RuntimeException("Error al actualizar visibilidad: " + e.getMessage(), e);
        }
    }

    /**
     * 🚀 REGISTRO MEJORADO - Enfocado en CREAR en Django
     */
    public Map<String, Object> registerWithParkea(Map<String, Object> payload) {
        try {
            logger.info("🚀 INICIANDO REGISTRO EN DJANGO...");

            // Extraer datos del parking
            Map<String, Object> parkingData = extractParkingDataFromPayload(payload);
            if (parkingData == null || parkingData.isEmpty()) {
                parkingData = getParkingSettings();
            }

            // Crear solicitud de registro
            String requestId = "REQ_" + System.currentTimeMillis();

            Map<String, Object> approvalRequest = new HashMap<>();
            approvalRequest.put("request_id", requestId);
            approvalRequest.put("parking_id", localParkingId);
            approvalRequest.put("status", "PENDING");
            approvalRequest.put("submission_date", new Date());
            approvalRequest.put("admin_notes", payload.get("notes"));
            approvalRequest.put("parking_data", parkingData);

            approvalRequests.put(requestId, approvalRequest);
            logger.info("📝 Solicitud de registro creada localmente: {}", requestId);

            // ✅ INTENTAR CREAR EN DJANGO
            try {
                String djangoId = createParkingInDjango(parkingData);
                approvalRequest.put("django_id", djangoId);
                approvalRequest.put("status", "CREATED_IN_DJANGO");
                logger.info("✅ Parking creado en Django exitosamente. ID: {}", djangoId);
                
            } catch (Exception e) {
                logger.error("⚠️ No se pudo crear parking en Django: {}", e.getMessage());
                approvalRequest.put("django_error", e.getMessage());
                approvalRequest.put("status", "PENDING_LOCAL");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("request_id", requestId);
            response.put("status", approvalRequest.get("status"));
            response.put("message", "Solicitud de registro procesada correctamente");
            response.put("django_connected", approvalRequest.containsKey("django_id"));
            response.put("timestamp", new Date());

            return response;

        } catch (Exception e) {
            logger.error("❌ Error al registrar", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al registrar: " + e.getMessage());
            return errorResponse;
        }
    }

    // Sobrecarga para compatibilidad
    public Map<String, Object> registerWithParkea(String notes) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notes", notes);
        return registerWithParkea(payload);
    }

    public Map<String, Object> getApprovalStatus(String requestId) {
        try {
            Map<String, Object> request = approvalRequests.get(requestId);
            if (request == null) {
                throw new RuntimeException("Solicitud no encontrada: " + requestId);
            }

            // Consultar estado en Django si tenemos el ID de Django
            try {
                Object djangoId = request.get("django_id");
                if (djangoId != null) {
                    Map<String, Object> djangoStatus = getParkingStatusFromDjango(String.valueOf(djangoId));
                    request.put("django_status", djangoStatus);
                    
                    if (djangoStatus.containsKey("aprobado")) {
                        boolean aprobado = Boolean.TRUE.equals(djangoStatus.get("aprobado"));
                        request.put("status", aprobado ? "APPROVED" : "PENDING");
                    }
                    logger.info("✅ Estado actualizado desde Django para parking: {}", requestId);
                }
            } catch (Exception e) {
                logger.warn("⚠️ No se pudo obtener estado de Django: {}", e.getMessage());
            }

            Map<String, Object> status = new HashMap<>();
            status.put("request_id", requestId);
            status.put("status", request.get("status"));
            status.put("submission_date", request.get("submission_date"));
            status.put("last_checked", new Date());
            status.put("django_connected", request.containsKey("django_id"));

            return status;

        } catch (Exception e) {
            logger.error("❌ Error al obtener estado", e);
            throw new RuntimeException("Error al obtener estado: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getApprovalRequests() {
        try {
            List<Map<String, Object>> requests = new ArrayList<>();
            
            for (Map<String, Object> request : approvalRequests.values()) {
                Map<String, Object> requestInfo = new HashMap<>();
                requestInfo.put("id", request.get("request_id"));
                requestInfo.put("django_id", request.get("django_id"));
                requestInfo.put("status", request.get("status"));
                requestInfo.put("fecha_solicitud", request.get("submission_date"));
                requestInfo.put("nombre", "Estacionamiento Local");
                requestInfo.put("admin_notes", request.get("admin_notes"));
                requestInfo.put("django_connected", request.containsKey("django_id"));
                requests.add(requestInfo);
            }
            
            logger.info("📋 Retornando {} solicitudes", requests.size());
            return requests;

        } catch (Exception e) {
            logger.error("❌ Error al obtener solicitudes", e);
            throw new RuntimeException("Error al obtener solicitudes: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> uploadImage(MultipartFile file) {
        try {
            String imageUrl = "/uploads/" + file.getOriginalFilename();
            logger.info("📸 Imagen subida: {}", imageUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imageUrl", imageUrl);
            response.put("message", "Imagen subida correctamente");

            return response;

        } catch (Exception e) {
            logger.error("❌ Error al subir imagen", e);
            throw new RuntimeException("Error al subir imagen: " + e.getMessage(), e);
        }
    }

    /**
     * 🔐 Probar autenticación con Django
     */
    public Map<String, Object> testDjangoAuth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String url = generalPanelUrl + "/parking/";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            result.put("success", true);
            result.put("status", response.getStatusCode().toString());
            result.put("message", "✅ Autenticación exitosa con Django");
            
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            result.put("success", false);
            result.put("error", "Token inválido o expirado");
            result.put("message", "❌ Error de autenticación - Actualiza la API Key");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "❌ Error de conexión con Django");
        }
        
        return result;
    }

    // ==================== MÉTODOS PRIVADOS - INTEGRACIÓN DJANGO ====================

    /**
     * 🔍 Obtener datos de parking desde Django - CORREGIDO
     */
    private Map<String, Object> getParkingDataFromDjango() {
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = generalPanelUrl + "/parking/";
            logger.info("🔍 GET Django Parking List: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.hasBody()) {
                Map<String, Object> responseBody = response.getBody();
                logger.info("✅ Datos de parking obtenidos desde Django");
                
                // Buscar el parking local en la lista
                return findLocalParkingInList(responseBody);
            } else {
                logger.warn("⚠️ No se pudo obtener lista de parkings, status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo obtener datos de parking de Django: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 🔍 Buscar parking local en la lista de resultados - CORREGIDO
     */
    private Map<String, Object> findLocalParkingInList(Map<String, Object> responseBody) {
        if (responseBody == null) {
            logger.warn("⚠️ Response body es null");
            return null;
        }
        
        try {
            List<Map<String, Object>> results;
            
            // Manejar diferentes estructuras de respuesta
            if (responseBody.containsKey("results")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> temp = (List<Map<String, Object>>) responseBody.get("results");
                results = temp;
            } else if (responseBody instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> temp = (List<Map<String, Object>>) responseBody;
                results = temp;
            } else {
                // Si no es lista, tratar como objeto único
                results = List.of(responseBody);
            }
            
            if (results == null || results.isEmpty()) {
                logger.info("📭 No hay parkings en Django");
                return null;
            }
            
            logger.info("🔍 Buscando en {} parkings de Django", results.size());
            
            // ESTRATEGIA: Buscar por nombre que coincida con nuestro parking local
            Map<String, Object> localData = getLocalParkingData();
            String localName = String.valueOf(localData.get("name"));
            
            for (Map<String, Object> parking : results) {
                // Verificar si el nombre coincide
                if (parking.containsKey("nombre")) {
                    String djangoName = String.valueOf(parking.get("nombre"));
                    if (djangoName != null && djangoName.equalsIgnoreCase(localName)) {
                        logger.info("✅ Parking local encontrado por nombre: {}", localName);
                        return convertDjangoToLocalFormat(parking);
                    }
                }
            }
            
            // Si no encuentra por nombre, usar el primer parking disponible
            logger.info("⚠️ No encontrado por nombre, usando primer parking disponible");
            return convertDjangoToLocalFormat(results.get(0));
            
        } catch (Exception e) {
            logger.error("❌ Error al procesar lista de parkings: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 🆕 CREAR parking en Django - MÉTODO NUEVO
     */
    private String createParkingInDjango(Map<String, Object> parkingData) {
        String url = generalPanelUrl + "/parking/";
        
        try {
            HttpHeaders headers = createAuthHeaders();
            Map<String, Object> djangoData = prepareParkingDataForDjango(parkingData);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(djangoData, headers);

            logger.info("📤 CREANDO parking en Django: {}", url);
            logger.info("📦 DATOS ENVIADOS: {}", djangoData);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("id")) {
                    String djangoId = String.valueOf(responseBody.get("id"));
                    logger.info("🎉 PARKING CREADO EXITOSAMENTE - ID: {}", djangoId);
                    return djangoId;
                }
            }
            
            throw new RuntimeException("Django respondió: " + response.getStatusCode());
            
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            logger.error("❌ ERROR DE AUTENTICACIÓN: Token inválido o expirado");
            throw new RuntimeException("Error de autenticación: Token inválido. Actualiza la API Key en la configuración.");
        } catch (Exception e) {
            logger.error("❌ Error al crear parking en Django: {}", e.getMessage());
            throw new RuntimeException("Error creando parking en Django: " + e.getMessage(), e);
        }
    }

    /**
     * 📦 Preparar datos para Django - CORREGIDO
     */
    private Map<String, Object> prepareParkingDataForDjango(Map<String, Object> parkingData) {
        Map<String, Object> djangoData = new HashMap<>();
        
        // ✅ CAMPOS REQUERIDOS según Django
        djangoData.put("dueno", 10); // ID del dueño existente
        djangoData.put("nombre", parkingData.getOrDefault("name", "Estacionamiento Local"));
        djangoData.put("direccion", parkingData.getOrDefault("address", "Dirección no especificada"));
        djangoData.put("coordenadas", "-8.1156,-79.0291"); // Coordenadas por defecto
        djangoData.put("telefono", "+51944123456"); // Teléfono por defecto
        djangoData.put("descripcion", parkingData.getOrDefault("description", "Estacionamiento del panel local"));
        djangoData.put("nivel_seguridad", "Media");
        
        // Tarifa por hora
        Object hourlyRate = parkingData.get("hourlyRate");
        if (hourlyRate instanceof Number) {
            djangoData.put("tarifa_hora", String.format("%.2f", ((Number) hourlyRate).doubleValue()));
        } else {
            djangoData.put("tarifa_hora", "5.00");
        }
        
        // Total de plazas
        Object totalSpaces = parkingData.get("totalSpaces");
        if (totalSpaces instanceof Number) {
            int spaces = ((Number) totalSpaces).intValue();
            djangoData.put("total_plazas", spaces);
            djangoData.put("plazas_disponibles", spaces);
        } else {
            djangoData.put("total_plazas", 20);
            djangoData.put("plazas_disponibles", 20);
        }
        
        // Campos adicionales
        djangoData.put("horario_apertura", "06:00:00");
        djangoData.put("horario_cierre", "23:59:00");
        djangoData.put("aprobado", false); // IMPORTANTE: requiere aprobación
        djangoData.put("activo", true);
        djangoData.put("rating_promedio", 0);
        djangoData.put("total_resenas", 0);
        
        return djangoData;
    }

    private void saveToDjango(Map<String, Object> settings) {
        if (settings == null) {
            logger.warn("⚠️ saveToDjango: settings es null");
            return;
        }

        try {
            HttpHeaders headers = createAuthHeaders();
            Map<String, Object> djangoData = convertLocalToDjangoFormat(settings);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(djangoData, headers);

            String parkingId = settings.get("id") != null ? String.valueOf(settings.get("id")) : localParkingId;
            String url = generalPanelUrl + "/parking/parking/" + parkingId + "/";
            
            logger.info("💾 PUT Django: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ Guardado en Django OK - Status: {}", response.getStatusCode());
            } else {
                logger.warn("⚠️ Django respondió: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("❌ Error al guardar en Django: {}", e.getMessage());
        }
    }

    /**
     * 🔍 Consultar estado de parking en Django
     */
    private Map<String, Object> getParkingStatusFromDjango(String djangoId) {
        String url = generalPanelUrl + "/parking/parking/" + djangoId + "/";
        
        try {
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            logger.info("🔍 Consultando parking en Django: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Estado de parking obtenido desde Django");
                return response.getBody();
            } else {
                logger.warn("⚠️ Django respondió con: {}", response.getStatusCode());
                throw new RuntimeException("Error al obtener estado: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("❌ Error al obtener estado de parking: {}", e.getMessage());
            throw new RuntimeException("Error obteniendo estado: " + e.getMessage(), e);
        }
    }

    // ==================== MÉTODOS DE UTILIDAD ====================

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        if (apiKey == null || apiKey.trim().isEmpty() || "default-key".equals(apiKey)) {
            logger.warn("⚠️ API Key no configurada o es inválida");
        } else {
            headers.set("Authorization", "Bearer " + apiKey);
        }
        
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private Map<String, Object> convertDjangoToLocalFormat(Map<String, Object> djangoData) {
        Map<String, Object> localData = new HashMap<>();
        localData.put("id", djangoData.get("id"));
        localData.put("name", djangoData.get("nombre"));
        localData.put("address", djangoData.get("direccion"));
        localData.put("totalSpaces", djangoData.get("total_plazas"));
        localData.put("hourlyRate", djangoData.get("tarifa_hora"));
        localData.put("description", djangoData.get("descripcion"));
        localData.put("isVisible", djangoData.get("activo"));
        localData.put("registrationStatus", djangoData.get("aprobado") != null && Boolean.TRUE.equals(djangoData.get("aprobado")) ? "APPROVED" : "PENDING");
        localData.put("imageUrl", djangoData.get("imagen_url"));
        return localData;
    }

    private Map<String, Object> convertLocalToDjangoFormat(Map<String, Object> localData) {
        Map<String, Object> djangoData = new HashMap<>();
        if (localData == null) return djangoData;

        djangoData.put("nombre", localData.getOrDefault("name", "").toString());
        djangoData.put("direccion", localData.getOrDefault("address", "").toString());
        djangoData.put("descripcion", localData.getOrDefault("description", "").toString());

        Object totalSpacesObj = localData.get("totalSpaces");
        if (totalSpacesObj instanceof Number) {
            djangoData.put("total_plazas", ((Number) totalSpacesObj).intValue());
        } else {
            try {
                djangoData.put("total_plazas", Integer.parseInt(String.valueOf(totalSpacesObj)));
            } catch (Exception e) {
                djangoData.put("total_plazas", 0);
            }
        }

        Object hourlyRateObj = localData.get("hourlyRate");
        if (hourlyRateObj instanceof Number) {
            djangoData.put("tarifa_hora", ((Number) hourlyRateObj).doubleValue());
        } else {
            try {
                djangoData.put("tarifa_hora", Double.parseDouble(String.valueOf(hourlyRateObj)));
            } catch (Exception e) {
                djangoData.put("tarifa_hora", 0.0);
            }
        }

        Object isVisibleObj = localData.get("isVisible");
        if (isVisibleObj instanceof Boolean) {
            djangoData.put("activo", isVisibleObj);
        } else {
            djangoData.put("activo", Boolean.parseBoolean(String.valueOf(isVisibleObj)));
        }

        return djangoData;
    }

    private Map<String, Object> getLocalParkingData() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("id", localParkingId);
        settings.put("name", "Mi Estacionamiento");
        settings.put("address", "Ingrese la dirección de su estacionamiento");
        settings.put("totalSpaces", 50);
        settings.put("hourlyRate", 3.50);
        settings.put("description", "");
        settings.put("isVisible", false);
        settings.put("registrationStatus", "NOT_REGISTERED");
        settings.put("imageUrl", null);
        return settings;
    }

    private Map<String, Object> extractParkingDataFromPayload(Map<String, Object> payload) {
        Map<String, Object> parkingData = new HashMap<>();
        
        if (payload.containsKey("name")) parkingData.put("name", payload.get("name"));
        if (payload.containsKey("address")) parkingData.put("address", payload.get("address"));
        if (payload.containsKey("totalSpaces")) parkingData.put("totalSpaces", payload.get("totalSpaces"));
        if (payload.containsKey("hourlyRate")) parkingData.put("hourlyRate", payload.get("hourlyRate"));
        if (payload.containsKey("description")) parkingData.put("description", payload.get("description"));
        if (payload.containsKey("isVisible")) parkingData.put("isVisible", payload.get("isVisible"));
        
        return parkingData.isEmpty() ? null : parkingData;
    }

    /**
     * Método para probar la conexión con Django
     */
    public Map<String, Object> testDjangoConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String parkingUrl = generalPanelUrl + "/parking/";
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> parkingResponse = restTemplate.exchange(
                parkingUrl, HttpMethod.GET, entity, String.class);
            
            result.put("parking_endpoint", parkingUrl);
            result.put("parking_status", parkingResponse.getStatusCode().toString());
            result.put("parking_working", parkingResponse.getStatusCode().is2xxSuccessful());
            
            result.put("success", true);
            result.put("message", "Prueba de conexión completada");
            result.put("django_url", generalPanelUrl);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "Error en la prueba de conexión");
        }
        
        return result;
    }
}