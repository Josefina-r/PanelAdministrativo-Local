package com.parkeaya.local_paneladmi.controller;

import com.parkeaya.local_paneladmi.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

    @PostMapping("/do-login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            String token = authService.authenticate(username, password);
            
            // Configurar la sesión
            session.setAttribute("TOKEN", token);
            session.setAttribute("USERNAME", username);
            session.setMaxInactiveInterval(3600); // 1 hora

            // Guardar el token en la sesión
            session.setAttribute("TOKEN", token);
            session.setAttribute("USERNAME", username);

        // Establecer la autenticación básica
        var authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Persistir el SecurityContext en la sesión para que sobreviva al redirect
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

        // Guardar un UserDTO mínimo en sesión para que los controladores que lo esperan lo encuentren
        var userDTO = new com.parkeaya.local_paneladmi.model.dto.UserDTO();
        userDTO.setUsername(username);
        userDTO.setEmail(username);
        userDTO.setIsStaff(true);
        userDTO.setIsSuperuser(false);
        session.setAttribute("USER", userDTO);

        return "redirect:/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
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
