package com.parkeaya.local_paneladmi.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.parkeaya.local_paneladmi.model.dto.UserDTO;
import com.parkeaya.local_paneladmi.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
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

            session.setAttribute("TOKEN", token);
            session.setAttribute("USERNAME", username);
            session.setMaxInactiveInterval(3600); // 1 hora

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setEmail(username);
           userDTO.setStaff(true);
           userDTO.setSuperuser(false);

            session.setAttribute("USER", userDTO);

            return "redirect:/dashboard";

        } catch (Exception e) {
            logger.error("Error al autenticar usuario {}", username, e);
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
