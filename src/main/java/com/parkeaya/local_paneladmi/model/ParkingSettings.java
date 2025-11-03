package com.parkeaya.local_paneladmi.model;

public class ParkingSettings {
    private String id;
    private String name;
    private String address;
    private Integer totalSpaces;
    private Double hourlyRate;
    private String description;
    private Boolean isVisible;
    private String registrationStatus;
    private String imageUrl;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private String email;
    private String businessHours;

    // Constructores, Getters y Setters (igual que antes)
    public ParkingSettings() {}

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

    public Boolean isVisible() { return isVisible; }
    public void setVisible(Boolean visible) { isVisible = visible; }

    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBusinessHours() { return businessHours; }
    public void setBusinessHours(String businessHours) { this.businessHours = businessHours; }
}