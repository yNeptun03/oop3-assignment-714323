package com.example.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.Model.Movie;


@Service
public class OMDbService {
    
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
        OMDbResponse response = restTemplate.getForObject(url, OMDbResponse.class);

        if (response == null || !"True".equals(response.Response)) {
            throw new RuntimeException("Movie not found in OMDb:" + title);

        } 

        Movie movie = new Movie();
        movie.setTitle(response.Title);
        movie.setYear(Integer.parseInt(response.Year));
        movie.setImdbId(response.imdbID);
        movie.setOverview(response.Plot);

        if (response.Released != null && !response.Released.equals("N/A")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            movie.setReleaseDate(LocalDate.parse(response.Released, formatter));
        }

        return movie;   

    }

    private static class OMDbResponse {
        public String Title;
        public String Year;
        public String Released;
        public String Plot;
        public String imdbID;
        public String Response;
    }
    
}
