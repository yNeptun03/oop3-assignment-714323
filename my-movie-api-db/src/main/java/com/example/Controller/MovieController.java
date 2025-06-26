package com.example.Controller;

import java.util.List;

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

/**
 * Handle user REST request for managing movie operations.
 * Provides endpoints for retrieving, adding, updating, and deleting movies.
 * All endpoints are prefixed with "/api/movies".
 * 
 * @see Movie
 * @see MovieService
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    
    @Autowired
    private MovieService movieService;

    /**
     * Retrieves a paginated list of all movies.
     * 
     * @param pageable Pagination parameters (page number, size, sorting)
     * @return ResponseEntity containing a page of movies
     * @see Pageable
     */
    @GetMapping
    public ResponseEntity<Page<Movie>> getAllMovies(Pageable pageable) {
        return ResponseEntity.ok(movieService.getAllMovies(pageable));
    }

    /**
     * Retrieves a specific movie by its ID.
     * 
     * @param id The ID of the movie to retrieve
     * @return ResponseEntity containing the movie if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id) {
        return movieService.getMovieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Adds a new movie by fetching and combining data from both OMDb and TMDB APIs.
     * 
     * @param title The title of the movie to add
     * @return ResponseEntity containing the created movie if successful
     *         - 200 OK if movie is successfully added
     *         - 400 Bad Request if the movie already exists or title is invalid
     *         - 500 Internal Server Error if API calls fail
     */
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

    /**
     * Updates the watched status of a movie.
     * 
     * @param id The ID of the movie to update
     * @param watched The new watched status to set
     * @return ResponseEntity containing the updated movie if successful
     *         - 200 OK if update is successful
     *         - 404 Not Found if movie doesn't exist
     */
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

    /**
     * Deletes a movie from the database.
     * 
     * @param id The ID of the movie to delete
     * @return ResponseEntity with no content if successful
     *         - 204 No Content if deletion is successful
     *         - 404 Not Found if movie doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves a list of all movie titles.
     * 
     * @param pageable Pagination parameters (page number, size, sorting)
     * @return ResponseEntity containing a list of movie titles
     * @see Pageable
     */
    @GetMapping("/titles")
    public ResponseEntity<List<String>> getAllMovieTitles(Pageable pageable) {
        return ResponseEntity.ok(movieService.getAllMovieTitles(pageable));
    }
}
