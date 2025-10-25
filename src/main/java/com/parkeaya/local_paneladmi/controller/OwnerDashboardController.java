package com.parkeaya.local_paneladmi.controller;

import com.parkeaya.local_paneladmi.model.dto.UserDTO;
import com.parkeaya.local_paneladmi.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/owner", "/dashboard"})  // Permite ambas rutas
public class OwnerDashboardController {
    
    private final DashboardService dashboardService;
    
    public OwnerDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping({"/dashboard", ""})  // Maneja tanto /owner/dashboard como /dashboard
    public String dashboard(Model model, HttpSession session) {
        // Verificar autenticación
        UserDTO user = (UserDTO) session.getAttribute("USER");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            // Obtener el token de la sesión
            String token = (String) session.getAttribute("TOKEN");
            if (token == null) {
                return "redirect:/login";
            }

            model.addAttribute("user", user);
            model.addAttribute("stats", dashboardService.getDashboardStats(token));
            model.addAttribute("recentReservations", dashboardService.getRecentReservations(token));
            model.addAttribute("parkings", dashboardService.getOwnerParkings(token));
        } catch (Exception e) {
            // Log del error y mostrar datos mínimos
            model.addAttribute("user", user);
            model.addAttribute("error", "Error al cargar los datos del dashboard");
            e.printStackTrace();
        }
        
        return "dashboard/index";
    }
}