package com.example.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Model.Movie;
import com.example.Model.MovieImage;

/**
 * Repository interface for MovieImage entity that extends JpaRepository.
 * Provides operations for managing movie images including posters and backdrops.
 * 
 * @see JpaRepository
 * @see MovieImage
 * @see Movie
 */
@Repository
public interface MovieImageRepository extends JpaRepository<MovieImage, Long> {
    /**
     * Finds all images associated with a specific movie.
     *
     * @param movie The movie to find images for
     * @return List of movie images associated with the given movie
     */
    List<MovieImage> findByMovie(Movie movie);

    /**
     * Finds a specific type of image (e.g., "POSTER" or "BACKDROP") for a given movie.
     *
     * @param movie The movie to find the image for
     * @param image The type of image to find (e.g., "POSTER", "BACKDROP")
     * @return Optional containing the movie image if found, empty Optional if no matching image exists
     */
    Optional<MovieImage> findByMovieAndImageType(Movie movie, String image);
}
