/*package com.parkeaya.local_paneladmi.controller;

import com.parkeaya.local_paneladmi.model.dto.ParkingLotDTO;
import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import com.parkeaya.local_paneladmi.service.ParkingService;
import com.parkeaya.local_paneladmi.service.AuthService;
import com.parkeaya.local_paneladmi.util.ParkingConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/parkings")
public class ParkingController {
    
    private static final Logger logger = LoggerFactory.getLogger(ParkingController.class);
    private final ParkingService parkingService;
    private final AuthService authService;

    public ParkingController(ParkingService parkingService, AuthService authService) {
        this.parkingService = parkingService;
        this.authService = authService;
    }

    @GetMapping
    public String listParkings(Principal principal, Model model) {
        try {
            String token = authService.getCurrentUserToken(principal);
            List<DjangoParkingDTO> parkings = parkingService.getOwnerParkings(token);
            model.addAttribute("parkings", parkings);
            return "parkings/list";
        } catch (Exception e) {
            logger.error("Error al listar estacionamientos: {}", e.getMessage());
            model.addAttribute("error", "Error al cargar estacionamientos: " + e.getMessage());
            return "parkings/list";
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("parking", new ParkingLotDTO());
        return "parkings/create";
    }

    @PostMapping("/create")
    public String createParking(
            @ModelAttribute("parking") ParkingLotDTO parkingLot,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String token = authService.getCurrentUserToken(principal);
            
            logger.info("Creando estacionamiento: {}", parkingLot.getNombre());
            
            // Convertir ParkingLotDTO a DjangoParkingDTO
            DjangoParkingDTO djangoParking = ParkingConverter.convertToDjango(parkingLot);
            
            // Crear el estacionamiento
            DjangoParkingDTO created = parkingService.createParking(djangoParking, token);
            
            // Si hay imagen, subirla
            if (imagen != null && !imagen.isEmpty() && created != null) {
                try {
                    parkingService.uploadParkingImage(created.getId(), imagen, true, token);
                    logger.info("Imagen subida exitosamente para el estacionamiento ID: {}", created.getId());
                } catch (Exception e) {
                    logger.warn("No se pudo subir la imagen, pero el estacionamiento fue creado: {}", e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("success", 
                "Estacionamiento '" + created.getNombre() + "' creado exitosamente. " +
                "Será enviado para aprobación y aparecerá en la app Parkeaya.");
            
            return "redirect:/parkings";
            
        } catch (Exception e) {
            logger.error("Error al crear estacionamiento: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", 
                "Error al crear estacionamiento: " + e.getMessage());
            redirectAttributes.addFlashAttribute("parking", parkingLot);
            return "redirect:/parkings/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Principal principal, Model model) {
        try {
            String token = authService.getCurrentUserToken(principal);
            DjangoParkingDTO parking = parkingService.getParkingById(id, token);
            ParkingLotDTO parkingLot = ParkingConverter.convertFromDjango(parking);
            model.addAttribute("parking", parkingLot);
            return "parkings/edit";
        } catch (Exception e) {
            logger.error("Error al cargar estacionamiento para editar: {}", e.getMessage());
            return "redirect:/parkings";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateParking(
            @PathVariable Long id,
            @ModelAttribute("parking") ParkingLotDTO parkingLot,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String token = authService.getCurrentUserToken(principal);
            DjangoParkingDTO djangoParking = ParkingConverter.convertToDjango(parkingLot);
            parkingService.updateParking(id, djangoParking, token);
            
            redirectAttributes.addFlashAttribute("success", "Estacionamiento actualizado exitosamente");
            return "redirect:/parkings";
            
        } catch (Exception e) {
            logger.error("Error al actualizar estacionamiento: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar estacionamiento: " + e.getMessage());
            return "redirect:/parkings/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteParking(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            String token = authService.getCurrentUserToken(principal);
            parkingService.deleteParking(id, token);
            
            redirectAttributes.addFlashAttribute("success", "Estacionamiento eliminado exitosamente");
            return "redirect:/parkings";
            
        } catch (Exception e) {
            logger.error("Error al eliminar estacionamiento: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar estacionamiento: " + e.getMessage());
            return "redirect:/parkings";
        }
    }

    @PostMapping("/{id}/upload-image")
    public String uploadParkingImage(
            @PathVariable Long id,
            @RequestParam("imagen") MultipartFile imagen,
            @RequestParam(value = "esPrincipal", defaultValue = "false") Boolean esPrincipal,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            String token = authService.getCurrentUserToken(principal);
            parkingService.uploadParkingImage(id, imagen, esPrincipal, token);
            
            redirectAttributes.addFlashAttribute("success", "Imagen subida exitosamente");
            return "redirect:/parkings/edit/" + id;
            
        } catch (Exception e) {
            logger.error("Error al subir imagen: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al subir imagen: " + e.getMessage());
            return "redirect:/parkings/edit/" + id;
        }
    }

    @GetMapping("/{id}")
    public String viewParking(@PathVariable Long id, Principal principal, Model model) {
        try {
            String token = authService.getCurrentUserToken(principal);
            DjangoParkingDTO parking = parkingService.getParkingById(id, token);
            model.addAttribute("parking", parking);
            return "parkings/view";
        } catch (Exception e) {
            logger.error("Error al cargar estacionamiento: {}", e.getMessage());
            return "redirect:/parkings";
        }
    }
}*/