package com.example.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity class representing a movie in the database.
 * Stores movie information including title, year, director, genre, and associated images.
 * Maintains a one-to-many relationship with MovieImage entities for storing movie posters and backdrops.
 * 
 * @see MovieImage
 */
@Entity
@Table(name = "movies")
public class Movie {
    /**
     * Unique identifier for the movie in the database.
     * Auto-generated using database identity strategy.
     * Stored as unsigned integer.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int unsigned")
    private Long id;

    /**
     * External movie ID (e.g., from TMDB or IMDB).
     * Cannot be null.
     */
    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    /**
     * Title of the movie.
     * Maximum length of 255 characters.
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Release year of the movie.
     * Maximum length of 20 characters.
     */
    @Column(name = "year", length = 20)
    private String year;

    /**
     * Director of the movie.
     * Maximum length of 255 characters.
     */
    @Column(name = "director", length = 255)
    private String director;

    /**
     * Genre of the movie.
     * Maximum length of 255 characters.
     */
    @Column(name = "genre", length = 255)
    private String genre;

    /**
     * Flag indicating whether the movie has been watched.
     * Stored as tinyint(1) in the database.
     */
    @Column(name = "watched", columnDefinition = "tinyint(1)")
    private Boolean watched;

    /**
     * Title of a similar movie recommendation.
     * Maximum length of 255 characters.
     */
    @Column(name = "similar_movie_title", length = 255)
    private String similarMovieTitle;

    /**
     * List of images associated with this movie (posters and backdrops).
     * One-to-many relationship with cascade operations and orphan removal.
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieImage> images = new ArrayList<>();

    /**
     * Default constructor required by JPA.
     */
    public Movie() {
    }

    /**
     * Adds an image to the movie and maintains the bidirectional relationship.
     *
     * @param image the image to add
     */
    public void addImage(MovieImage image) {
        images.add(image);
        image.setMovie(this);
    }

    /**
     * Removes an image from the movie and updates the bidirectional relationship.
     *
     * @param image the image to remove
     */
    public void removeImage(MovieImage image) {
        images.remove(image);
        image.setMovie(null);
    }

    /**
     * Gets the database ID of the movie.
     *
     * @return the movie's database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the movie.
     *
     * @param id the ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the external movie ID (e.g., from TMDB or IMDB).
     *
     * @return the external movie ID
     */
    public Long getMovieId() {
        return movieId;
    }

    /**
     * Sets the external movie ID.
     *
     * @param movieId the external movie ID to set
     */
    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    /**
     * Gets the title of the movie.
     *
     * @return the movie title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the movie.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the release year of the movie.
     *
     * @return the release year
     */
    public String getYear() {
        return year;
    }

    /**
     * Sets the release year of the movie.
     *
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    /**
     * Gets the director of the movie.
     *
     * @return the director's name
     */
    public String getDirector() {
        return director;
    }

    /**
     * Sets the director of the movie.
     *
     * @param director the director's name to set
     */
    public void setDirector(String director) {
        this.director = director;
    }

    /**
     * Gets the genre of the movie.
     *
     * @return the movie genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Sets the genre of the movie.
     *
     * @param genre the genre to set
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Checks if the movie has been watched.
     *
     * @return true if the movie has been watched, false otherwise
     */
    public Boolean isWatched() {
        return watched;
    }

    /**
     * Sets the watched status of the movie.
     *
     * @param watched the watched status to set
     */
    public void setWatched(Boolean watched) {
        this.watched = watched;
    }

    /**
     * Gets the title of a similar movie recommendation.
     *
     * @return the similar movie title
     */
    public String getSimilarMovieTitle() {
        return similarMovieTitle;
    }

    /**
     * Sets the title of a similar movie recommendation.
     *
     * @param similarMovieTitle the similar movie title to set
     */
    public void setSimilarMovieTitle(String similarMovieTitle) {
        this.similarMovieTitle = similarMovieTitle;
    }

    /**
     * Gets the list of images associated with this movie.
     *
     * @return the list of movie images
     */
    public List<MovieImage> getImages() {
        return images;
    }

    /**
     * Sets the list of images associated with this movie.
     * Note: It's recommended to use addImage() and removeImage() methods instead
     * to maintain the bidirectional relationship.
     *
     * @param images the list of images to set
     */
    public void setImages(List<MovieImage> images) {
        this.images = images;
    }
}
