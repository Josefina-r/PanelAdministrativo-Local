package com.parkeaya.local_paneladmi.model.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class ParkingLotDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private BigDecimal precioHora;
    private Integer totalPlazas;
    private Integer plazasDisponibles;
    private Integer nivelSeguridad;
    private String descripcion;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private LocalTime horarioApertura;
    private LocalTime horarioCierre;
    private String telefono;
    private Boolean tieneCamaras;
    private Boolean tieneVigilancia24h;
    private Boolean aceptaReservas;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public Integer getNivelSeguridad() { return nivelSeguridad; }
    public void setNivelSeguridad(Integer nivelSeguridad) { this.nivelSeguridad = nivelSeguridad; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }
    
    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }
    
    public LocalTime getHorarioApertura() { return horarioApertura; }
    public void setHorarioApertura(LocalTime horarioApertura) { this.horarioApertura = horarioApertura; }
    
    public LocalTime getHorarioCierre() { return horarioCierre; }
    public void setHorarioCierre(LocalTime horarioCierre) { this.horarioCierre = horarioCierre; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public Boolean getTieneCamaras() { return tieneCamaras; }
    public void setTieneCamaras(Boolean tieneCamaras) { this.tieneCamaras = tieneCamaras; }
    
    public Boolean getTieneVigilancia24h() { return tieneVigilancia24h; }
    public void setTieneVigilancia24h(Boolean tieneVigilancia24h) { this.tieneVigilancia24h = tieneVigilancia24h; }
    
    public Boolean getAceptaReservas() { return aceptaReservas; }
    public void setAceptaReservas(Boolean aceptaReservas) { this.aceptaReservas = aceptaReservas; }
}