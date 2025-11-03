package com.parkeaya.local_paneladmi.model.dto;

import java.time.LocalDateTime;

public class ParkingRegistrationRequest {
    private String id;
    private String name;
    private String address;
    private Integer totalSpaces;
    private Double hourlyRate;
    private String description;
    private Boolean isVisible;
    private String notes;
    private String registrationStatus;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public ParkingRegistrationRequest() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ParkingRegistrationRequest(String name, String address, Integer totalSpaces, Double hourlyRate) {
        this();
        this.name = name;
        this.address = address;
        this.totalSpaces = totalSpaces;
        this.hourlyRate = hourlyRate;
        this.registrationStatus = "PENDING";
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getTotalSpaces() { return totalSpaces; }
    public void setTotalSpaces(Integer totalSpaces) { this.totalSpaces = totalSpaces; }

    public Double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(Double hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "ParkingRegistrationRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", totalSpaces=" + totalSpaces +
                ", hourlyRate=" + hourlyRate +
                ", description='" + description + '\'' +
                ", isVisible=" + isVisible +
                ", notes='" + notes + '\'' +
                ", registrationStatus='" + registrationStatus + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}