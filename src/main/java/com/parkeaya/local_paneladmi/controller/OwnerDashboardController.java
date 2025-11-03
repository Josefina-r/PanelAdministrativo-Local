package com.parkeaya.local_paneladmi.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.parkeaya.local_paneladmi.service.DashboardService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OwnerDashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(OwnerDashboardController.class);
    private final DashboardService dashboardService;
    
    public OwnerDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model, HttpSession session) {
        String username = "Usuario";
        
        try {
            logger.info("🔍 Iniciando carga de dashboard...");
            
            // Validar autenticación
            if (principal == null) {
                logger.warn("❌ Usuario no autenticado - redirigiendo a login");
                return "redirect:/login";
            }

            String token = (String) session.getAttribute("TOKEN");
            username = (String) session.getAttribute("USERNAME");
            
            logger.info("👤 Usuario: {}, Token: {}", username, token != null ? "presente" : "ausente");

            if (token == null) {
                logger.warn("❌ Token no encontrado - redirigiendo a login");
                return "redirect:/login";
            }

            try {
                var stats = dashboardService.getDashboardStats(token);
                logger.info("📊 Stats cargados: {}", stats != null ? "OK" : "NULL");
                model.addAttribute("stats", stats);
            } catch (Exception e) {
                logger.error("❌ Error cargando stats: {}", e.getMessage());
                model.addAttribute("stats", createDefaultStats());
            }

            try {
                var parkings = dashboardService.getOwnerParkings(token);
                logger.info("🅿️ Parkings cargados: {}", parkings.size());
                model.addAttribute("parkings", parkings);
            } catch (Exception e) {
                logger.error("❌ Error cargando parkings: {}", e.getMessage());
                model.addAttribute("parkings", new ArrayList<>());
            }

            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Dashboard");
            
            logger.info("✅ Dashboard cargado exitosamente");
            return "dashboard/dashboard";

        } catch (Exception e) {
            logger.error("💥 Error crítico en dashboard", e);
            model.addAttribute("error", "Error cargando dashboard: " + e.getMessage());
            model.addAttribute("username", username);
            return "dashboard/error";
        }
    }

    @GetMapping("/settings")
    public String settings(Principal principal, Model model, HttpSession session) {
        try {
            logger.info(" ========== CARGANDO CONFIGURACIÓN ==========");
            
            if (principal == null) {
                return "redirect:/login";
            }

            String token = (String) session.getAttribute("TOKEN");
            String username = getUsername(principal, session);
            
            if (token == null) {
                return "redirect:/login";
            }

            model.addAttribute("username", username);
            model.addAttribute("pageTitle", "Configuración - ParkeYa");
            
            logger.info("✅ Configuración cargada para: {}", username);
            return "sections/settings";
            
        } catch (Exception e) {
            logger.error("❌ Error cargando configuración: {}", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/spa/settings")
    public String spaSettings() {
        return "sections/settings";
    }

    @GetMapping("/spa/reservations")
    public String spaReservations() {
        return "sections/reservations";
    }

    @GetMapping("/spa/users")
    public String spaUsers() {
        return "sections/users";
    }

    @GetMapping("/spa/payments")
    public String spaPayments() {
        return "sections/payments";
    }

    @GetMapping("/spa/violations")
    public String spaViolations() {
        return "sections/violations";
    }

    // ✅ MÉTODOS AUXILIARES
    private String loadSectionPage(String section, String title, Principal principal, Model model, HttpSession session) {
        try {
            logger.info("📁 ========== CARGANDO {} ==========", title.toUpperCase());
            
            if (principal == null) {
                return "redirect:/login";
            }

            String token = (String) session.getAttribute("TOKEN");
            String username = getUsername(principal, session);
            
            if (token == null) {
                return "redirect:/login";
            }

            model.addAttribute("username", username);
            model.addAttribute("pageTitle", title + " - ParkeYa");
            
            logger.info("✅ {} cargada para: {}", title, username);
            return "sections/" + section;
            
        } catch (Exception e) {
            logger.error("❌ Error cargando {}: {}", title, e.getMessage());
            return "redirect:/dashboard";
        }
    }

    private String getUsername(Principal principal, HttpSession session) {
        String username = (String) session.getAttribute("USERNAME");
        if (username == null && principal != null) {
            username = principal.getName();
            session.setAttribute("USERNAME", username);
        }
        return username != null ? username : "Usuario";
    }
    
    private Map<String, Object> createDefaultStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalParkings", 0);
        stats.put("activeSpaces", 0);
        stats.put("totalRevenue", 0.0);
        stats.put("message", "Datos temporalmente no disponibles");
        return stats;
    }
}