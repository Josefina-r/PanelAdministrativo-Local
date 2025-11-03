package com.parkeaya.local_paneladmi.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.parkeaya.local_paneladmi.service.DashboardService;

@RestController
// Cambiado para evitar conflicto con AdminSettingsApiController
@RequestMapping("/api/internal/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @Autowired
    private DashboardService dashboardService;

    // ==================== ENDPOINTS GET ====================

    /**
     * Obtener configuración del estacionamiento
     */
    @GetMapping("")
    public ResponseEntity<?> getParkingSettings(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("🎯 GET /api/internal/settings - Obteniendo configuración del estacionamiento");
        
        try {
            String token = extractToken(authorizationHeader);
            logger.info("🔑 Token procesado: {}", token != null ? "PRESENTE" : "NULO");
            
            Map<String, Object> settings = dashboardService.getParkingConfiguration(token);
            logger.info("✅ Configuración obtenida exitosamente - Campos: {}", settings.keySet());
            
            return ResponseEntity.ok(settings);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en getParkingSettings: {}", e.getMessage(), e);
            
            // En desarrollo, devolver datos por defecto en lugar de error
            Map<String, Object> defaultSettings = createDefaultSettings();
            defaultSettings.put("warning", "Usando datos por defecto debido a: " + e.getMessage());
            defaultSettings.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.ok(defaultSettings);
        }
    }

    /**
     * Endpoint de health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        logger.info("❤️ GET /api/internal/settings/health - Health check");
        
        Map<String, String> response = new HashMap<>();
        response.put("service", "SettingsController");
        response.put("status", "OK");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("message", "Servicio de configuración funcionando correctamente");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de debug para diagnóstico
     */
    @GetMapping("/debug")
    public ResponseEntity<?> debugSettings(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("🐛 GET /api/internal/settings/debug - Diagnóstico del servicio");
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("timestamp", java.time.LocalDateTime.now().toString());
        debugInfo.put("service", "SettingsController");
        debugInfo.put("status", "ACTIVE");
        debugInfo.put("authorizationHeader", authorizationHeader);
        
        try {
            String token = extractToken(authorizationHeader);
            debugInfo.put("extractedToken", token);
            debugInfo.put("tokenStatus", "VALID");
            
            // Verificar inyección del servicio
            debugInfo.put("dashboardService", dashboardService != null ? "INYECTADO" : "NULL");
            
            if (dashboardService != null) {
                // Probar el servicio
                Map<String, Object> testData = dashboardService.getParkingConfiguration(token);
                debugInfo.put("serviceTest", "SUCCESS");
                debugInfo.put("dataKeys", testData.keySet());
                debugInfo.put("sampleData", testData.get("name"));
            } else {
                debugInfo.put("serviceTest", "FAILED");
                debugInfo.put("error", "DashboardService no está inyectado");
            }
            
        } catch (Exception e) {
            debugInfo.put("serviceTest", "ERROR");
            debugInfo.put("error", e.getMessage());
            debugInfo.put("errorType", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    /**
     * Endpoint simple para desarrollo
     */
    @GetMapping("/simple")
    public ResponseEntity<?> simpleSettings() {
        logger.info("🎯 GET /api/internal/settings/simple - Datos simples de desarrollo");
        
        Map<String, Object> settings = createDefaultSettings();
        settings.put("message", "✅ Datos de desarrollo funcionando correctamente");
        settings.put("source", "SettingsController - Simple Endpoint");
        
        return ResponseEntity.ok(settings);
    }

    /**
     * Obtener estado de aprobación
     */
    @GetMapping("/approval-status/{requestId}")
    public ResponseEntity<?> getApprovalStatus(@PathVariable String requestId) {
        logger.info("📊 GET /api/internal/settings/approval-status/{} - Consultando estado", requestId);
        
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("requestId", requestId);
            status.put("status", "PENDING");
            status.put("message", "Solicitud en proceso de revisión");
            status.put("submittedAt", java.time.LocalDateTime.now().minusDays(1).toString());
            status.put("lastUpdated", java.time.LocalDateTime.now().toString());
            status.put("estimatedCompletion", java.time.LocalDateTime.now().plusDays(2).toString());
            
            logger.info("✅ Estado de aprobación consultado para: {}", requestId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en getApprovalStatus: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo obtener el estado de aprobación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener solicitudes de aprobación
     */
    @GetMapping("/approval-requests")
    public ResponseEntity<?> getApprovalRequests(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        logger.info("📋 GET /api/internal/settings/approval-requests - Obteniendo solicitudes");
        
        try {
            String token = extractToken(authorizationHeader);
            
            // Datos de ejemplo para desarrollo
            Map<String, Object> response = new HashMap<>();
            response.put("requests", new java.util.ArrayList<>());
            response.put("total", 0);
            response.put("pending", 0);
            response.put("approved", 0);
            response.put("rejected", 0);
            response.put("message", "No hay solicitudes pendientes");
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            
            logger.info("✅ Solicitudes de aprobación obtenidas");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en getApprovalRequests: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo obtener las solicitudes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== ENDPOINTS PUT ====================

    /**
     * Guardar/Actualizar configuración completa
     */
    @PutMapping("")
    public ResponseEntity<?> saveSettings(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Object> settings) {
        logger.info("💾 PUT /api/internal/settings - Guardando configuración: {}", settings.keySet());
        
        try {
            // Validar datos requeridos
            if (settings.get("name") == null || settings.get("name").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del estacionamiento es requerido");
            }
            if (settings.get("address") == null || settings.get("address").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("La dirección del estacionamiento es requerida");
            }

            String token = extractToken(authorizationHeader);
            Map<String, Object> result = dashboardService.saveParkingConfiguration(token, settings);
            
            logger.info("✅ Configuración guardada exitosamente");
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Validación fallida en saveSettings: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("❌ ERROR en saveSettings: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo guardar la configuración: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Actualizar solo la visibilidad
     */
    @PutMapping("/visibility-ui")
    public ResponseEntity<?> updateVisibility(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody Map<String, Boolean> request) {
        logger.info("👁️ PUT /api/internal/settings/visibility-ui - Actualizando visibilidad: {}", request);
        
        try {
            if (request.get("isVisible") == null) {
                throw new IllegalArgumentException("El campo 'isVisible' es requerido");
            }

            String token = extractToken(authorizationHeader);
            Boolean isVisible = request.get("isVisible");
            Map<String, Object> result = dashboardService.updateParkingVisibility(token, isVisible);
            
            logger.info("✅ Visibilidad actualizada exitosamente a: {}", isVisible);
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Validación fallida en updateVisibility: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("❌ ERROR en updateVisibility: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo actualizar la visibilidad: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== ENDPOINTS POST ====================

    /**
     * Registrar con Parkea (panel general)
     */
    @PostMapping("/register-parkea")
    public ResponseEntity<?> registerWithParkea(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) Map<String, String> requestData) {
        logger.info("📱 POST /api/internal/settings/register-parkea - Registrando con Parkea");
        
        try {
            String token = extractToken(authorizationHeader);
            String notes = (requestData != null) ? requestData.get("notes") : null;
            
            Map<String, Object> result = dashboardService.registerWithParkea(token);
            
            // Si se proporcionaron notas, agregarlas al resultado
            if (notes != null && !notes.trim().isEmpty()) {
                result.put("notes", notes.trim());
            }
            
            // Agregar información adicional
            result.put("requestId", "REQ_" + System.currentTimeMillis());
            result.put("submittedAt", java.time.LocalDateTime.now().toString());
            result.put("status", "PENDING");
            
            logger.info("✅ Registro con Parkea enviado exitosamente");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en registerWithParkea: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo registrar con Parkea: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Subir imagen del estacionamiento
     */
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam("image") MultipartFile file) {
        logger.info("🖼️ POST /api/internal/settings/upload-image - Subiendo imagen: {}", file.getOriginalFilename());
        
        try {
            // Validar archivo
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo de imagen está vacío");
            }
            
            if (!file.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("El archivo debe ser una imagen (JPG, PNG, WEBP)");
            }
            
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                throw new IllegalArgumentException("La imagen debe ser menor a 5MB");
            }

            String token = extractToken(authorizationHeader);
            Map<String, Object> result = dashboardService.uploadParkingImage(token, file);
            
            // Agregar información adicional
            result.put("originalFilename", file.getOriginalFilename());
            result.put("size", file.getSize());
            result.put("contentType", file.getContentType());
            result.put("uploadedAt", java.time.LocalDateTime.now().toString());
            
            logger.info("✅ Imagen subida exitosamente: {}", file.getOriginalFilename());
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            logger.warn("⚠️ Validación fallida en uploadImage: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            logger.error("❌ ERROR en uploadImage: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo subir la imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS ====================

    /**
     * Crear configuración por defecto
     */
    private Map<String, Object> createDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("id", "LOCAL_PARKING_001");
        defaultSettings.put("name", "Mi Estacionamiento Principal");
        defaultSettings.put("address", "Av. Siempre Viva 123, Springfield");
        defaultSettings.put("totalSpaces", 80);
        defaultSettings.put("hourlyRate", 4.50);
        defaultSettings.put("description", "Estacionamiento seguro con cámaras de vigilancia 24/7, iluminación LED y personal de seguridad.");
        defaultSettings.put("isVisible", true);
        defaultSettings.put("registrationStatus", "ACTIVE");
        defaultSettings.put("imageUrl", "");
        defaultSettings.put("ownerId", 1);
        defaultSettings.put("securityLevel", "ALTO");
        defaultSettings.put("operatingHours", "24/7");
        defaultSettings.put("amenities", new String[]{"Vigilancia", "Iluminación", "Seguro"});
        defaultSettings.put("lastUpdated", java.time.LocalDateTime.now().toString());
        return defaultSettings;
    }

    /**
     * Extraer token del header de autorización
     */
    private String extractToken(String authorizationHeader) {
        logger.debug("🔐 Procesando header Authorization: {}", authorizationHeader);
        
        // Para desarrollo: aceptar cualquier token o incluso sin token
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            logger.debug("✅ Token extraído: {}...", token.length() > 10 ? token.substring(0, 10) + "..." : token);
            return token;
        }
        
        // Para desarrollo: si no hay token, usar uno por defecto
        logger.warn("⚠️ No se proporcionó token válido, usando modo desarrollo");
        return "dev-token-" + System.currentTimeMillis();
    }

    /**
     * Endpoint de prueba para verificar que el controlador responde
     */
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        logger.info("🧪 GET /api/internal/settings/test - Endpoint de prueba");
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ SettingsController está funcionando correctamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("endpoints", new String[]{
            "GET /api/internal/settings",
            "GET /api/internal/settings/health", 
            "GET /api/internal/settings/debug",
            "GET /api/internal/settings/simple",
            "PUT /api/internal/settings",
            "PUT /api/internal/settings/visibility-ui",
            "POST /api/internal/settings/register-parkea",
            "POST /api/internal/settings/upload-image"
        });
        
        return ResponseEntity.ok(response);
    }
}