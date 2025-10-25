package com.parkeaya.local_paneladmi.util;

import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import com.parkeaya.local_paneladmi.model.dto.ParkingLotDTO;

public class ParkingConverter {
    
    public static DjangoParkingDTO convertToDjango(ParkingLotDTO parkingLot) {
        if (parkingLot == null) return null;
        
        DjangoParkingDTO django = new DjangoParkingDTO();
        django.setId(parkingLot.getId());
        django.setNombre(parkingLot.getNombre());
        django.setDireccion(parkingLot.getDireccion());
        django.setPrecioHora(parkingLot.getPrecioHora());
        django.setTotalPlazas(parkingLot.getTotalPlazas());
        django.setPlazasDisponibles(parkingLot.getPlazasDisponibles());
        return django;
    }
    
    public static ParkingLotDTO convertFromDjango(DjangoParkingDTO django) {
        if (django == null) return null;
        
        ParkingLotDTO parkingLot = new ParkingLotDTO();
        parkingLot.setId(django.getId());
        parkingLot.setNombre(django.getNombre());
        parkingLot.setDireccion(django.getDireccion());
        parkingLot.setPrecioHora(django.getPrecioHora());
        parkingLot.setTotalPlazas(django.getTotalPlazas());
        parkingLot.setPlazasDisponibles(django.getPlazasDisponibles());
        return parkingLot;
    }
}