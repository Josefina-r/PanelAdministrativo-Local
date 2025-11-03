package com.parkeaya.local_paneladmi.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkeaya.local_paneladmi.service.DashboardService;
import com.parkeaya.local_paneladmi.service.DjangoIntegrationService;

import jakarta.servlet.http.HttpSession;

@RestController
// Añadido produces para forzar JSON y evitar respuestas inesperadas
@RequestMapping(path = "/api/parking", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*") // Para desarrollo, ajusta en producción
public class ParkingApiController {
    
    private static final Logger logger = LoggerFactory.getLogger(ParkingApiController.class);
    private final DashboardService dashboardService;
    private final DjangoIntegrationService djangoIntegrationService;
    
    public ParkingApiController(DashboardService dashboardService, 
                              DjangoIntegrationService djangoIntegrationService) {
        this.dashboardService = dashboardService;
        this.djangoIntegrationService = djangoIntegrationService;
    }
    
    // ✅ ENDPOINT PRINCIPAL: Guardar configuración y enviar solicitud de aprobación
    @PostMapping(path = "/save-and-register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveConfigurationAndRegister(
            @RequestBody Map<String, Object> parkingData,
            Principal principal, 
            HttpSession session) {
        try {
            logger.info("💾 POST /api/parking/save-and-register - Guardando configuración y enviando solicitud");
            logger.debug("Payload save-and-register: {}", parkingData);

            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            // Validar datos requeridos
            if (!parkingData.containsKey("name") || !parkingData.containsKey("address")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Nombre y dirección del estacionamiento son requeridos"
                ));
            }

            // 1. Primero guardar la configuración localmente
            var savedConfig = dashboardService.saveParkingConfiguration(token, parkingData);
            logger.info(" Configuración guardada localmente");

            // 2. Preparar datos para la solicitud de aprobación
            Map<String, Object> approvalRequest = new HashMap<>();
            approvalRequest.put("nombre", parkingData.get("name"));
            approvalRequest.put("direccion", parkingData.get("address"));
            approvalRequest.put("total_plazas", parkingData.get("totalSpaces"));
            approvalRequest.put("plazas_disponibles", parkingData.get("totalSpaces")); // Inicialmente todas disponibles
            approvalRequest.put("tarifa_hora", parkingData.get("hourlyRate"));
            approvalRequest.put("descripcion", parkingData.getOrDefault("description", ""));
            approvalRequest.put("coordenadas", parkingData.getOrDefault("coordinates", ""));
            approvalRequest.put("telefono", parkingData.getOrDefault("phone", ""));
            approvalRequest.put("horario_apertura", parkingData.getOrDefault("openingTime", "08:00"));
            approvalRequest.put("horario_cierre", parkingData.getOrDefault("closingTime", "22:00"));
            approvalRequest.put("nivel_seguridad", parkingData.getOrDefault("securityLevel", "MEDIO"));
            approvalRequest.put("servicios", parkingData.getOrDefault("services", new String[]{"Vigilancia"}));
            approvalRequest.put("notas_aprobacion", parkingData.getOrDefault("adminNotes", ""));
            approvalRequest.put("panel_local_id", "PANEL_LOCAL_" + System.currentTimeMillis());

            // 3. Enviar solicitud a Django
            var result = djangoIntegrationService.sendApprovalRequestToDjango(token, approvalRequest);
            if (result == null) {
                logger.warn("sendApprovalRequestToDjango devolvió null");
                return ResponseEntity.status(502).body(Map.of("error", "No response from approval service"));
            }
            
            Object externalId = result.get("id");
            logger.info("✅ Solicitud de aprobación enviada exitosamente. ID externo: {}", externalId);

            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Configuración guardada y solicitud de aprobación enviada",
                "local_config", savedConfig,
                "approval_request", Map.of(
                    "request_id", externalId != null ? externalId : "UNKNOWN",
                    "status", "PENDING",
                    "fecha_solicitud", result.getOrDefault("fecha_solicitud", "")
                )
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error en save-and-register: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al procesar la solicitud: " + e.getMessage()
            ));
        }
    }

    // ✅ ENDPOINT SIMPLIFICADO: Solo enviar solicitud de aprobación (para formularios existentes)
    @PostMapping(path = "/send-approval-request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendApprovalRequest(
            @RequestBody Map<String, Object> approvalRequest,
            Principal principal, 
            HttpSession session) {
        try {
            logger.info("📨 POST /api/parking/send-approval-request - Enviando solicitud");
            logger.debug("Payload send-approval-request: {}", approvalRequest);

            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            // Validar datos requeridos
            if (!approvalRequest.containsKey("name") || !approvalRequest.containsKey("address")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Nombre y dirección del estacionamiento son requeridos"
                ));
            }

            // Mapear datos al formato de Django
            Map<String, Object> djangoRequest = new HashMap<>();
            djangoRequest.put("nombre", approvalRequest.get("name"));
            djangoRequest.put("direccion", approvalRequest.get("address"));
            djangoRequest.put("total_plazas", approvalRequest.get("totalSpaces"));
            djangoRequest.put("plazas_disponibles", approvalRequest.get("totalSpaces"));
            djangoRequest.put("tarifa_hora", approvalRequest.get("hourlyRate"));
            djangoRequest.put("descripcion", approvalRequest.getOrDefault("description", ""));
            djangoRequest.put("coordenadas", approvalRequest.getOrDefault("coordinates", ""));
            djangoRequest.put("telefono", approvalRequest.getOrDefault("phone", ""));
            djangoRequest.put("horario_apertura", approvalRequest.getOrDefault("openingTime", "08:00"));
            djangoRequest.put("horario_cierre", approvalRequest.getOrDefault("closingTime", "22:00"));
            djangoRequest.put("nivel_seguridad", approvalRequest.getOrDefault("securityLevel", "MEDIO"));
            djangoRequest.put("servicios", approvalRequest.getOrDefault("services", new String[]{"Vigilancia"}));
            djangoRequest.put("notas_aprobacion", approvalRequest.getOrDefault("adminNotes", ""));
            djangoRequest.put("panel_local_id", approvalRequest.getOrDefault("panelLocalId", "PANEL_LOCAL_" + System.currentTimeMillis()));

            // Enviar solicitud a Django
            var result = djangoIntegrationService.sendApprovalRequestToDjango(token, djangoRequest);
            if (result == null) {
                logger.warn("sendApprovalRequestToDjango devolvió null");
                return ResponseEntity.status(502).body(Map.of("error", "No response from approval service"));
            }
            
            Object id = result.get("id");
            logger.info("✅ Solicitud enviada. ID: {}", id);
            return ResponseEntity.ok(Map.of(
                "status", "PENDING",
                "message", "Solicitud de aprobación enviada correctamente",
                "request_id", id != null ? id : "UNKNOWN",
                "fecha_solicitud", result.getOrDefault("fecha_solicitud", "")
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error enviando solicitud de aprobación: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al enviar solicitud de aprobación: " + e.getMessage()
            ));
        }
    }
    
    // ✅ OBTENER CONFIGURACIÓN ACTUAL
    @GetMapping("/settings")
    public ResponseEntity<?> getParkingSettings(Principal principal, HttpSession session) {
        try {
            logger.info("⚙️ Obteniendo configuración actual del estacionamiento");
            
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            var config = dashboardService.getParkingConfiguration(token);
            
            // Agregar estado de aprobación si existe
            var approvalStatus = djangoIntegrationService.getLatestApprovalStatus(token);
            if (approvalStatus != null) {
                config.put("approval_status", approvalStatus);
            }
            
            logger.info("✅ Configuración obtenida exitosamente");
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo configuración: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al obtener configuración: " + e.getMessage()
            ));
        }
    }
    
    // ✅ GUARDAR CONFIGURACIÓN (sin enviar aprobación)
    @PutMapping("/settings")
    public ResponseEntity<?> saveParkingSettings(
            @RequestBody Map<String, Object> parkingData,
            Principal principal, 
            HttpSession session) {
        try {
            logger.info("💾 Guardando configuración del estacionamiento");
            
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            var savedConfig = dashboardService.saveParkingConfiguration(token, parkingData);
            
            logger.info("✅ Configuración guardada exitosamente");
            return ResponseEntity.ok(savedConfig);
            
        } catch (Exception e) {
            logger.error("❌ Error guardando configuración: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al guardar configuración: " + e.getMessage()
            ));
        }
    }
    
    // ✅ Verificar estado de la solicitud de aprobación
    @GetMapping("/approval-status/{requestId}")
    public ResponseEntity<?> getApprovalStatus(
            @PathVariable String requestId,
            Principal principal, 
            HttpSession session) {
        try {
            logger.info("📊 Consultando estado de aprobación para solicitud: {}", requestId);
            
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            var status = djangoIntegrationService.getApprovalStatus(token, requestId);
            
            logger.info("✅ Estado de aprobación obtenido: {}", status.get("estado"));
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("❌ Error consultando estado de aprobación: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al consultar estado: " + e.getMessage()
            ));
        }
    }
    
    // ✅ Listar solicitudes de aprobación del panel local
    @GetMapping("/approval-requests")
    public ResponseEntity<?> getApprovalRequests(Principal principal, HttpSession session) {
        try {
            logger.info("📋 Obteniendo historial de solicitudes de aprobación");
            
            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            var requests = djangoIntegrationService.getApprovalRequests(token);
            
            logger.info("✅ Historial de solicitudes obtenido: {} solicitudes", requests.size());
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            logger.error("❌ Error obteniendo historial de solicitudes: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al obtener historial: " + e.getMessage()
            ));
        }
    }
    
    // ✅ Registro con Parkea - Versión mejorada
    @PostMapping(path = "/register-parkea", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerWithParkea(
            @RequestBody(required = false) Map<String, Object> additionalData,
            Principal principal, 
            HttpSession session) {
        try {
            logger.info("📱 POST /api/parking/register-parkea - Registrando con Parkea");
            logger.debug("Payload register-parkea: {}", additionalData);

            if (principal == null) {
                return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
            }

            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token no encontrado"));
            }

            // Obtener configuración actual
            var parkingConfig = dashboardService.getParkingConfiguration(token);
            
            if (parkingConfig == null || parkingConfig.get("name") == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No hay configuración de estacionamiento guardada"
                ));
            }

            // Preparar datos para la solicitud
            Map<String, Object> approvalRequest = new HashMap<>();
            approvalRequest.put("nombre", parkingConfig.get("name"));
            approvalRequest.put("direccion", parkingConfig.get("address"));
            approvalRequest.put("total_plazas", parkingConfig.get("totalSpaces"));
            approvalRequest.put("plazas_disponibles", parkingConfig.get("totalSpaces"));
            approvalRequest.put("tarifa_hora", parkingConfig.get("hourlyRate"));
            approvalRequest.put("descripcion", parkingConfig.getOrDefault("description", ""));
            approvalRequest.put("coordenadas", parkingConfig.getOrDefault("coordinates", ""));
            approvalRequest.put("telefono", parkingConfig.getOrDefault("phone", ""));
            approvalRequest.put("horario_apertura", parkingConfig.getOrDefault("openingTime", "08:00"));
            approvalRequest.put("horario_cierre", parkingConfig.getOrDefault("closingTime", "22:00"));
            approvalRequest.put("nivel_seguridad", parkingConfig.getOrDefault("securityLevel", "MEDIO"));
            approvalRequest.put("servicios", parkingConfig.getOrDefault("services", new String[]{"Vigilancia"}));
            approvalRequest.put("notas_aprobacion", additionalData != null ? additionalData.get("notes") : "");
            approvalRequest.put("panel_local_id", "PANEL_LOCAL_" + System.currentTimeMillis());

            // Enviar solicitud
            var result = djangoIntegrationService.sendApprovalRequestToDjango(token, approvalRequest);
            if (result == null) {
                logger.warn("sendApprovalRequestToDjango devolvió null");
                return ResponseEntity.status(502).body(Map.of("error", "No response from approval service"));
            }
            
            Object reqId = result.get("id");
            logger.info("✅ Solicitud de registro enviada. ID: {}", reqId);
            return ResponseEntity.ok(Map.of(
                "status", "PENDING",
                "message", "Solicitud de registro enviada para aprobación",
                "request_id", reqId != null ? reqId : "UNKNOWN",
                "fecha_solicitud", result.getOrDefault("fecha_solicitud", ""),
                "next_step", "Esperar aprobación del panel general"
            ));
            
        } catch (Exception e) {
            logger.error("❌ Error enviando solicitud de registro: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Error al enviar solicitud de registro: " + e.getMessage()
            ));
        }
    }
    
    // ✅ Webhook para recibir actualizaciones de Django
    @PostMapping("/webhook/approval-update")
    public ResponseEntity<?> handleApprovalUpdate(
            @RequestBody Map<String, Object> updateData) {
        try {
            logger.info("🔄 Recibiendo actualización de aprobación desde Django: {}", updateData);
            
            String requestId = (String) updateData.get("request_id");
            String status = (String) updateData.get("estado");
            String motivo = (String) updateData.get("motivo");
            
            if (requestId == null || status == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Datos incompletos"));
            }
            
            // Procesar la actualización
            djangoIntegrationService.processApprovalUpdate(requestId, status, motivo);
            
            logger.info("✅ Actualización de aprobación procesada: {} - {}", requestId, status);
            return ResponseEntity.ok(Map.of("status", "processed"));
            
        } catch (Exception e) {
            logger.error("❌ Error procesando actualización de aprobación: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error procesando actualización"));
        }
    }

    // ✅ ENDPOINT DE HEALTH CHECK
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "local-paneladmi",
            "timestamp", System.currentTimeMillis()
        ));
    }
}