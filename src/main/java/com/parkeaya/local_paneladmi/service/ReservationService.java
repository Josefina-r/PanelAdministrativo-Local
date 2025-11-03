package com.parkeaya.local_paneladmi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkeaya.local_paneladmi.model.dto.UserDTO;
import com.parkeaya.local_paneladmi.model.dto.ParkingDTO;
import com.parkeaya.local_paneladmi.model.dto.RecentReservationDTO;
import com.parkeaya.local_paneladmi.model.entity.User;
import com.parkeaya.local_paneladmi.model.entity.Parking;
import com.parkeaya.local_paneladmi.model.entity.Reservation;
import com.parkeaya.local_paneladmi.repository.UserRepository;
import com.parkeaya.local_paneladmi.repository.ParkingRepository;
import com.parkeaya.local_paneladmi.repository.ReservationRepository;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ParkingRepository parkingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              ParkingRepository parkingRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.parkingRepository = parkingRepository;
    }

    // Obtener todas las reservas recientes
    public List<RecentReservationDTO> getRecentReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Crear nueva reserva
    public Reservation createReservation(RecentReservationDTO dto) {
        Optional<User> userOpt = userRepository.findById(dto.getUser().getId());
        Optional<Parking> parkingOpt = parkingRepository.findById(dto.getParking().getId());

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }
        if (parkingOpt.isEmpty()) {
            throw new IllegalArgumentException("Parking no encontrado");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(userOpt.get());
        reservation.setParking(parkingOpt.get());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setEstado(dto.getEstado());

        // Actualizar plazas disponibles
        parkingOpt.get().ocuparPlaza();

        return reservationRepository.save(reservation);
    }

    // Actualizar reserva existente
    public Reservation updateReservation(Long id, RecentReservationDTO dto) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        Optional<User> userOpt = userRepository.findById(dto.getUser().getId());
        Optional<Parking> parkingOpt = parkingRepository.findById(dto.getParking().getId());

        if (userOpt.isEmpty() || parkingOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario o Parking no encontrado");
        }

        reservation.setUser(userOpt.get());
        reservation.setParking(parkingOpt.get());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setEstado(dto.getEstado());

        return reservationRepository.save(reservation);
    }

    // Eliminar reserva
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // Liberar plaza
        reservation.getParking().liberarPlaza();

        reservationRepository.delete(reservation);
    }

    // Convertir entidad a DTO
    private RecentReservationDTO convertToDTO(Reservation reservation) {
        RecentReservationDTO dto = new RecentReservationDTO();
        dto.setId(reservation.getId());

        // UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(reservation.getUser().getId());
        userDTO.setNombre(reservation.getUser().getNombre());
        dto.setUser(userDTO);

        // ParkingDTO
        ParkingDTO parkingDTO = new ParkingDTO();
        parkingDTO.setId(reservation.getParking().getId());
        parkingDTO.setNombre(reservation.getParking().getNombre());
        dto.setParking(parkingDTO);

        dto.setReservationDate(reservation.getReservationDate());
        dto.setEstado(reservation.getEstado());

        return dto;
    }
}
