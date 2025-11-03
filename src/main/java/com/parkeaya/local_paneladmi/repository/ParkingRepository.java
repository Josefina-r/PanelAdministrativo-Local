package com.parkeaya.local_paneladmi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.parkeaya.local_paneladmi.model.entity.Parking;

@Repository
public interface ParkingRepository extends JpaRepository<Parking, Long> {
}
