package com.example.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Model.Movie;

/**
 * Repository interface for Movie entity that extends JpaRepository.
 * Provides basic CRUD operations and custom query methods for movie data.
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    /**
     * Finds a movie by its title, ignoring case sensitivity.
     * This method is case-insensitive, so searching for "Inception" will match "inception" or "INCEPTION".
     *
     * @param title The title of the movie to search for
     * @return Optional containing the movie if found, empty Optional if no movie matches
     */
    Optional<Movie> findByTitleIgnoreCase(String title);
}
