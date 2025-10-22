package com.parkeaya.local_paneladmi.model.dto;

public class ParkingStatsDTO {
    private Long parkingId;
    private String parkingName;
    private Integer totalReservations;
    private java.math.BigDecimal totalRevenue;
    private Integer availableSpaces;
    private Integer totalSpaces;
    private Double occupancyRate;
    
    // Getters and Setters
    public Long getParkingId() { return parkingId; }
    public void setParkingId(Long parkingId) { this.parkingId = parkingId; }
    
    public String getParkingName() { return parkingName; }
    public void setParkingName(String parkingName) { this.parkingName = parkingName; }
    
    public Integer getTotalReservations() { return totalReservations; }
    public void setTotalReservations(Integer totalReservations) { this.totalReservations = totalReservations; }
    
    public java.math.BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(java.math.BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    
    public Integer getAvailableSpaces() { return availableSpaces; }
    public void setAvailableSpaces(Integer availableSpaces) { this.availableSpaces = availableSpaces; }
    
    public Integer getTotalSpaces() { return totalSpaces; }
    public void setTotalSpaces(Integer totalSpaces) { this.totalSpaces = totalSpaces; }
    
    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }
}