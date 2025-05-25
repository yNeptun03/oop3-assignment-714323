package com.example.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.Model.Movie;
import com.example.Model.MovieImage;
import com.example.Repository.MovieImageRepository;

@Service
public class TMDBService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.url}")
    private String apiUrl;

    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w500";  // You can use: w92, w154, w185, w342, w500, w780, original
    private static final String BACKDROP_SIZE = "w1280"; // You can use: w300, w780, w1280, original

    private final RestTemplate restTemplate;
    private final MovieImageRepository movieImageRepository;

    @Autowired
    public TMDBService(RestTemplate restTemplate, MovieImageRepository movieImageRepository) {
        this.restTemplate = restTemplate;
        this.movieImageRepository = movieImageRepository;
    }

    @Transactional
    public Movie fetchMovieData(String title) {
        // First, search for the movie to get its ID
        String searchUrl = UriComponentsBuilder.fromHttpUrl(apiUrl + "/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", title)
                .build()
                .toUriString();

        TMDBSearchResponse searchResponse = restTemplate.getForObject(searchUrl, TMDBSearchResponse.class);
        
        if (searchResponse == null || searchResponse.results.isEmpty()) {
            throw new RuntimeException("Movie not found in TMDB: " + title);
        }

        // Get the first result
        TMDBMovieResult firstResult = searchResponse.results.get(0);

        // Now fetch detailed movie information
        String detailUrl = UriComponentsBuilder.fromHttpUrl(apiUrl + "/movie/" + firstResult.id)
                .queryParam("api_key", apiKey)
                .build()
                .toUriString();

        TMDBMovieDetail detailResponse = restTemplate.getForObject(detailUrl, TMDBMovieDetail.class);
        
        if (detailResponse == null) {
            throw new RuntimeException("Failed to fetch movie details from TMDB");
        }

        Movie movie = new Movie();
        movie.setTitle(detailResponse.title);
        movie.setOverview(detailResponse.overview);
        movie.setVoteAverage(detailResponse.vote_average);
        
        if (detailResponse.release_date != null && !detailResponse.release_date.isEmpty()) {
            movie.setReleaseDate(LocalDate.parse(detailResponse.release_date));
        }

        // Download and store images in separate table
        try {
            if (detailResponse.poster_path != null) {
                String posterUrl = TMDB_IMAGE_BASE_URL + POSTER_SIZE + detailResponse.poster_path;
                byte[] posterData = downloadImage(posterUrl);
                saveMovieImage(movie, posterData, "POSTER", MediaType.IMAGE_JPEG_VALUE);
            }
            
            if (detailResponse.backdrop_path != null) {
                String backdropUrl = TMDB_IMAGE_BASE_URL + BACKDROP_SIZE + detailResponse.backdrop_path;
                byte[] backdropData = downloadImage(backdropUrl);
                saveMovieImage(movie, backdropData, "BACKDROP", MediaType.IMAGE_JPEG_VALUE);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to download movie images", e);
        }

        return movie;
    }

    private void saveMovieImage(Movie movie, byte[] imageData, String imageType, String contentType) {
        MovieImage movieImage = new MovieImage();
        movieImage.setMovie(movie);
        movieImage.setImageType(imageType);
        movieImage.setImageData(imageData);
        movieImage.setContentType(contentType);
        movieImageRepository.save(movieImage);
    }

    private byte[] downloadImage(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toByteArray();
        }
    }

    // Inner classes to map TMDB API responses
    private static class TMDBSearchResponse {
        public List<TMDBMovieResult> results;
    }

    private static class TMDBMovieResult {
        public int id;
        public String title;
    }

    private static class TMDBMovieDetail {
        public String title;
        public String overview;
        public String poster_path;
        public String backdrop_path;
        public double vote_average;
        public String release_date;
    }
} 