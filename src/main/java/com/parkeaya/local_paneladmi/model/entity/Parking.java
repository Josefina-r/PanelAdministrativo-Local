package com.parkeaya.local_paneladmi.model.entity;

/*
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import com.parkeaya.local_paneladmi.model.enums.ParkingStatus;

@Entity
@Table(name = "parkings")
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String address;
    
    private Double latitude;
    private Double longitude;
    
    @Column(nullable = false)
    private Integer totalSpaces;
    
    @Column(nullable = false)
    private Integer availableSpaces;
    
    @Column(nullable = false)
    private BigDecimal pricePerHour;
    
    private BigDecimal pricePerDay;
    private BigDecimal pricePerMonth;
    
    @Enumerated(EnumType.STRING)
    private ParkingStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;
    
    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL)
    private List<Reservation> reservations;
    
    // Getters and Setters
}*/