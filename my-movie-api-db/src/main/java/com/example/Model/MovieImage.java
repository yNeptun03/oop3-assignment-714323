package com.example.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entity class representing a movie image in the database.
 * Stores different types of images (posters and backdrops) associated with movies.
 * 
 * The images are stored as binary data (LONGBLOB) in the database with their content type
 * and type classification (POSTER or BACKDROP).
 * 
 * @see Movie
 */
@Entity
@Table(name = "movie_images")
public class MovieImage {
    
    /**
     * Unique identifier for the movie image.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The movie this image is associated with.
     * Many images can belong to one movie (Many-to-One relationship).
     */
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false, columnDefinition = "int unsigned")
    private Movie movie;

    /**
     * The type of image (e.g., "POSTER" or "BACKDROP").
     * Cannot be null.
     */
    @Column(name = "image_type", nullable = false)
    private String imageType;

    /**
     * The binary data of the image.
     * Stored as LONGBLOB in the database.
     * Cannot be null.
     */
    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] imageData;

    /**
     * The MIME type of the image (e.g., "image/jpeg", "image/png").
     * Cannot be null.
     */
    @Column(name = "content_type", nullable = false)
    private String contentType;

    /**
     * Default constructor required by JPA.
     */
    public MovieImage() {
    }

    /**
     * Gets the unique identifier of the image.
     *
     * @return the image ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the image.
     *
     * @param id the image ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the movie associated with this image.
     *
     * @return the associated movie
     */
    public Movie getMovie() {
        return movie;
    }

    /**
     * Sets the movie associated with this image.
     *
     * @param movie the movie to associate with this image
     */
    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    /**
     * Gets the type of the image (POSTER or BACKDROP).
     *
     * @return the image type
     */
    public String getImageType() {
        return imageType;
    }

    /**
     * Sets the type of the image.
     *
     * @param imageType the type to set (e.g., "POSTER" or "BACKDROP")
     */
    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * Gets the binary data of the image.
     *
     * @return the image data as byte array
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * Sets the binary data of the image.
     *
     * @param imageData the image data to set
     */
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    /**
     * Gets the MIME type of the image.
     *
     * @return the content type (e.g., "image/jpeg")
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the MIME type of the image.
     *
     * @param contentType the content type to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
} 