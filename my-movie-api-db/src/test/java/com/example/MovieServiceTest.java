package com.example;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.Model.Movie;
import com.example.Repository.MovieRepository;
import com.example.Service.MovieService;
import com.example.Service.OMDbService;
import com.example.Service.TMDBService;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private OMDbService omdbService;

    @Mock
    private TMDBService tmdbService;

    @Mock
    private MovieRepository movieRepository;

    @Test
    void testAddMovieReturnsMergedMovie() {
        // Arrange
        Movie omdb = new Movie();
        omdb.setTitle("Flash");
        omdb.setMovieId(123L);
        when(omdbService.fetchMovieData("Flash")).thenReturn(omdb);

        Movie tmdb = new Movie();
        tmdb.setTitle("Flash");
        tmdb.setMovieId(123L);
        when(tmdbService.fetchMovieData("Flash")).thenReturn(tmdb);

        Movie savedMovie = new Movie();
        savedMovie.setId(15L);
        savedMovie.setTitle("Flash");
        savedMovie.setMovieId(123L);

        when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);
        when(movieRepository.findByTitleIgnoreCase("Flash")).thenReturn(Optional.of(savedMovie));

        // Act
        Movie result = movieService.addMovie("Flash");

        // Assert
        assertEquals("Flash", result.getTitle());
        verify(movieRepository).save(any(Movie.class));
    }
}
