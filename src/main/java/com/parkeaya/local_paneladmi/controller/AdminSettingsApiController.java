package com.parkeaya.local_paneladmi.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkeaya.local_paneladmi.service.SettingsService;

@RestController
@RequestMapping("/api/admin/settings")
@CrossOrigin(origins = "*")
public class AdminSettingsApiController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSettingsApiController.class);
    private final SettingsService settingsService;

    // ✅ INYECTA EL SETTINGS SERVICE QUE YA TIENES
    public AdminSettingsApiController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    // ✅ ENDPOINT CORREGIDO - USA EL SERVICE EXISTENTE
    @PostMapping("/register-parkea")
    public ResponseEntity<?> registerWithParkea(@RequestBody Map<String, Object> requestData) {
        logger.info("📱 POST /api/admin/settings/register-parkea - Registrando con Parkea");
        logger.info("📦 Datos recibidos: {}", requestData);
        
        try {
            // ✅ USA EL MÉTODO registerWithParkea QUE YA TIENE LA INTEGRACIÓN CON DJANGO
            Map<String, Object> result = settingsService.registerWithParkea(requestData);
            
            logger.info("✅ Solicitud procesada exitosamente - ID: {}", result.get("request_id"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en registerWithParkea: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    // ✅ Endpoint para obtener configuración - USA EL SERVICE EXISTENTE
    @GetMapping("")
    public ResponseEntity<?> getSettings() {
        logger.info("🎯 GET /api/admin/settings - Obteniendo configuración");
        
        try {
            Map<String, Object> settings = settingsService.getParkingSettings();
            logger.info("✅ Configuración obtenida exitosamente");
            return ResponseEntity.ok(settings);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en getSettings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al obtener configuración: " + e.getMessage()));
        }
    }

    // ✅ Endpoint para guardar configuración - USA EL SERVICE EXISTENTE
    @PutMapping("")
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, Object> settings) {
        logger.info("💾 PUT /api/admin/settings - Guardando configuración: {}", settings.keySet());
        
        try {
            Map<String, Object> result = settingsService.saveSettings(settings);
            logger.info("✅ Configuración guardada exitosamente");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en saveSettings: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al guardar configuración: " + e.getMessage()));
        }
    }

    // ✅ Endpoint para actualizar visibilidad - USA EL SERVICE EXISTENTE
    @PutMapping("/visibility")
    public ResponseEntity<?> updateVisibility(@RequestBody Map<String, Boolean> request) {
        logger.info("👁️ PUT /api/admin/settings/visibility - Actualizando visibilidad: {}", request);
        
        try {
            if (request.get("isVisible") == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("El campo 'isVisible' es requerido"));
            }

            Map<String, Object> result = settingsService.updateVisibility(request.get("isVisible"));
            logger.info("✅ Visibilidad actualizada exitosamente a: {}", request.get("isVisible"));
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en updateVisibility: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al actualizar visibilidad: " + e.getMessage()));
        }
    }

    // ✅ Endpoint para subir imagen - USA EL SERVICE EXISTENTE
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestBody Map<String, String> request) {
        logger.info("🖼️ POST /api/admin/settings/upload-image - Subiendo imagen");
        
        try {
            // En una implementación real usarías MultipartFile
            // Por ahora simulamos una respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Endpoint de imagen listo - usar MultipartFile en implementación real");
            response.put("imageUrl", "/images/placeholder.jpg");
            
            logger.info("✅ Endpoint de imagen respondiendo");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ ERROR en uploadImage: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al subir imagen: " + e.getMessage()));
        }
    }

    // ✅ Método auxiliar para respuestas de error
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", java.time.LocalDateTime.now().toString());
        return error;
    }

    // ✅ Endpoint de health check
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("service", "AdminSettingsApiController");
        response.put("status", "OK");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("message", "Servicio de configuración administrativa funcionando");
        response.put("usingSettingsService", "YES");
        return ResponseEntity.ok(response);
    }

    // ✅ Endpoint para probar la conexión con Django
    @GetMapping("/test-django-connection")
    public ResponseEntity<?> testDjangoConnection() {
        try {
            // Obtener configuración forzará la conexión con Django
            Map<String, Object> settings = settingsService.getParkingSettings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Conexión con Django funcionando correctamente");
            response.put("djangoUrl", "Configurado en SettingsService");
            response.put("localParkingId", "LOCAL_PARKING_001");
            response.put("sampleData", settings.get("name"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error en conexión con Django: " + e.getMessage());
            response.put("djangoUrl", "Verificar configuración");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}