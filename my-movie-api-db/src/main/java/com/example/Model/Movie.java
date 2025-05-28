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

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int unsigned")
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "year", length = 20)
    private String year;

    @Column(name = "director", length = 255)
    private String director;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "watched", columnDefinition = "tinyint(1)")
    private Boolean watched;

    @Column(name = "similar_movie_title", length = 255)
    private String similarMovieTitle;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieImage> images = new ArrayList<>();

    // Default constructor
    public Movie() {
    }

    // Add helper method to manage bidirectional relationship
    public void addImage(MovieImage image) {
        images.add(image);
        image.setMovie(this);
    }

    public void removeImage(MovieImage image) {
        images.remove(image);
        image.setMovie(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Boolean isWatched() {
        return watched;
    }

    public void setWatched(Boolean watched) {
        this.watched = watched;
    }

    public String getSimilarMovieTitle() {
        return similarMovieTitle;
    }

    public void setSimilarMovieTitle(String similarMovieTitle) {
        this.similarMovieTitle = similarMovieTitle;
    }

    public List<MovieImage> getImages() {
        return images;
    }

    public void setImages(List<MovieImage> images) {
        this.images = images;
    }
}
