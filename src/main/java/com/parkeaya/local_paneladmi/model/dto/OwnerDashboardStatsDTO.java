package com.parkeaya.local_paneladmi.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class OwnerDashboardStatsDTO {
    private Integer totalParkings;
    private Integer activeReservations;
    private BigDecimal todayRevenue;
    private BigDecimal monthlyRevenue;
    private Integer availableSpaces;
    private Integer totalSpaces;
    private List<RecentReservationDTO> recentReservations;
    private List<ParkingStatsDTO> parkingStats;
    
    // Getters and Setters
    public Integer getTotalParkings() { return totalParkings; }
    public void setTotalParkings(Integer totalParkings) { this.totalParkings = totalParkings; }
    
    public Integer getActiveReservations() { return activeReservations; }
    public void setActiveReservations(Integer activeReservations) { this.activeReservations = activeReservations; }
    
    public BigDecimal getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(BigDecimal todayRevenue) { this.todayRevenue = todayRevenue; }
    
    public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
    public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
    
    public Integer getAvailableSpaces() { return availableSpaces; }
    public void setAvailableSpaces(Integer availableSpaces) { this.availableSpaces = availableSpaces; }
    
    public Integer getTotalSpaces() { return totalSpaces; }
    public void setTotalSpaces(Integer totalSpaces) { this.totalSpaces = totalSpaces; }
    
    public List<RecentReservationDTO> getRecentReservations() { return recentReservations; }
    public void setRecentReservations(List<RecentReservationDTO> recentReservations) { this.recentReservations = recentReservations; }
    
    public List<ParkingStatsDTO> getParkingStats() { return parkingStats; }
    public void setParkingStats(List<ParkingStatsDTO> parkingStats) { this.parkingStats = parkingStats; }
}