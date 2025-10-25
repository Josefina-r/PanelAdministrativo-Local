package com.parkeaya.local_paneladmi.controller;

import com.parkeaya.local_paneladmi.model.dto.ParkingLotDTO;
import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import com.parkeaya.local_paneladmi.service.ParkingService;
import com.parkeaya.local_paneladmi.util.ParkingConverter;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/parking")
public class ParkingController {
    
    private static final Logger logger = LoggerFactory.getLogger(ParkingController.class);
    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("parkingLot", new ParkingLotDTO());
        return "parking/create";
    }

    @PostMapping("/create")
    public String createParking(
            @ModelAttribute ParkingLotDTO parkingLot,
            @RequestParam(required = false) MultipartFile imagen,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            String token = (String) session.getAttribute("TOKEN");
            
            // Convertir ParkingLotDTO a DjangoParkingDTO
            DjangoParkingDTO djangoParking = ParkingConverter.convertToDjango(parkingLot);
            
            // Crear el estacionamiento
            DjangoParkingDTO created = parkingService.createParking(djangoParking, token);
            
            // Convertir la respuesta de vuelta a ParkingLotDTO
            ParkingLotDTO createdParking = ParkingConverter.convertFromDjango(created);

            // Si hay imagen, subirla
            if (imagen != null && !imagen.isEmpty() && createdParking != null) {
                parkingService.uploadParkingImage(createdParking.getId(), imagen, true, token);
            }

            redirectAttributes.addFlashAttribute("mensaje", "Estacionamiento creado exitosamente");
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear estacionamiento: " + e.getMessage());
            return "redirect:/parking/create";
        }
    }
}