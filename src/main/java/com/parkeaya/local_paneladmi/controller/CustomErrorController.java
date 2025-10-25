package com.parkeaya.local_paneladmi.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Obtener el código de error
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "Ha ocurrido un error";
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            switch(statusCode) {
                case 403:
                    errorMessage = "No tienes permiso para acceder a esta página";
                    break;
                case 404:
                    errorMessage = "Recurso no encontrado";
                    break;
                case 500:
                    errorMessage = "Error interno del servidor";
                    break;
            }
        }
        
        model.addAttribute("error", errorMessage);
        return "error/error";  // Esto usará templates/error/error.html
    }
}