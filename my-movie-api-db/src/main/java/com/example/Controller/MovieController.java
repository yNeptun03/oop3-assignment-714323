package com.example.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Model.Movie;
import com.example.Service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    
    @Autowired
    private MovieService movieService;

    // Get paginated list of movies
    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getAllMovies(pageable));
    }

    // Get a specific movie by ID
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add a new movie (combines data from OMDb and TMDB)
    @PostMapping
    public ResponseEntity<Movie> addMovie(@RequestParam String title) {
        logger.info("Received request to add movie: {}", title);
        try {
            Movie movie = movieService.addMovie(title);
            logger.info("Successfully added movie: {}", movie.getTitle());
            return ResponseEntity.ok(movie);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request to add movie: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error adding movie: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Update watched status
    @PatchMapping("/{id}/watched")
    public ResponseEntity<Movie> updateWatchedStatus(
            @PathVariable Long id,
            @RequestParam boolean watched) {
        try {
            Movie updatedMovie = movieService.updateWatchedStatus(id, watched);
            return ResponseEntity.ok(updatedMovie);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a movie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
