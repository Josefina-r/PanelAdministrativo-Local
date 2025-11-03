package com.parkeaya.local_paneladmi.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parkings")
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "total_plazas")
    private Integer totalPlazas;

    @Column(name = "plazas_disponibles")
    private Integer plazasDisponibles;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "esta_abierto")
    private Boolean estaAbierto;

    // Constructores
    public Parking() {}

    public Parking(String nombre, Integer totalPlazas) {
        this.nombre = nombre;
        this.totalPlazas = totalPlazas;
        this.plazasDisponibles = totalPlazas;
        this.activo = true;
        this.estaAbierto = true;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getTotalPlazas() { return totalPlazas; }
    public void setTotalPlazas(Integer totalPlazas) { this.totalPlazas = totalPlazas; }

    public Integer getPlazasDisponibles() { return plazasDisponibles; }
    public void setPlazasDisponibles(Integer plazasDisponibles) { this.plazasDisponibles = plazasDisponibles; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Boolean getEstaAbierto() { return estaAbierto; }
    public void setEstaAbierto(Boolean estaAbierto) { this.estaAbierto = estaAbierto; }

    // Métodos para ocupar y liberar plaza
    public synchronized void ocuparPlaza() {
        if (!activo || !estaAbierto || plazasDisponibles <= 0) {
            throw new RuntimeException("No se puede ocupar plaza: Parking cerrado o lleno");
        }
        plazasDisponibles--;
    }

    public synchronized void liberarPlaza() {
        if (!activo || !estaAbierto || plazasDisponibles >= totalPlazas) {
            throw new RuntimeException("No se puede liberar plaza: Parking cerrado o lleno");
        }
        plazasDisponibles++;
    }
}
