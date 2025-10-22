package com.parkeaya.local_paneladmi.controller;

import com.parkeaya.local_paneladmi.model.dto.UserDTO;
import com.parkeaya.local_paneladmi.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owner")
public class OwnerDashboardController {
    
    private final DashboardService dashboardService;
    
    public OwnerDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Verificar autenticación
        UserDTO user = (UserDTO) session.getAttribute("USER");
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("stats", dashboardService.getDashboardStats(user.getEmail()));
        model.addAttribute("recentReservations", dashboardService.getRecentReservations(user.getEmail()));
        model.addAttribute("parkings", dashboardService.getOwnerParkings(user.getEmail()));
        
        return "dashboard/index";
    }
}