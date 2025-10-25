package com.parkeaya.local_paneladmi.service;

import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
public class ParkingService {
    private static final Logger logger = LoggerFactory.getLogger(ParkingService.class);

    @Value("${django.api.url}")
    private String djangoApiUrl;

    private final RestTemplate restTemplate;

    public ParkingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<DjangoParkingDTO> getOwnerParkings(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List<DjangoParkingDTO>> response = restTemplate.exchange(
                djangoApiUrl + "/api/parking/",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<DjangoParkingDTO>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al obtener estacionamientos: {}", e.getMessage());
            throw new RuntimeException("Error al obtener estacionamientos: " + e.getMessage());
        }
    }

    public DjangoParkingDTO getParkingById(Long parkingId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                djangoApiUrl + "/api/parking/" + parkingId + "/",
                HttpMethod.GET,
                request,
                DjangoParkingDTO.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al obtener estacionamiento: {}", e.getMessage());
            throw new RuntimeException("Error al obtener estacionamiento: " + e.getMessage());
        }
    }

    public DjangoParkingDTO createParking(DjangoParkingDTO parking, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        HttpEntity<DjangoParkingDTO> request = new HttpEntity<>(parking, headers);
        
        try {
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                djangoApiUrl + "/api/parking/",
                HttpMethod.POST,
                request,
                DjangoParkingDTO.class
            );
            
            logger.info("Estacionamiento creado exitosamente: {}", parking.getNombre());
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al crear estacionamiento: {}", e.getMessage());
            throw new RuntimeException("Error al crear estacionamiento: " + e.getMessage());
        }
    }

    public DjangoParkingDTO updateParking(Long id, DjangoParkingDTO parking, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        HttpEntity<DjangoParkingDTO> request = new HttpEntity<>(parking, headers);
        
        try {
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                djangoApiUrl + "/api/parking/" + id + "/",
                HttpMethod.PUT,
                request,
                DjangoParkingDTO.class
            );
            
            logger.info("Estacionamiento actualizado exitosamente: {}", parking.getNombre());
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al actualizar estacionamiento: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar estacionamiento: " + e.getMessage());
        }
    }

    public void deleteParking(Long parkingId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            restTemplate.exchange(
                djangoApiUrl + "/api/parking/" + parkingId + "/",
                HttpMethod.DELETE,
                request,
                Void.class
            );
            
            logger.info("Estacionamiento eliminado exitosamente: ID {}", parkingId);
        } catch (Exception e) {
            logger.error("Error al eliminar estacionamiento: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar estacionamiento: " + e.getMessage());
        }
    }

    public void uploadParkingImage(Long parkingId, MultipartFile image, Boolean esPrincipal, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(token);
        
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("imagen", image.getResource());
            body.add("es_principal", esPrincipal.toString());
            body.add("parking_lot", parkingId.toString());
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            restTemplate.exchange(
                djangoApiUrl + "/parking-images/",
                HttpMethod.POST,
                requestEntity,
                Void.class
            );
            
            logger.info("Imagen subida exitosamente para el estacionamiento ID: {}", parkingId);
        } catch (Exception e) {
            logger.error("Error al subir imagen: {}", e.getMessage());
            throw new RuntimeException("Error al subir imagen: " + e.getMessage());
        }
    }

    public List<DjangoParkingDTO> searchParkings(String query, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List<DjangoParkingDTO>> response = restTemplate.exchange(
                djangoApiUrl + "/parking-lots/search/?q=" + query,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<DjangoParkingDTO>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al buscar estacionamientos: {}", e.getMessage());
            throw new RuntimeException("Error al buscar estacionamientos: " + e.getMessage());
        }
    }

    public void updateAvailability(Long parkingId, Integer availableSpaces, String token) {
        try {
            DjangoParkingDTO currentParking = getParkingById(parkingId, token);
            currentParking.setPlazasDisponibles(availableSpaces);
            updateParking(parkingId, currentParking, token);
            logger.info("Disponibilidad actualizada para el estacionamiento ID {}: {} espacios", parkingId, availableSpaces);
        } catch (Exception e) {
            logger.error("Error al actualizar disponibilidad: {}", e.getMessage());
            throw new RuntimeException("Error al actualizar disponibilidad: " + e.getMessage());
        }
    }
}