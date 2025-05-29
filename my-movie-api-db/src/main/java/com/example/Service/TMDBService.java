package com.example.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.Model.Movie;
import com.example.Model.MovieImage;
import com.example.Repository.MovieImageRepository;

/**
 * Service class that handles interactions with The Movie Database (TMDB) API.
 * Provides functionality to fetch movie details, images, and similar movies.
 */
@Service
public class TMDBService {

    @Value("${tmdb.api.key}")
    private String apiKey;

    @Value("${tmdb.api.url}")
    private String apiUrl;

    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w500";  // You can use: w92, w154, w185, w342, w500, w780, original
    private static final String BACKDROP_SIZE = "w1280"; // You can use: w300, w780, w1280, original

    // TMDB Genre IDs to Names mapping
    private static final Map<Integer, String> GENRE_MAP = new HashMap<>();
    static {
        GENRE_MAP.put(28, "Action");
        GENRE_MAP.put(12, "Adventure");
        GENRE_MAP.put(16, "Animation");
        GENRE_MAP.put(35, "Comedy");
        GENRE_MAP.put(80, "Crime");
        GENRE_MAP.put(99, "Documentary");
        GENRE_MAP.put(18, "Drama");
        GENRE_MAP.put(10751, "Family");
        GENRE_MAP.put(14, "Fantasy");
        GENRE_MAP.put(36, "History");
        GENRE_MAP.put(27, "Horror");
        GENRE_MAP.put(10402, "Music");
        GENRE_MAP.put(9648, "Mystery");
        GENRE_MAP.put(10749, "Romance");
        GENRE_MAP.put(878, "Science Fiction");
        GENRE_MAP.put(10770, "TV Movie");
        GENRE_MAP.put(53, "Thriller");
        GENRE_MAP.put(10752, "War");
        GENRE_MAP.put(37, "Western");
    }

    private final RestTemplate restTemplate;
    private final MovieImageRepository movieImageRepository;

    @Autowired
    public TMDBService(RestTemplate restTemplate, MovieImageRepository movieImageRepository) {
        this.restTemplate = restTemplate;
        this.movieImageRepository = movieImageRepository;
    }

    /**
     * Fetches movie data from TMDB API based on the movie title.
     * Retrieves movie details including title, year, genre, director, and images.
     *
     * @param title The title of the movie to search for
     * @return Movie object containing the fetched movie data
     * @throws RuntimeException if movie not found or API errors occur
     */
    @Transactional
    public Movie fetchMovieData(String title) {
        try {
            // First, search for the movie to get its ID
            String searchUrl = UriComponentsBuilder.fromHttpUrl(apiUrl + "/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", title)
                    .build()
                    .toUriString();

            TMDBSearchResponse searchResponse = restTemplate.getForObject(searchUrl, TMDBSearchResponse.class);
            
            if (searchResponse == null) {
                throw new RuntimeException("Failed to get response from TMDB API");
            }

            if (searchResponse.status_code != null) {
                handleTMDBError(searchResponse.status_code, searchResponse.status_message);
            }

            if (searchResponse.results == null || searchResponse.results.isEmpty()) {
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

            if (detailResponse.status_code != null) {
                handleTMDBError(detailResponse.status_code, detailResponse.status_message);
            }

            Movie movie = new Movie();
            movie.setMovieId((long) firstResult.id);
            movie.setTitle(detailResponse.title);
            movie.setYear(detailResponse.release_date != null ? detailResponse.release_date.substring(0, 4) : null);
            movie.setDirector(detailResponse.director);
            
            // Get genre name from the first genre_id
            String genre = firstResult.genre_ids != null && !firstResult.genre_ids.isEmpty() 
                ? GENRE_MAP.getOrDefault(firstResult.genre_ids.get(0), "Unknown")
                : null;
            movie.setGenre(genre);
            
            movie.setWatched(false);

            String similarMovieTitle = fetchSimilarMovie(firstResult.id);
            movie.setSimilarMovieTitle(similarMovieTitle);



            // Try to download and store images, but continue even if it fails
            try {
                if (detailResponse.poster_path != null) {
                    try {
                        String posterUrl = TMDB_IMAGE_BASE_URL + POSTER_SIZE + detailResponse.poster_path;
                        byte[] posterData = downloadImage(posterUrl);
                        if (posterData != null && posterData.length > 0) {
                            saveMovieImage(movie, posterData, "POSTER", MediaType.IMAGE_JPEG_VALUE);
                        }
                    } catch (Exception e) {
                        // Log the error but continue
                        System.err.println("Failed to download poster image: " + e.getMessage());
                    }
                }
                
                if (detailResponse.backdrop_path != null) {
                    try {
                        String backdropUrl = TMDB_IMAGE_BASE_URL + BACKDROP_SIZE + detailResponse.backdrop_path;
                        byte[] backdropData = downloadImage(backdropUrl);
                        if (backdropData != null && backdropData.length > 0) {
                            saveMovieImage(movie, backdropData, "BACKDROP", MediaType.IMAGE_JPEG_VALUE);
                        }
                    } catch (Exception e) {
                        // Log the error but continue
                        System.err.println("Failed to download backdrop image: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // Log the error but continue with movie saving
                System.err.println("Error processing movie images: " + e.getMessage());
            }

            return movie;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid TMDB API key. Please check your configuration.");
            }
            throw new RuntimeException("TMDB API error: " + e.getMessage());
        }
    }

    private void handleTMDBError(Integer statusCode, String statusMessage) {
        if (statusCode == 7) {
            throw new RuntimeException("Invalid TMDB API key. Please check your configuration.");
        } else if (statusCode == 34) {
            throw new RuntimeException("Movie not found in TMDB");
        } else {
            throw new RuntimeException("TMDB API error: " + statusMessage);
        }
    }

    private void saveMovieImage(Movie movie, byte[] imageData, String imageType, String contentType) {
        MovieImage movieImage = new MovieImage();
        movieImage.setImageType(imageType);
        movieImage.setImageData(imageData);
        movieImage.setContentType(contentType);
        movie.addImage(movieImage);
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

    // Updated inner classes to include error fields
    private static class TMDBSearchResponse {
        public List<TMDBMovieResult> results;
        public Integer status_code;
        public String status_message;
    }

    private static class TMDBMovieResult {
        public int id;
        public String title;
        public List<Integer> genre_ids;
    }

    private static class TMDBMovieDetail {
        public String title;
        public String release_date;
        public String poster_path;
        public String backdrop_path;
        public String director;
        public Integer status_code;
        public String status_message;
    }

    private static class TMDBSimilarMoviesResponse {
        public List<TMDBMovieResult> results;
        public Integer status_code;
        public String status_message;
    }

    private String fetchSimilarMovie(int movieId) {
        try {
            String similarMoviesUrl = UriComponentsBuilder.fromHttpUrl(apiUrl + "/movie/" + movieId + "/similar")
                    .queryParam("api_key", apiKey)
                    .build()
                    .toUriString();

            TMDBSimilarMoviesResponse similarResponse = restTemplate.getForObject(similarMoviesUrl, TMDBSimilarMoviesResponse.class);
            
            if (similarResponse == null || similarResponse.results == null || similarResponse.results.isEmpty()) {
                return null;
            }

            // Get the first similar movie's title
            return similarResponse.results.get(0).title;
        } catch (Exception e) {
            System.err.println("Error fetching similar movies: " + e.getMessage());
            return null;
        }
    }
} 