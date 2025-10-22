package com.parkeaya.local_paneladmi.controller;

import com.parkeaya.local_paneladmi.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas");
        }
        return "auth/login";
    }

    @PostMapping("/login")
public String login(@RequestParam String username,
                    @RequestParam String password,
                    HttpSession session,
                    Model model) {
    try {
        String token = authService.authenticate(username, password);
        session.setAttribute("TOKEN", token);
        session.setAttribute("USERNAME", username);

        // Esto renderiza directamente la plantilla Thymeleaf
        return "dashboard/index"; 
    } catch (Exception e) {
        model.addAttribute("error", "Credenciales incorrectas");
        return "auth/login";
    }
}


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
