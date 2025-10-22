package com.parkeaya.local_paneladmi.service;

import com.parkeaya.local_paneladmi.model.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DashboardService {

    @Value("${django.api.url}")
    private String djangoApiUrl;

    private final RestTemplate restTemplate;
    private final ParkingService parkingService;

    public DashboardService(RestTemplate restTemplate, ParkingService parkingService) {
        this.restTemplate = restTemplate;
        this.parkingService = parkingService;
    }

    /**
     * Obtiene estadísticas del dashboard para el dueño
     */
    public OwnerDashboardStatsDTO getDashboardStats(String ownerEmail) {
        try {
            // Obtener parkings del dueño
            List<DjangoParkingDTO> ownerParkings = parkingService.getParkingsByOwnerEmail(ownerEmail);
            
            // Calcular estadísticas básicas
            OwnerDashboardStatsDTO stats = new OwnerDashboardStatsDTO();
            stats.setTotalParkings(ownerParkings.size());
            stats.setTotalSpaces(ownerParkings.stream().mapToInt(p -> p.getTotalPlazas() != null ? p.getTotalPlazas() : 0).sum());
            stats.setAvailableSpaces(ownerParkings.stream().mapToInt(p -> p.getPlazasDisponibles() != null ? p.getPlazasDisponibles() : 0).sum());
            
            // Calcular métricas adicionales
            stats.setActiveReservations(calculateActiveReservations(ownerParkings));
            stats.setTodayRevenue(calculateTodayRevenue(ownerParkings));
            stats.setMonthlyRevenue(calculateMonthlyRevenue(ownerParkings));
            
            // Obtener reservas recientes
            stats.setRecentReservations(getRecentReservations(ownerEmail));
            
            // Estadísticas por parking
            stats.setParkingStats(calculateParkingStats(ownerParkings));
            
            return stats;
            
        } catch (Exception e) {
            System.err.println("Error en getDashboardStats: " + e.getMessage());
            return createDefaultStats();
        }
    }

    /**
     * Obtiene reservas recientes para el dueño
     */
    public List<RecentReservationDTO> getRecentReservations(String ownerEmail) {
        try {
            // Datos de ejemplo temporalmente
            return Arrays.asList(
                createMockReservation(1L, "Juan Pérez", "Parking Central", "confirmada"),
                createMockReservation(2L, "María García", "Parking Norte", "activa"),
                createMockReservation(3L, "Carlos López", "Parking Sur", "completada")
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene los parkings del dueño
     */
    public List<DjangoParkingDTO> getOwnerParkings(String ownerEmail) {
        return parkingService.getParkingsByOwnerEmail(ownerEmail);
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private Integer calculateActiveReservations(List<DjangoParkingDTO> parkings) {
        return parkings.stream()
            .mapToInt(p -> (int) ((p.getPlazasDisponibles() != null ? p.getPlazasDisponibles() : 0) * 0.3))
            .sum();
    }

    private BigDecimal calculateTodayRevenue(List<DjangoParkingDTO> parkings) {
        return parkings.stream()
            .map(parking -> {
                BigDecimal price = parking.getPrecioHora() != null ? parking.getPrecioHora() : BigDecimal.ZERO;
                Integer spaces = parking.getTotalPlazas() != null ? parking.getTotalPlazas() : 0;
                return price.multiply(BigDecimal.valueOf(spaces * 0.4));
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyRevenue(List<DjangoParkingDTO> parkings) {
        return calculateTodayRevenue(parkings).multiply(BigDecimal.valueOf(30));
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

    private RecentReservationDTO createMockReservation(Long id, String userName, String parkingName, String estado) {
        RecentReservationDTO reservation = new RecentReservationDTO();
        reservation.setId(id);
        
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername(userName);
        reservation.setUser(user);
        
        ParkingDTO parking = new ParkingDTO();
        parking.setId(1L);
        parking.setNombre(parkingName);
        reservation.setParking(parking);
        
        reservation.setEstado(estado);
        reservation.setReservationDate(new Date());
        
        return reservation;
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