package com.parkeaya.local_paneladmi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkeaya.local_paneladmi.model.dto.RecentReservationDTO;
import com.parkeaya.local_paneladmi.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationsController {

    private final ReservationService reservationService;

    public ReservationsController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Listar reservas recientes
    @GetMapping("/recent")
    public ResponseEntity<List<RecentReservationDTO>> getRecentReservations() {
        return ResponseEntity.ok(reservationService.getRecentReservations());
    }

    // Crear reserva
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody RecentReservationDTO dto) {
        return ResponseEntity.ok(reservationService.createReservation(dto));
    }

    // Actualizar reserva
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody RecentReservationDTO dto) {
        return ResponseEntity.ok(reservationService.updateReservation(id, dto));
    }

    // Eliminar reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok().build();
    }
}
