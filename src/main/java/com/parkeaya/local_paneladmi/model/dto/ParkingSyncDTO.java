package com.parkeaya.local_paneladmi.model.dto;

import java.math.BigDecimal;

public class ParkingSyncDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private Integer totalPlazas;
    private Integer plazasDisponibles;
    private Boolean estaAbierto;
    private Double ratingPromedio;
    private Long ownerId;
    
    // Constructores
    public ParkingSyncDTO() {}
    
    public ParkingSyncDTO(Long id, String nombre, String direccion, BigDecimal latitud, 
                         BigDecimal longitud, Integer totalPlazas, Integer plazasDisponibles,
                         Boolean estaAbierto, Double ratingPromedio, Long ownerId) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.totalPlazas = totalPlazas;
        this.plazasDisponibles = plazasDisponibles;
        this.estaAbierto = estaAbierto;
        this.ratingPromedio = ratingPromedio;
        this.ownerId = ownerId;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public Integer getPlazasDisponibles() { return plazasDisponibles; }
    public void setPlazasDisponibles(Integer plazasDisponibles) { this.plazasDisponibles = plazasDisponibles; }
    
    public Boolean getEstaAbierto() { return estaAbierto; }
    public void setEstaAbierto(Boolean estaAbierto) { this.estaAbierto = estaAbierto; }
    
    public Double getRatingPromedio() { return ratingPromedio; }
    public void setRatingPromedio(Double ratingPromedio) { this.ratingPromedio = ratingPromedio; }
    
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}