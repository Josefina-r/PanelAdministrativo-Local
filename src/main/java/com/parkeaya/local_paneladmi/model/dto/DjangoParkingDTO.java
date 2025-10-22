package com.parkeaya.local_paneladmi.model.dto;

import java.math.BigDecimal;

public class DjangoParkingDTO {
    private Long id;
    private Long dueno;  // Campo que viene de Django
    private String nombre;
    private String direccion;
    private BigDecimal precioHora;
    private Integer totalPlazas;
    private Integer plazasDisponibles;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getDueno() { return dueno; }
    public void setDueno(Long dueno) { this.dueno = dueno; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public BigDecimal getPrecioHora() { return precioHora; }
    public void setPrecioHora(BigDecimal precioHora) { this.precioHora = precioHora; }
    
    public Integer getTotalPlazas() { return totalPlazas; }
    public void setTotalPlazas(Integer totalPlazas) { this.totalPlazas = totalPlazas; }
    
    public Integer getPlazasDisponibles() { return plazasDisponibles; }
    public void setPlazasDisponibles(Integer plazasDisponibles) { this.plazasDisponibles = plazasDisponibles; }
}