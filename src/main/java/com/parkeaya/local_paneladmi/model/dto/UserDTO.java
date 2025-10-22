package com.parkeaya.local_paneladmi.model.dto;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Boolean isStaff;
    private Boolean isSuperuser;
    private String rol;
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getIsStaff() { return isStaff; }
    public void setIsStaff(Boolean isStaff) { this.isStaff = isStaff; }
    public Boolean getIsSuperuser() { return isSuperuser; }
    public void setIsSuperuser(Boolean isSuperuser) { this.isSuperuser = isSuperuser; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}