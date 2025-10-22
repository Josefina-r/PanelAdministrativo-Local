package com.parkeaya.local_paneladmi.controller;



/*import com.parkeaya.local_paneladmi.model.dto.DjangoParkingDTO;
import com.parkeaya.local_paneladmi.service.ParkingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/owner/parkings")
public class ParkingManagementController {
    
    private final ParkingService parkingService;
    
    public ParkingManagementController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }
    
    @GetMapping
    public String listParkings(Model model, Principal principal) {
        List<DjangoParkingDTO> parkings = parkingService.getParkingsByOwnerEmail(principal.getName());
        model.addAttribute("parkings", parkings);
        return "parking/list";
    }
    
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("parking", new DjangoParkingDTO());
        return "parking/create";
    }
    
    @PostMapping("/create")
    public String createParking(@ModelAttribute DjangoParkingDTO parking, 
                              Principal principal) {
        try {
            parkingService.createParking(parking, principal.getName());
            return "redirect:/owner/parkings?success=created";
        } catch (Exception e) {
            return "redirect:/owner/parkings/create?error=" + e.getMessage();
        }
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Principal principal) {
        try {
            DjangoParkingDTO parking = parkingService.getParkingByIdAndOwner(id, principal.getName());
            model.addAttribute("parking", parking);
            return "parking/edit";
        } catch (Exception e) {
            return "redirect:/owner/parkings?error=not_found";
        }
    }
    
    @PostMapping("/edit/{id}")
    public String updateParking(@PathVariable Long id, 
                              @ModelAttribute DjangoParkingDTO parking,
                              Principal principal) {
        try {
            parkingService.updateParking(id, parking, principal.getName());
            return "redirect:/owner/parkings?success=updated";
        } catch (Exception e) {
            return "redirect:/owner/parkings/edit/" + id + "?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/delete/{id}")
    public String deleteParking(@PathVariable Long id, Principal principal) {
        try {
            parkingService.deleteParking(id, principal.getName());
            return "redirect:/owner/parkings?success=deleted";
        } catch (Exception e) {
            return "redirect:/owner/parkings?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/availability/{id}")
    public String updateAvailability(@PathVariable Long id,
                                   @RequestParam Integer availableSpaces,
                                   Principal principal) {
        try {
            parkingService.updateAvailability(id, availableSpaces, principal.getName());
            return "redirect:/owner/parkings?success=availability_updated";
        } catch (Exception e) {
            return "redirect:/owner/parkings?error=" + e.getMessage();
        }
    }
}*/