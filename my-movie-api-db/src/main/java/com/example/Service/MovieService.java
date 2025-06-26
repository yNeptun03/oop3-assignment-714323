package com.example.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.Model.Movie;
import com.example.Repository.MovieRepository;
/*import com.example.Service.OMDbService;
import com.example.Service.TMDBService;/* */

/**
 * Service class that manages movie operations by integrating data from both OMDb and TMDB APIs.
 * Handles movie creation, updates, and deletion with data merging.
 */
@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private OMDbService omdbService;

    @Autowired
    private TMDBService tmdbService;

    /**
     * Retrieves a list of all movies from the database.
     *
     * @param pageable Pagination parameters
     * @return Page of movies
     */
    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    /**
     * Retrieves a movie by its ID.
     *
     * @param id The movie's ID
     * @return Optional containing the movie if found
     */
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    /**
     * Retrieves a list of unwatched movies from the database using Streams API.
     *
     * @param pageable Pagination parameters
     * @return List of unwatched movies
     */
    public List<Movie> getUnwatchedMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(movie -> !movie.isWatched())
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all movie titles from the database using Streams API.
     *
     * @param pageable Pagination parameters
     * @return List of movie titles
     */
    public List<String> getAllMovieTitles(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(Movie::getTitle)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves movies by director using Streams API.
     *
     * @param director The director's name to filter by
     * @param pageable Pagination parameters
     * @return List of movies by the specified director
     */
    public List<Movie> getMoviesByDirector(String director, Pageable pageable) {
        return movieRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(movie -> director.equalsIgnoreCase(movie.getDirector()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves movies by year using Streams API.
     *
     * @param year The year to filter by
     * @param pageable Pagination parameters
     * @return List of movies from the specified year
     */
    public List<Movie> getMoviesByYear(String year, Pageable pageable) {
        return movieRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(movie -> year.equals(movie.getYear()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of watched movies from the database using Streams API.
     *
     * @param pageable Pagination parameters
     * @return List of watched movies
     */
    public List<Movie> getWatchedMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(Movie::isWatched)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new movie by fetching and merging data from both OMDb and TMDB APIs.
     * Creates a new transaction for the operation.
     *
     * @param title The title of the movie to add
     * @return The saved movie with merged data
     * @throws IllegalArgumentException if movie already exists
     * @throws RuntimeException if API calls fail or data validation fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Movie addMovie(String title) {
        logger.info("Starting movie addition process for title: {}", title);
        Movie savedMovie = null;
        
        try {
            // Check if movie already exists
            logger.info("Checking if movie already exists in database");
            Optional<Movie> existingMovie = movieRepository.findByTitleIgnoreCase(title);
            if (existingMovie.isPresent()) {
                logger.warn("Movie already exists in database: {}", title);
                throw new IllegalArgumentException("Movie already exists in database");
            }
            logger.info("Movie does not exist in database, proceeding with API calls");

            // Fetch movie data from both APIs
            logger.info("Fetching movie data from OMDb API");
            Movie omdbMovie = null;
            try {
                omdbMovie = omdbService.fetchMovieData(title);
                logger.info("Successfully fetched data from OMDb API: {}", omdbMovie.getTitle());
            } catch (Exception e) {
                logger.error("Error fetching data from OMDb API: {}", e.getMessage());
                throw new RuntimeException("Failed to fetch data from OMDb API: " + e.getMessage());
            }

            logger.info("Fetching movie data from TMDB API");
            Movie tmdbMovie = null;
            try {
                tmdbMovie = tmdbService.fetchMovieData(title);
                logger.info("Successfully fetched data from TMDB API: {}", tmdbMovie.getTitle());
            } catch (Exception e) {
                logger.error("Error fetching data from TMDB API: {}", e.getMessage());
                // Don't throw here, as we can still proceed with OMDb data
                logger.warn("Continuing with only OMDb data as TMDB fetch failed");
            }

            // Merge data from both APIs
            logger.info("Merging movie data from both APIs");
            Movie mergedMovie = mergeMovieData(omdbMovie, tmdbMovie);
            logger.info("Successfully merged movie data. Title: {}, Year: {}, Director: {}", 
                mergedMovie.getTitle(), mergedMovie.getYear(), mergedMovie.getDirector());
            
            // Validate merged movie data
            if (mergedMovie.getMovieId() == null) {
                logger.error("Movie ID is null after merging");
                throw new RuntimeException("Movie ID is required but was not set");
            }
            
            // Save to database
            logger.info("Attempting to save movie to database");
            try {
                savedMovie = movieRepository.save(mergedMovie);
                logger.info("Successfully saved movie to database with ID: {}", savedMovie.getId());
                
                // Verify the save by reading back from database
                Optional<Movie> verifyMovie = movieRepository.findById(savedMovie.getId());
                if (verifyMovie.isPresent()) {
                    logger.info("Verified movie exists in database with ID: {}", savedMovie.getId());
                } else {
                    logger.error("Movie not found in database after save!");
                    throw new RuntimeException("Movie not found in database after save");
                }
                
                return savedMovie;
            } catch (Exception e) {
                logger.error("Error saving movie to database: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to save movie to database: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error in addMovie process: {}", e.getMessage(), e);
            // If we saved the movie but encountered an error later, try to delete it
            if (savedMovie != null && savedMovie.getId() != null) {
                try {
                    logger.info("Attempting to clean up saved movie due to error");
                    movieRepository.deleteById(savedMovie.getId());
                } catch (Exception deleteEx) {
                    logger.error("Error cleaning up saved movie: {}", deleteEx.getMessage());
                }
            }
            throw e;
        }
    }

    /**
     * Updates the watched status of a movie.
     *
     * @param id The movie's ID
     * @param watched The new watched status
     * @return The updated movie
     * @throws IllegalArgumentException if movie not found
     */
    @Transactional
    public Movie updateWatchedStatus(Long id, boolean watched) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        
        movie.setWatched(watched);
        return movieRepository.save(movie);
    }

    /**
     * Deletes a movie from the database.
     *
     * @param id The movie's ID
     * @throws IllegalArgumentException if movie not found
     */
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie not found");
        }
        movieRepository.deleteById(id);
    }

    /**
     * Merges movie data from OMDb and TMDB APIs, prioritizing OMDb data for basic information
     * and supplementing with TMDB data when available.
     *
     * @param omdbMovie Movie data from OMDb API
     * @param tmdbMovie Movie data from TMDB API
     * @return Merged movie data
     * @throws RuntimeException if OMDb data is null
     */
    private Movie mergeMovieData(Movie omdbMovie, Movie tmdbMovie) {
        logger.info("Starting movie data merge process");
        Movie mergedMovie = new Movie();
        
        if (omdbMovie == null) {
            logger.error("OMDb movie data is null");
            throw new RuntimeException("OMDb movie data is required");
        }
        
        // Merge basic information from OMDb
        logger.info("Setting basic information from OMDb");
        mergedMovie.setTitle(omdbMovie.getTitle());
        mergedMovie.setYear(omdbMovie.getYear());
        mergedMovie.setDirector(omdbMovie.getDirector());
        mergedMovie.setMovieId(omdbMovie.getMovieId());
        
        // Merge additional details from TMDB if available
        if (tmdbMovie != null) {
            logger.info("Merging additional details from TMDB");
            // If TMDB has a director and OMDb doesn't, use TMDB's
            if (mergedMovie.getDirector() == null || mergedMovie.getDirector().isEmpty()) {
                logger.info("Using director from TMDB");
                mergedMovie.setDirector(tmdbMovie.getDirector());
            }
            
            // If TMDB has a genre, use it
            if (tmdbMovie.getGenre() != null && !tmdbMovie.getGenre().isEmpty()) {
                logger.info("Using genre from TMDB");
                mergedMovie.setGenre(tmdbMovie.getGenre());
            }
        }
        
        // Set default values
        logger.info("Setting default values");
        mergedMovie.setWatched(false);
        mergedMovie.setSimilarMovieTitle(null);
        
        logger.info("Movie merge completed. Final movie data: Title={}, Year={}, Director={}, MovieId={}", 
            mergedMovie.getTitle(), mergedMovie.getYear(), mergedMovie.getDirector(), mergedMovie.getMovieId());
        
        return mergedMovie;
    }
}
