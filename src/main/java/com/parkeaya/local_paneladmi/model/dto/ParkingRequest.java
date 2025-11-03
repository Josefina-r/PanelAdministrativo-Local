package com.parkeaya.local_paneladmi.model.dto;

import java.math.BigDecimal;

public class ParkingRequest {
    private String nombre;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer totalPlazas;
    
    // Constructores
    public ParkingRequest() {}
    
    public ParkingRequest(String nombre, String direccion, BigDecimal latitud, 
                         BigDecimal longitud, Integer totalPlazas) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.totalPlazas = totalPlazas;
    }
    
    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    
    public Integer getTotalPlazas() { return totalPlazas; }
    public void setTotalPlazas(Integer totalPlazas) { this.totalPlazas = totalPlazas; }
}