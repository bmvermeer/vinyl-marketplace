package io.snyk.devrel.vynil_marketplace.controller;

import io.snyk.devrel.vynil_marketplace.domain.Vinyl;
import io.snyk.devrel.vynil_marketplace.service.VinylService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Controller
public class VinylController {

    private final VinylService vinylService;
    private static final String UPLOAD_DIR = "./uploads";

    public VinylController(VinylService vinylService) {
        this.vinylService = vinylService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/vinyls";
    }

    @GetMapping("/vinyls")
    public String listVinyls(
            @RequestParam(required = false) String search,
            Model model) {
        
        var vinyls = (search != null && !search.isBlank())
                ? vinylService.search(search)
                : vinylService.findAll();
        
        if (search != null && !search.isBlank()) {
            model.addAttribute("searchTerm", search);
        }
        
        model.addAttribute("vinyls", vinyls);
        return "vinyls";
    }

    @GetMapping("/vinyls/add")
    public String showAddForm(Model model) {
        model.addAttribute("vinyl", new Vinyl());
        return "vinyl-form";
    }

    @PostMapping("/vinyls/add")
    public String addVinyl(@ModelAttribute Vinyl vinyl,
                           @RequestParam("image") MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
            if (!imageFile.isEmpty()) {
                var uploadPath = Path.of(UPLOAD_DIR);
                var filename = imageFile.getOriginalFilename();
                var filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath);
                vinyl.setImageUrl("/uploads/" + filename);
            }

            vinylService.save(vinyl);
            redirectAttributes.addFlashAttribute("success", "Vinyl record added successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }
        return "redirect:/vinyls";
    }

    @GetMapping("/vinyls/{id}")
    public String viewVinyl(@PathVariable Long id, Model model) {
        var vinyl = vinylService.findById(id);
        if (vinyl == null) {
            return "redirect:/vinyls";
        }
        model.addAttribute("vinyl", vinyl);
        return "vinyl-detail";
    }

    @GetMapping("/vinyls/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        var vinyl = vinylService.findById(id);
        if (vinyl == null) {
            return "redirect:/vinyls";
        }
        model.addAttribute("vinyl", vinyl);
        model.addAttribute("editMode", true);
        return "vinyl-form";
    }

    @PostMapping("/vinyls/{id}/edit")
    public String editVinyl(@PathVariable Long id,
                            @ModelAttribute Vinyl vinyl,
                            @RequestParam("image") MultipartFile imageFile,
                            RedirectAttributes redirectAttributes) {
        try {
            var existingVinyl = vinylService.findById(id);
            if (existingVinyl == null) {
                return "redirect:/vinyls";
            }

            if (!imageFile.isEmpty()) {
                var uploadPath = Path.of(UPLOAD_DIR);
                var filename = imageFile.getOriginalFilename();
                var filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath);
                vinyl.setImageUrl("/uploads/" + filename);
            } else {
                vinyl.setImageUrl(existingVinyl.getImageUrl());
            }

            vinyl.setId(id);
            vinylService.save(vinyl);
            redirectAttributes.addFlashAttribute("success", "Vinyl record updated successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }
        return "redirect:/vinyls/" + id;
    }
}
