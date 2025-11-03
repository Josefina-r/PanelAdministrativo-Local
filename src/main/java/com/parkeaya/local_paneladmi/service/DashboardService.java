package com.parkeaya.local_paneladmi.service;

import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Value("${django.api.base-url:http://localhost:8000/api}")
    private String djangoApiUrl;

    private final RestTemplate restTemplate;

    public DashboardService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ==================== MÉTODOS DE CONFIGURACIÓN ====================

    public Map<String, Object> getParkingConfiguration(String token) {
        logger.info("🔍 INICIANDO getParkingConfiguration");
        
        try {
            // Obtener todos los parkings del dueño
            List<DjangoParkingDTO> ownerParkings = getOwnerParkings(token);
            
            if (ownerParkings.isEmpty()) {
                logger.warn("No se encontraron parkings para el usuario");
                return createEmptyParkingConfig();
            }
            
         
            DjangoParkingDTO parking = ownerParkings.get(0);
            logger.info("✅ Parking encontrado: {}", parking.getNombre());
            
            // Mapear a la estructura esperada por el frontend
            Map<String, Object> config = new HashMap<>();
            config.put("id", parking.getId() != null ? parking.getId().toString() : "No asignado");
            config.put("name", parking.getNombre());
            config.put("address", parking.getDireccion());
            config.put("totalSpaces", parking.getTotalPlazas() != null ? parking.getTotalPlazas() : 0);
            config.put("hourlyRate", parking.getPrecioHora() != null ? parking.getPrecioHora().doubleValue() : 0.0);
            config.put("description", ""); // No existe en DTO
            config.put("isVisible", parking.getActivo() != null ? parking.getActivo() : false);
            config.put("registrationStatus", determineRegistrationStatus(parking));
            config.put("imageUrl", ""); // No existe en DTO
            config.put("ownerId", parking.getDueno()); // ✅ Campo agregado
            
            logger.info("✅ Configuración obtenida exitosamente para: {}", parking.getNombre());
            return config;
            
        } catch (Exception e) {
            logger.error("❌ ERROR en getParkingConfiguration: {}", e.getMessage(), e);
            return createEmptyParkingConfig();
        }
    }

    public Map<String, Object> updateParkingSettings(String token, Map<String, Object> settings) {
        logger.info(" INICIANDO updateParkingSettings");
        
        try {
            // Obtener el ID del parking
            List<DjangoParkingDTO> ownerParkings = getOwnerParkings(token);
            if (ownerParkings.isEmpty()) {
                throw new RuntimeException("No se encontraron parkings para actualizar");
            }
            
            DjangoParkingDTO firstParking = ownerParkings.get(0);
            Long parkingId = firstParking.getId();
            
            // Preparar los datos para la actualización según tu DTO
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("nombre", settings.get("name"));
            updateData.put("direccion", settings.get("address"));
            updateData.put("total_plazas", settings.get("totalSpaces"));
            updateData.put("tarifa_hora", settings.get("hourlyRate"));
            updateData.put("activo", settings.get("isVisible")); // ✅ Campo agregado
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateData, headers);
            
            String url = djangoApiUrl + "/parking/" + parkingId + "/";
            logger.info("🌐 Actualizando parking en: {}", url);
            logger.info("📝 Datos a actualizar: {}", updateData);
            
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                DjangoParkingDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Configuración actualizada exitosamente");
                result.put("status", "success");
                logger.info("✅ Parking actualizado exitosamente");
                return result;
            } else {
                throw new RuntimeException("Error en la respuesta del servidor: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR en updateParkingSettings: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al actualizar configuración: " + e.getMessage());
            return error;
        }
    }

    public Map<String, Object> updateParkingVisibility(String token, Boolean isVisible) {
        logger.info(" INICIANDO updateParkingVisibility: {}", isVisible);
        
        try {
            // Obtener el ID del parking
            List<DjangoParkingDTO> ownerParkings = getOwnerParkings(token);
            if (ownerParkings.isEmpty()) {
                throw new RuntimeException("No se encontraron parkings para actualizar");
            }
            
            DjangoParkingDTO firstParking = ownerParkings.get(0);
            Long parkingId = firstParking.getId();
            
            // Preparar datos para actualizar visibilidad
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("activo", isVisible);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateData, headers);
            
            String url = djangoApiUrl + "/parking/" + parkingId + "/";
            logger.info(" Actualizando visibilidad en: {}", url);
            
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                DjangoParkingDTO.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Visibilidad actualizada exitosamente");
                result.put("status", "success");
                logger.info("✅ Visibilidad actualizada a: {}", isVisible);
                return result;
            } else {
                throw new RuntimeException("Error en la respuesta del servidor: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR en updateParkingVisibility: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al actualizar visibilidad: " + e.getMessage());
            return error;
        }
    }

    public Map<String, Object> registerWithParkea(String token) {
        logger.info("📝 INICIANDO registerWithParkea");
        
        try {
            // Obtener datos del parking actual
            List<DjangoParkingDTO> parkings = getOwnerParkings(token);
            if (parkings.isEmpty()) {
                throw new RuntimeException("No hay estacionamiento configurado para registrar");
            }
            DjangoParkingDTO parking = parkings.get(0);

            // Preparar payload para registro
            Map<String, Object> registrationData = new HashMap<>();
            registrationData.put("parking_id", parking.getId());
            registrationData.put("nombre", parking.getNombre());
            registrationData.put("direccion", parking.getDireccion());
            registrationData.put("total_plazas", parking.getTotalPlazas());
            registrationData.put("precio_hora", parking.getPrecioHora());
            registrationData.put("status", "PENDING");
            registrationData.put("fecha_solicitud", new Date());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(registrationData, headers);

            // Usar /api/parking/register o /api/parking/{id}/register que sí existe en Django
            String url = djangoApiUrl + "/parking/" + parking.getId() + "/register";
            logger.info("🌐 Enviando solicitud de registro a: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Solicitud de registro enviada exitosamente");
                result.put("status", "PENDING");
                result.put("request_id", response.getBody() != null ? response.getBody().get("id") : null);
                result.put("parking_id", parking.getId());
                logger.info("✅ Registro enviado exitosamente para parking ID: {}", parking.getId());
                return result;
            } else {
                throw new RuntimeException("Error en respuesta: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR en registerWithParkea: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al registrar con Parkea: " + e.getMessage());
            error.put("status", "ERROR");
            return error;
        }
    }

    public Map<String, Object> uploadParkingImage(String token, MultipartFile file) {
        logger.info(" INICIANDO uploadParkingImage: {}", file.getOriginalFilename());
        
        try {
            // Obtener el ID del parking
            List<DjangoParkingDTO> ownerParkings = getOwnerParkings(token);
            if (ownerParkings.isEmpty()) {
                throw new RuntimeException("No se encontraron parkings para actualizar");
            }
            
            // Simular subida de imagen
            String imageUrl = "/media/parking/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Imagen subida exitosamente");
            result.put("imageUrl", imageUrl);
            
            logger.info("✅ Imagen subida exitosamente: {}", imageUrl);
            return result;
            
        } catch (Exception e) {
            logger.error("❌ ERROR en uploadParkingImage: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error al subir imagen: " + e.getMessage());
            return error;
        }
    }

    public Map<String, Object> saveParkingConfiguration(String token, Map<String, Object> parkingData) {
        logger.info("💾 INICIANDO saveParkingConfiguration");
        
        try {
            // Validar datos requeridos
            if (!parkingData.containsKey("name") || !parkingData.containsKey("address")) {
                throw new RuntimeException("Nombre y dirección son requeridos");
            }

            // Obtener parkings existentes para determinar si es creación o actualización
            List<DjangoParkingDTO> existingParkings = getOwnerParkings(token);
            
            if (existingParkings.isEmpty()) {
                // CREAR NUEVO PARKING
                return createNewParking(token, parkingData);
            } else {
                // ACTUALIZAR PARKING EXISTENTE
                return updateExistingParking(token, existingParkings.get(0).getId(), parkingData);
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR en saveParkingConfiguration: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar configuración: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS DEL DASHBOARD ====================

    public Map<String, Object> getDashboardStats(String token) {
        logger.info(" INICIANDO getDashboardStats");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            logger.info("📊 Obteniendo parkings del dueño...");
            List<DjangoParkingDTO> ownerParkings = getOwnerParkings(token);
            logger.info("✅ Parkings obtenidos: {}", ownerParkings.size());
            
            // Calcular estadísticas básicas
            int totalParkings = ownerParkings.size();
            long activeParkings = ownerParkings.stream()
                .filter(p -> p != null && p.getActivo() != null && p.getActivo())
                .count();
            int totalSpaces = ownerParkings.stream()
                .filter(Objects::nonNull)
                .mapToInt(p -> p.getTotalPlazas() != null ? p.getTotalPlazas() : 0)
                .sum();
            int availableSpaces = ownerParkings.stream()
                .filter(Objects::nonNull)
                .mapToInt(p -> p.getPlazasDisponibles() != null ? p.getPlazasDisponibles() : 0)
                .sum();
            int occupiedSpaces = totalSpaces - availableSpaces;
            
            // Poblar estadísticas
            stats.put("totalParkings", totalParkings);
            stats.put("activeParkings", activeParkings);
            stats.put("totalSpaces", totalSpaces);
            stats.put("availableSpaces", availableSpaces);
            stats.put("occupiedSpaces", occupiedSpaces);
            stats.put("activeReservations", 0);
            stats.put("todayRevenue", 0.0);
            stats.put("monthlyRevenue", 0.0);
            
            logger.info(" Estadísticas generadas: totalParkings={}, totalSpaces={}", totalParkings, totalSpaces);
            
        } catch (Exception e) {
            logger.error(" ERROR en getDashboardStats: {}", e.getMessage(), e);
            stats = createDefaultStats();
        }
        
        logger.info("🔚 FINALIZANDO getDashboardStats");
        return stats;
    }

    public List<DjangoParkingDTO> getOwnerParkings(String token) {
        logger.info(" INICIANDO getOwnerParkings");
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            String url = djangoApiUrl + "/parking/";
            logger.info(" Haciendo request a: {}", url);
            
            ResponseEntity<List<DjangoParkingDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<DjangoParkingDTO>>() {}
            );
            
            List<DjangoParkingDTO> parkings = response.getBody() != null ? response.getBody() : Collections.emptyList();
            logger.info("✅ getOwnerParkings exitoso: {} parkings", parkings.size());
            return parkings;
            
        } catch (Exception e) {
            logger.error(" ERROR en getOwnerParkings: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<Object> getRecentReservations(String token) {
        logger.info(" getRecentReservations - retornando lista vacía");
        return Collections.emptyList();
    }

    // ==================== MÉTODOS PRIVADOS AUXILIARES ====================

    private Map<String, Object> createNewParking(String token, Map<String, Object> parkingData) {
        logger.info(" Creando nuevo parking");
        
        try {
            // Preparar datos para crear nuevo parking según tu DTO EXACTO
            Map<String, Object> createData = new HashMap<>();
            createData.put("nombre", parkingData.get("name"));
            createData.put("direccion", parkingData.get("address"));
            createData.put("total_plazas", parkingData.get("totalSpaces"));
            createData.put("plazas_disponibles", parkingData.get("totalSpaces")); // Inicialmente todas disponibles
            createData.put("precio_hora", parkingData.get("hourlyRate"));
            createData.put("activo", parkingData.getOrDefault("isVisible", false));
            // NO incluir campos que no existen en tu DTO
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(createData, headers);

            String url = djangoApiUrl + "/parking/";
            logger.info(" Creando nuevo parking en: {}", url);
            logger.info(" Datos de creación: {}", createData);

            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                DjangoParkingDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                DjangoParkingDTO createdParking = response.getBody();
                
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Parking creado exitosamente");
                result.put("status", "success");
                result.put("parkingId", createdParking.getId());
                result.put("name", createdParking.getNombre());
                result.put("address", createdParking.getDireccion());
                
                logger.info("✅ Nuevo parking creado exitosamente: {}", createdParking.getNombre());
                return result;
            } else {
                throw new RuntimeException("Error al crear parking: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("❌ ERROR creando nuevo parking: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear parking: " + e.getMessage());
        }
    }

    private Map<String, Object> updateExistingParking(String token, Long parkingId, Map<String, Object> parkingData) {
        logger.info(" Actualizando parking existente ID: {}", parkingId);
        
        try {
            // Preparar datos para actualización - SOLO CAMPOS EXISTENTES
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("nombre", parkingData.get("name"));
            updateData.put("direccion", parkingData.get("address"));
            updateData.put("total_plazas", parkingData.get("totalSpaces"));
            updateData.put("precio_hora", parkingData.get("hourlyRate"));
            updateData.put("activo", parkingData.getOrDefault("isVisible", false));
            // NO incluir campos que no existen en tu DTO

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(updateData, headers);

            String url = djangoApiUrl + "/parking/" + parkingId + "/";
            logger.info(" Actualizando parking en: {}", url);
            logger.info(" Datos de actualización: {}", updateData);

            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                DjangoParkingDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Configuración actualizada exitosamente");
                result.put("status", "success");
                result.put("parkingId", parkingId);
                result.put("name", parkingData.get("name"));
                result.put("address", parkingData.get("address"));
                
                logger.info(" Parking actualizado exitosamente ID: {}", parkingId);
                return result;
            } else {
                throw new RuntimeException("Error al actualizar parking: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("❌ ERROR actualizando parking: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar parking: " + e.getMessage());
        }
    }

    private Map<String, Object> createEmptyParkingConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("id", "No asignado");
        config.put("name", "");
        config.put("address", "");
        config.put("totalSpaces", 0);
        config.put("hourlyRate", 0.0);
        config.put("description", "");
        config.put("isVisible", false);
        config.put("registrationStatus", "NOT_REGISTERED");
        config.put("imageUrl", "");
        config.put("ownerId", null);
        return config;
    }

    private String determineRegistrationStatus(DjangoParkingDTO parking) {
        // Lógica para determinar el estado de registro
        if (parking.getActivo() != null && parking.getActivo()) {
            return "ACTIVE";
        } else {
            return "NOT_REGISTERED";
        }
    }

    private Map<String, Object> createDefaultStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalParkings", 0);
        stats.put("activeParkings", 0);
        stats.put("totalSpaces", 0);
        stats.put("availableSpaces", 0);
        stats.put("occupiedSpaces", 0);
        stats.put("activeReservations", 0);
        stats.put("todayRevenue", 0.0);
        stats.put("monthlyRevenue", 0.0);
        return stats;
    }
}