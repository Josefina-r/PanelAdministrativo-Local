package com.parkeaya.local_paneladmi.service;

import com.parkeaya.local_paneladmi.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Value("${django.api.url}")
    private String djangoApiUrl;

    private final RestTemplate restTemplate;
    private final ParkingService parkingService;

    public DashboardService(RestTemplate restTemplate, ParkingService parkingService) {
        this.restTemplate = restTemplate;
        this.parkingService = parkingService;
    }

    /**
     * Obtiene los estacionamientos del dueño
     */
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
            
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Error al obtener estacionamientos: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene estadísticas del dashboard para el dueño
     */
    public OwnerDashboardStatsDTO getDashboardStats(String token) {
        try {
            // Obtener parkings del dueño usando el token JWT
            List<DjangoParkingDTO> ownerParkings = getOwnerParkings(token);
            
            // Calcular estadísticas básicas
            OwnerDashboardStatsDTO stats = new OwnerDashboardStatsDTO();
            stats.setTotalParkings(ownerParkings.size());
            stats.setTotalSpaces(ownerParkings.stream().mapToInt(p -> p.getTotalPlazas() != null ? p.getTotalPlazas() : 0).sum());
            stats.setAvailableSpaces(ownerParkings.stream().mapToInt(p -> p.getPlazasDisponibles() != null ? p.getPlazasDisponibles() : 0).sum());
            
            // Obtener métricas desde la API
            stats.setActiveReservations(getActiveReservationsCount(token));
            stats.setTodayRevenue(getTodayRevenue(token));
            stats.setMonthlyRevenue(getMonthlyRevenue(token));
            
            // Obtener reservas recientes
            stats.setRecentReservations(getRecentReservations(token));
            
            // Estadísticas por parking
            stats.setParkingStats(calculateParkingStats(ownerParkings));
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas del dashboard: {}", e.getMessage(), e);
            return createDefaultStats();
        }
    }

    /**
     * Obtiene reservas recientes
     */
    public List<RecentReservationDTO> getRecentReservations(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<List<RecentReservationDTO>> response = restTemplate.exchange(
                djangoApiUrl + "/reservations/recent/",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<List<RecentReservationDTO>>() {}
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error al obtener reservas recientes: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private Integer getActiveReservationsCount(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<Integer> response = restTemplate.exchange(
                djangoApiUrl + "/reservations/active/count/",
                HttpMethod.GET,
                request,
                Integer.class
            );
            
            return response.getBody() != null ? response.getBody() : 0;
        } catch (Exception e) {
            logger.error("Error al obtener conteo de reservas activas: {}", e.getMessage(), e);
            return 0;
        }
    }

    private BigDecimal getTodayRevenue(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(
                djangoApiUrl + "/reservations/revenue/today/",
                HttpMethod.GET,
                request,
                BigDecimal.class
            );
            
            return response.getBody() != null ? response.getBody() : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error al obtener ingresos del día: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getMonthlyRevenue(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(
                djangoApiUrl + "/reservations/revenue/monthly/",
                HttpMethod.GET,
                request,
                BigDecimal.class
            );
            
            return response.getBody() != null ? response.getBody() : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Error al obtener ingresos mensuales: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }

    private List<ParkingStatsDTO> calculateParkingStats(List<DjangoParkingDTO> parkings) {
        return parkings.stream().map(parking -> {
            ParkingStatsDTO stats = new ParkingStatsDTO();
            stats.setParkingId(parking.getId());
            stats.setParkingName(parking.getNombre());
            stats.setTotalSpaces(parking.getTotalPlazas() != null ? parking.getTotalPlazas() : 0);
            stats.setAvailableSpaces(parking.getPlazasDisponibles() != null ? parking.getPlazasDisponibles() : 0);
            
            if (stats.getTotalSpaces() > 0) {
                double occupancyRate = ((double) (stats.getTotalSpaces() - stats.getAvailableSpaces()) / stats.getTotalSpaces()) * 100;
                stats.setOccupancyRate(Math.round(occupancyRate * 100.0) / 100.0);
            } else {
                stats.setOccupancyRate(0.0);
            }
            
            return stats;
        }).collect(java.util.stream.Collectors.toList());
    }

    private OwnerDashboardStatsDTO createDefaultStats() {
        OwnerDashboardStatsDTO stats = new OwnerDashboardStatsDTO();
        stats.setTotalParkings(0);
        stats.setActiveReservations(0);
        stats.setTodayRevenue(BigDecimal.ZERO);
        stats.setMonthlyRevenue(BigDecimal.ZERO);
        stats.setAvailableSpaces(0);
        stats.setTotalSpaces(0);
        stats.setRecentReservations(Collections.emptyList());
        stats.setParkingStats(Collections.emptyList());
        return stats;
    }
}