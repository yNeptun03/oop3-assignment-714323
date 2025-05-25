package com.example.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Model.Movie;
import com.example.Repository.MovieRepository;
/*import com.example.Service.OMDbService;
import com.example.Service.TMDBService;/* */

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private OMDbService omdbService;

    @Autowired
    private TMDBService tmdbService;

    public Page<Movie> getAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable);
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional
    public Movie addMovie(String title) {
        // Check if movie already exists
        Optional<Movie> existingMovie = movieRepository.findByTitleIgnoreCase(title);
        if (existingMovie.isPresent()) {
            throw new IllegalArgumentException("Movie already exists in database");
        }

        // Fetch movie data from both APIs
        Movie omdbMovie = omdbService.fetchMovieData(title);
        Movie tmdbMovie = tmdbService.fetchMovieData(title);

        // Merge data from both APIs
        Movie mergedMovie = mergeMovieData(omdbMovie, tmdbMovie);
        
        // Save to database
        return movieRepository.save(mergedMovie);
    }

    @Transactional
    public Movie updateWatchedStatus(Long id, boolean watched) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        
        movie.setWatched(watched);
        return movieRepository.save(movie);
    }

    @Transactional
    public Movie updateRating(Long id, int rating) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        
        movie.setRating(rating);
        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new IllegalArgumentException("Movie not found");
        }
        movieRepository.deleteById(id);
    }

    private Movie mergeMovieData(Movie omdbMovie, Movie tmdbMovie) {
        Movie mergedMovie = new Movie();
        
        // Merge basic information
        mergedMovie.setTitle(omdbMovie.getTitle());
        mergedMovie.setYear(omdbMovie.getYear());
        mergedMovie.setImdbId(omdbMovie.getImdbId());
        
        // Merge additional details from TMDB
        if (tmdbMovie != null) {
            mergedMovie.setOverview(tmdbMovie.getOverview());
            mergedMovie.setReleaseDate(tmdbMovie.getReleaseDate());
            mergedMovie.setVoteAverage(tmdbMovie.getVoteAverage());
        }
        
        // Set default values
        mergedMovie.setWatched(false);
        mergedMovie.setRating(0);
        
        return mergedMovie;
    }
}
