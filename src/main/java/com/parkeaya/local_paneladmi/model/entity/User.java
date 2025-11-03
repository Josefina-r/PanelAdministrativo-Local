package com.parkeaya.local_paneladmi.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "is_staff")
    private boolean isStaff;

    @Column(name = "is_superuser")
    private boolean isSuperuser;

    // Constructores
    public User() {}

    public User(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isStaff() { return isStaff; }
    public void setStaff(boolean staff) { isStaff = staff; }

    public boolean isSuperuser() { return isSuperuser; }
    public void setSuperuser(boolean superuser) { isSuperuser = superuser; }
}
