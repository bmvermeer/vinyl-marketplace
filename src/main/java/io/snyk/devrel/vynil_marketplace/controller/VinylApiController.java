package io.snyk.devrel.vynil_marketplace.controller;

import io.snyk.devrel.vynil_marketplace.domain.Vinyl;
import io.snyk.devrel.vynil_marketplace.service.VinylService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/vinyls")
public class VinylApiController {

    private final VinylService vinylService;
    private static final String UPLOAD_DIR = "./uploads";

    public VinylApiController(VinylService vinylService) {
        this.vinylService = vinylService;
    }

    @GetMapping
    public List<Vinyl> getAllVinyls(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return vinylService.search(search);
        }
        return vinylService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vinyl> getVinyl(@PathVariable Long id) {
        var vinyl = vinylService.findById(id);
        if (vinyl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vinyl);
    }

    @PostMapping
    public ResponseEntity<Vinyl> createVinyl(
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam String genre,
            @RequestParam Integer releaseYear,
            @RequestParam Double price,
            @RequestParam String condition,
            @RequestParam(required = false) MultipartFile image) {
        
        var vinyl = new Vinyl();
        vinyl.setTitle(title);
        vinyl.setArtist(artist);
        vinyl.setGenre(genre);
        vinyl.setReleaseYear(releaseYear);
        vinyl.setPrice(price);
        vinyl.setCondition(condition);

        if (image != null && !image.isEmpty()) {
            try {
                var uploadPath = Path.of(UPLOAD_DIR);
                var filename = image.getOriginalFilename();
                var filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath);
                vinyl.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        var saved = vinylService.save(vinyl);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vinyl> updateVinyl(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String artist,
            @RequestParam String genre,
            @RequestParam Integer releaseYear,
            @RequestParam Double price,
            @RequestParam String condition,
            @RequestParam(required = false) MultipartFile image) {
        
        var existingVinyl = vinylService.findById(id);
        if (existingVinyl == null) {
            return ResponseEntity.notFound().build();
        }

        existingVinyl.setTitle(title);
        existingVinyl.setArtist(artist);
        existingVinyl.setGenre(genre);
        existingVinyl.setReleaseYear(releaseYear);
        existingVinyl.setPrice(price);
        existingVinyl.setCondition(condition);

        if (image != null && !image.isEmpty()) {
            try {
                var uploadPath = Path.of(UPLOAD_DIR);
                var filename = image.getOriginalFilename();
                var filePath = uploadPath.resolve(filename);
                Files.copy(image.getInputStream(), filePath);
                existingVinyl.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        var saved = vinylService.save(existingVinyl);
        return ResponseEntity.ok(saved);
    }
}
