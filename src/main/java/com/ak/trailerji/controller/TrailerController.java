package com.ak.trailerji.controller;

import com.ak.trailerji.dto.TrailerDto;
import com.ak.trailerji.service.TrailerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/trailers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class TrailerController {

    private final Logger LOGGER= LoggerFactory.getLogger(TrailerController.class);

    private final TrailerService trailerService;
    
    @GetMapping("/public/latest")
    public ResponseEntity<List<TrailerDto>> getLatestTrailers(
            @RequestParam(defaultValue = "20") int limit) {
        List<TrailerDto> trailers = trailerService.getLatestOfficialTrailers(limit);
        LOGGER.info("#test1.0 ============================= getLatestTrailers ={}" , trailers);
        return ResponseEntity.ok(trailers);
    }
    
    @GetMapping("/public/search")
    public ResponseEntity<List<TrailerDto>> searchTrailers(
            @RequestParam String movieName) {
        List<TrailerDto> trailers = trailerService.searchTrailersByMovieName(movieName);
        LOGGER.info("#test1.1 ============================= searchTrailers= {}" , trailers);
        return ResponseEntity.ok(trailers);
    }
    
    @GetMapping("/public/popular")
    public ResponseEntity<List<TrailerDto>> getPopularTrailers(
            @RequestParam(defaultValue = "20") int limit) {
        List<TrailerDto> trailers = trailerService.getLatestOfficialTrailers(limit);
        LOGGER.info("#test1.2 ============================= getPopularTrailers= {}" , trailers);
        return ResponseEntity.ok(trailers);
    }
    
    @GetMapping("/public/genres/{genre}")
    public ResponseEntity<List<TrailerDto>> getTrailersByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "20") int limit) {
        // Implementation would filter by genre
        List<TrailerDto> trailers = trailerService.getLatestOfficialTrailers(limit);
        return ResponseEntity.ok(trailers);
    }
    
    @GetMapping("/public/studios/{studio}")
    public ResponseEntity<List<TrailerDto>> getTrailersByStudio(
            @PathVariable String studio,
            @RequestParam(defaultValue = "20") int limit) {
        // Implementation would filter by studio
        List<TrailerDto> trailers = trailerService.getLatestOfficialTrailers(limit);
        return ResponseEntity.ok(trailers);
    }
    
    // Protected endpoints for authenticated users
    @GetMapping("/favorites")
    public ResponseEntity<List<TrailerDto>> getUserFavorites() {
        // Implementation would get user's favorite trailers
        return ResponseEntity.ok(List.of());
    }
    
    @PostMapping("/favorites/{videoId}")
    public ResponseEntity<String> addToFavorites(@PathVariable String videoId) {
        // Implementation would add trailer to user's favorites
        return ResponseEntity.ok("Added to favorites");
    }
    
    @DeleteMapping("/favorites/{videoId}")
    public ResponseEntity<String> removeFromFavorites(@PathVariable String videoId) {
        // Implementation would remove trailer from user's favorites
        return ResponseEntity.ok("Removed from favorites");
    }
}