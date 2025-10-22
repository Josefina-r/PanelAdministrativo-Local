package com.parkeaya.local_paneladmi.model.dto;

import java.util.Date;

public class RecentReservationDTO {
    private Long id;
    private UserDTO user;
    private ParkingDTO parking;
    private Date reservationDate;
    private String estado;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
    
    public ParkingDTO getParking() { return parking; }
    public void setParking(ParkingDTO parking) { this.parking = parking; }
    
    public Date getReservationDate() { return reservationDate; }
    public void setReservationDate(Date reservationDate) { this.reservationDate = reservationDate; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}