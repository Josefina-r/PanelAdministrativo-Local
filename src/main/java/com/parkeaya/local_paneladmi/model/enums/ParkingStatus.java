package com.parkeaya.local_paneladmi.model.enums;

public enum ParkingStatus {
    PENDING_APPROVAL,    // Pendiente de aprobación
    ACTIVE,              // Activo y visible
    INACTIVE,            // Inactivo temporalmente
    REJECTED,            // Rechazado por administrador
    SUSPENDED            // Suspendido por incumplimiento
}