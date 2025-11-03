package com.parkeaya.local_paneladmi.model.dto;

import java.time.LocalDateTime;

public class PaymentDTO {

    private Long id;
    private String usuario;
    private Double monto;
    private String metodoPago;
    private LocalDateTime fecha;
    private String estado;

    public PaymentDTO() {}

    public PaymentDTO(Long id, String usuario, Double monto, String metodoPago, LocalDateTime fecha, String estado) {
        this.id = id;
        this.usuario = usuario;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
