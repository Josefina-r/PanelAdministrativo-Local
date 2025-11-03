package com.parkeaya.local_paneladmi.service;

import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ParkingService {
    private static final Logger logger = LoggerFactory.getLogger(ParkingService.class);

    @Value("${django.api.url}")
    private String djangoApiUrl;

    private final RestTemplate restTemplate;

    public ParkingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Crea headers comunes para todas las peticiones
     */
    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");
        return headers;
    }

    /**
     * Maneja excepciones de forma centralizada
     */
    private void handleException(String operation, Exception e) {
        if (e instanceof HttpClientErrorException) {
            HttpClientErrorException httpException = (HttpClientErrorException) e;
            logger.error("Error HTTP {} al {}: Status {}, Response: {}", 
                httpException.getStatusCode(), operation, 
                httpException.getStatusCode(), httpException.getResponseBodyAsString());
            throw new RuntimeException("Error del servidor: " + httpException.getStatusCode());
        } else if (e instanceof HttpServerErrorException) {
            logger.error("Error del servidor al {}: {}", operation, e.getMessage());
            throw new RuntimeException("Error interno del servidor. Intente más tarde.");
        } else if (e instanceof ResourceAccessException) {
            logger.error("Error de conexión al {}: {}", operation, e.getMessage());
            throw new RuntimeException("Error de conexión con el servidor. Verifique su conexión.");
        } else {
            logger.error("Error inesperado al {}: {}", operation, e.getMessage());
            throw new RuntimeException("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Intenta ejecutar una operación con reintentos
     */
    private <T> T executeWithRetry(Operation<T> operation, String operationName, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    handleException(operationName, e);
                }
                logger.warn("Intento {} fallido para {}. Reintentando...", attempt, operationName);
                try {
                    TimeUnit.SECONDS.sleep(2); // Espera 2 segundos entre reintentos
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Operación interrumpida");
                }
            }
        }
        throw new RuntimeException("Todos los reintentos fallaron para: " + operationName);
    }

    @FunctionalInterface
    private interface Operation<T> {
        T execute();
    }

    public List<DjangoParkingDTO> getOwnerParkings(String token) {
        logger.info("Obteniendo estacionamientos del propietario");
        
        return executeWithRetry(() -> {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            // URL CORREGIDA: quitamos /api extra
            String url = djangoApiUrl + "/parking/";
            logger.info("Haciendo request a: {}", url);
            
            ResponseEntity<List<DjangoParkingDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<DjangoParkingDTO>>() {}
            );
            
            List<DjangoParkingDTO> parkings = response.getBody();
            logger.info("Se obtuvieron {} estacionamientos", parkings != null ? parkings.size() : 0);
            return parkings != null ? parkings : Collections.emptyList();
            
        }, "obtener estacionamientos", 3);
    }

    public DjangoParkingDTO getParkingById(Long parkingId, String token) {
        logger.info("Obteniendo estacionamiento con ID: {}", parkingId);
        
        return executeWithRetry(() -> {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            // URL CORREGIDA: quitamos /api extra
            String url = djangoApiUrl + "/parking/" + parkingId + "/";
            logger.info("Haciendo request a: {}", url);
            
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                DjangoParkingDTO.class
            );
            
            DjangoParkingDTO parking = response.getBody();
            if (parking == null) {
                throw new RuntimeException("Estacionamiento no encontrado");
            }
            
            logger.info("Estacionamiento obtenido: {}", parking.getNombre());
            return parking;
            
        }, "obtener estacionamiento por ID", 3);
    }

    public DjangoParkingDTO createParking(DjangoParkingDTO parking, String token) {
        logger.info("Creando nuevo estacionamiento: {}", parking.getNombre());
        
        // Validación básica
        if (parking.getNombre() == null || parking.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del estacionamiento es requerido");
        }

        // Intentar obtener latitud/longitud mediante varios nombres de getters/fields (soporte por reflexión)
        Double lat = getCoordinateValue(parking,
            "getLatitud", "getLatitude", "getLat", "latitud", "latitude", "lat");
        Double lon = getCoordinateValue(parking,
            "getLongitud", "getLongitude", "getLng", "getLon", "longitud", "longitude", "lng", "lon");

        if (lat == null || lon == null) {
            throw new RuntimeException("Las coordenadas del estacionamiento son requeridas");
        }
        
        return executeWithRetry(() -> {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<DjangoParkingDTO> request = new HttpEntity<>(parking, headers);
            
            // URL CORREGIDA: quitamos /api extra
            String url = djangoApiUrl + "/parking/";
            logger.info("Haciendo request a: {}", url);
            
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                DjangoParkingDTO.class
            );
            
            DjangoParkingDTO createdParking = response.getBody();
            logger.info("Estacionamiento creado exitosamente: {} (ID: {})", 
                createdParking.getNombre(), createdParking.getId());
            return createdParking;
            
        }, "crear estacionamiento", 3);
    }

    private Double getCoordinateValue(DjangoParkingDTO parking, String... candidateNames) {
        if (parking == null) return null;
        Class<?> cls = parking.getClass();

        // Try getters
        for (String name : candidateNames) {
            try {
                java.lang.reflect.Method method = cls.getMethod(name);
                Object val = method.invoke(parking);
                if (val instanceof Number) {
                    return ((Number) val).doubleValue();
                }
                if (val instanceof String) {
                    String s = ((String) val).trim();
                    if (!s.isEmpty()) {
                        return Double.parseDouble(s);
                    }
                }
            } catch (NoSuchMethodException ignored) {
                // no such getter, try next
            } catch (Exception ignored) {
                // invocation failed, continue trying others
            }
        }

        // Try direct fields
        for (String name : candidateNames) {
            try {
                java.lang.reflect.Field field = cls.getDeclaredField(name);
                field.setAccessible(true);
                Object val = field.get(parking);
                if (val instanceof Number) {
                    return ((Number) val).doubleValue();
                }
                if (val instanceof String) {
                    String s = ((String) val).trim();
                    if (!s.isEmpty()) {
                        return Double.parseDouble(s);
                    }
                }
            } catch (NoSuchFieldException ignored) {
                // no such field, try next
            } catch (Exception ignored) {
                // access failed, continue trying others
            }
        }

        return null;
    }

    public DjangoParkingDTO updateParking(Long id, DjangoParkingDTO parking, String token) {
        logger.info("Actualizando estacionamiento ID: {}", id);
        
        return executeWithRetry(() -> {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<DjangoParkingDTO> request = new HttpEntity<>(parking, headers);
            
            // URL CORREGIDA: quitamos /api extra
            String url = djangoApiUrl + "/parking/" + id + "/";
            logger.info("Haciendo request a: {}", url);
            
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                DjangoParkingDTO.class
            );
            
            DjangoParkingDTO updatedParking = response.getBody();
            logger.info("Estacionamiento actualizado exitosamente: {} (ID: {})", 
                updatedParking.getNombre(), updatedParking.getId());
            return updatedParking;
            
        }, "actualizar estacionamiento", 3);
    }

    public void deleteParking(Long parkingId, String token) {
        logger.info("Eliminando estacionamiento ID: {}", parkingId);
        
        executeWithRetry(() -> {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            // URL CORREGIDA: quitamos /api extra
            String url = djangoApiUrl + "/parking/" + parkingId + "/";
            logger.info("Haciendo request a: {}", url);
            
            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Void.class
            );
            
            logger.info("Estacionamiento eliminado exitosamente: ID {}", parkingId);
            return null;
            
        }, "eliminar estacionamiento", 3);
    }

    public void uploadParkingImage(Long parkingId, MultipartFile image, Boolean esPrincipal, String token) {
        logger.info("Subiendo imagen para estacionamiento ID: {}", parkingId);

        if (image == null || image.isEmpty()) {
            throw new RuntimeException("La imagen es requerida");
        }

        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(token);
            headers.set("User-Agent", "ParkeYa-LocalPanel/1.0");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("imagen", image.getResource());
            if (esPrincipal != null) {
                body.add("es_principal", esPrincipal.toString());
            }

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            // URL CORREGIDA: quitamos /api extra
            String url = djangoApiUrl + "/parking/" + parkingId + "/upload_image/";
            logger.info("Haciendo request a: {}", url);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Void.class
            );

            logger.info("Imagen subida, status: {}", response.getStatusCode());
            return null;
        }, "subir imagen", 3);
    }

    public void updateAvailability(Long parkingId, Integer availableSpaces, String token) {
        logger.info("Actualizando disponibilidad del estacionamiento ID {} a {} espacios",
            parkingId, availableSpaces);

        if (availableSpaces == null || availableSpaces < 0) {
            throw new RuntimeException("El número de espacios disponibles debe ser mayor o igual a 0");
        }

        executeWithRetry(() -> {
            // Obtener el estacionamiento actual
            DjangoParkingDTO currentParking = getParkingById(parkingId, token);

            // Validar que no exceda la capacidad máxima
            if (availableSpaces > currentParking.getTotalPlazas()) {
                throw new RuntimeException("Los espacios disponibles no pueden exceder la capacidad total");
            }

            // Actualizar disponibilidad
            currentParking.setPlazasDisponibles(availableSpaces);
            updateParking(parkingId, currentParking, token);

            return null;
        }, "actualizar disponibilidad", 3);
    }

    /**
     * Método adicional: Verificar estado del servicio Django
     */
    public boolean checkDjangoServiceHealth() {
        try {
            // URL CORREGIDA: quitamos /api extra si es necesario
            String healthUrl = djangoApiUrl + "/health/";
            // Si health no existe, probar con una URL básica
            if (!healthUrl.contains("localhost:8000")) {
                healthUrl = djangoApiUrl;
            }
            
            ResponseEntity<String> response = restTemplate.getForEntity(
                healthUrl,
                String.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("El servicio Django no está disponible: {}", e.getMessage());
            return false;
        }
    }
}