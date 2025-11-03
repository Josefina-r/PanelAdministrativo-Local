package com.parkeaya.local_paneladmi.repository;

import com.parkeaya.local_paneladmi.model.entity.Reservation;
import com.parkeaya.local_paneladmi.model.entity.Parking;
import com.parkeaya.local_paneladmi.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Obtener todas las reservas de un usuario específico
    List<Reservation> findByUser(User user);

    // Obtener todas las reservas de un estacionamiento específico
    List<Reservation> findByParking(Parking parking);

    // Obtener reservas por estado (ejemplo: "Activa", "Cancelada")
    List<Reservation> findByEstado(String estado);

    // Obtener reservas recientes entre dos fechas
    List<Reservation> findByReservationDateBetween(Date startDate, Date endDate);

    // Obtener reservas recientes de un usuario específico
    List<Reservation> findByUserAndReservationDateBetween(User user, Date startDate, Date endDate);

    // Obtener reservas de un estacionamiento específico con estado determinado
    List<Reservation> findByParkingAndEstado(Parking parking, String estado);
}
