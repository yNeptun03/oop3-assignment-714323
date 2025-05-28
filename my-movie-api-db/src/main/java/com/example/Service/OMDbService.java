package com.example.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.Model.Movie;

@Service
public class OMDbService {
    private static final Logger logger = LoggerFactory.getLogger(OMDbService.class);
    
    @Value("${omdb.api.key}")
    private String apiKey;

    @Value("${omdb.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public OMDbService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Movie fetchMovieData(String title) {
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("apikey", apiKey)
                .queryParam("t", title)
                .queryParam("type", "movie")
                .build()
                .toUriString();
        
        logger.info("Calling OMDb API with URL: {}", url);
        OMDbResponse response = restTemplate.getForObject(url, OMDbResponse.class);
        logger.info("OMDb API Response: {}", response);

        if (response == null) {
            logger.error("OMDb API returned null response");
            throw new RuntimeException("Failed to get response from OMDb API");
        }

        // Check for API key error
        if (response.Error != null) {
            logger.error("OMDb API error: {}", response.Error);
            if (response.Error.contains("Invalid API key")) {
                throw new RuntimeException("Invalid OMDb API key. Please check your configuration.");
            }
            throw new RuntimeException("OMDb API error: " + response.Error);
        }

        if (!"True".equals(response.Response)) {
            logger.error("Movie not found in OMDb: {}", title);
            throw new RuntimeException("Movie not found in OMDb: " + title);
        } 

        logger.info("Successfully found movie in OMDb: {}", response.Title);
        Movie movie = new Movie();
        movie.setTitle(response.Title);
        movie.setYear(response.Year);
        movie.setDirector(response.Director);
        
        // Set movieId from IMDB ID (remove 'tt' prefix and convert to long)
        if (response.imdbID != null && response.imdbID.startsWith("tt")) {
            try {
                long movieId = Long.parseLong(response.imdbID.substring(2));
                movie.setMovieId(movieId);
                logger.info("Set movieId from IMDB ID: {}", movieId);
            } catch (NumberFormatException e) {
                logger.error("Invalid IMDB ID format: {}", response.imdbID);
                throw new RuntimeException("Invalid IMDB ID format: " + response.imdbID);
            }
        } else {
            logger.error("Invalid or missing IMDB ID: {}", response.imdbID);
            throw new RuntimeException("Invalid or missing IMDB ID");
        }
        
        // Set default values
        movie.setWatched(false);
        movie.setSimilarMovieTitle(null);

        return movie;   
    }

    private static class OMDbResponse {
        public String Title;
        public String Year;
        public String Released;
        public String Plot;
        public String imdbID;
        public String Response;
        public String Director;
        public String Error;

        @Override
        public String toString() {
            return "OMDbResponse{" +
                    "Title='" + Title + '\'' +
                    ", Year='" + Year + '\'' +
                    ", imdbID='" + imdbID + '\'' +
                    ", Response='" + Response + '\'' +
                    ", Director='" + Director + '\'' +
                    ", Error='" + Error + '\'' +
                    '}';
        }
    }
}
