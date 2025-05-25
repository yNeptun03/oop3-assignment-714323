package com.example.Controller;

import com.example.Model.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.Service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    
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
        try {
            Movie movie = movieService.addMovie(title);
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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

    // Update movie rating
    @PatchMapping("/{id}/rating")
    public ResponseEntity<Movie> updateRating(
            @PathVariable Long id,
            @RequestParam int rating) {
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Movie updatedMovie = movieService.updateRating(id, rating);
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
