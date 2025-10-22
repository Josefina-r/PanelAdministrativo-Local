package com.parkeaya.local_paneladmi.service;

import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class ParkingService {

    @Value("${django.api.url}")
    private String djangoApiUrl;

    private final RestTemplate restTemplate;

    public ParkingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<DjangoParkingDTO> getParkingsByOwnerEmail(String email) {
        try {
            // Obtener todos los parkings
            String url = djangoApiUrl + "/parking/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<DjangoParkingDTO[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                DjangoParkingDTO[].class
            );
            
            // Filtrar por dueño (usando el campo 'dueno' que viene de Django)
            Long ownerId = getOwnerIdByEmail(email);
            if (response.getBody() != null) {
                return Arrays.stream(response.getBody())
                    .filter(parking -> parking.getDueno() != null && parking.getDueno().equals(ownerId))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            System.err.println("Error al obtener parkings: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public DjangoParkingDTO getParkingByIdAndOwner(Long parkingId, String email) {
        try {
            String url = djangoApiUrl + "/parking/" + parkingId + "/";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                DjangoParkingDTO.class
            );
            
            // Verificar propiedad
            Long ownerId = getOwnerIdByEmail(email);
            DjangoParkingDTO parking = response.getBody();
            if (parking != null && !parking.getDueno().equals(ownerId)) {
                throw new RuntimeException("No tienes permisos para acceder a este parking");
            }
            
            return parking;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener parking: " + e.getMessage());
        }
    }

    public DjangoParkingDTO createParking(DjangoParkingDTO parkingDTO, String email) {
        try {
            Long ownerId = getOwnerIdByEmail(email);
            // El campo en Django es 'dueno', no 'ownerId'
            // Django se encargará de asignar el dueño basado en la autenticación
            return createParkingInDjango(parkingDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error al crear parking: " + e.getMessage());
        }
    }

    public DjangoParkingDTO updateParking(Long parkingId, DjangoParkingDTO parkingDTO, String email) {
        try {
            // Verificar propiedad primero
            getParkingByIdAndOwner(parkingId, email);
            return updateParkingInDjango(parkingId, parkingDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar parking: " + e.getMessage());
        }
    }

    public void deleteParking(Long parkingId, String email) {
        try {
            // Verificar propiedad primero
            getParkingByIdAndOwner(parkingId, email);
            deleteParkingInDjango(parkingId);
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar parking: " + e.getMessage());
        }
    }

    public void updateAvailability(Long parkingId, Integer availableSpaces, String email) {
        try {
            // Verificar propiedad primero
            DjangoParkingDTO currentParking = getParkingByIdAndOwner(parkingId, email);
            currentParking.setPlazasDisponibles(availableSpaces);
            updateParkingInDjango(parkingId, currentParking);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar disponibilidad: " + e.getMessage());
        }
    }

    // ========== MÉTODOS PRIVADOS ==========

    private DjangoParkingDTO createParkingInDjango(DjangoParkingDTO parkingDTO) {
        String url = djangoApiUrl + "/parking/";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<DjangoParkingDTO> entity = new HttpEntity<>(parkingDTO, headers);
        
        ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            entity,
            DjangoParkingDTO.class
        );
        
        return response.getBody();
    }

    private DjangoParkingDTO updateParkingInDjango(Long parkingId, DjangoParkingDTO parkingDTO) {
        String url = djangoApiUrl + "/parking/" + parkingId + "/";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<DjangoParkingDTO> entity = new HttpEntity<>(parkingDTO, headers);
        
        ResponseEntity<DjangoParkingDTO> response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            entity,
            DjangoParkingDTO.class
        );
        
        return response.getBody();
    }

    private void deleteParkingInDjango(Long parkingId) {
        String url = djangoApiUrl + "/parking/" + parkingId + "/";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    // Método temporal - necesitas implementar la lógica real
    private Long getOwnerIdByEmail(String email) {
        // Por ahora retornamos un ID fijo
        // En producción, necesitas consultar tu API de Django para mapear email → ownerId
        return 1L;
    }
}