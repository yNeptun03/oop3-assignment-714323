package com.example;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.Controller.MovieController;
import com.example.Model.Movie;
import com.example.Service.MovieService;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Test
    @DisplayName("GET /api/movies returns paginated movies")
    void testGetAllMovies() throws Exception {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        Mockito.when(movieService.getAllMovies(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(movie), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Movie"));
    }

    @Test
    @DisplayName("GET /api/movies/{id} returns a movie")
    void testGetMovieById() throws Exception {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        Mockito.when(movieService.getMovieById(1L)).thenReturn(Optional.of(movie));

        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    @DisplayName("POST /api/movies adds a movie")
    void testAddMovie() throws Exception {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        Mockito.when(movieService.addMovie("Test Movie")).thenReturn(movie);

        mockMvc.perform(post("/api/movies")
                        .param("title", "Test Movie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Movie"));
    }

    @Test
    @DisplayName("PATCH /api/movies/{id}/watched updates watched status")
    void testUpdateWatchedStatus() throws Exception {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setWatched(true);
        Mockito.when(movieService.updateWatchedStatus(1L, true)).thenReturn(movie);

        mockMvc.perform(patch("/api/movies/1/watched")
                        .param("watched", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watched").value(true));
    }

    @Test
    @DisplayName("DELETE /api/movies/{id} deletes a movie")
    void testDeleteMovie() throws Exception {
        Mockito.doNothing().when(movieService).deleteMovie(1L);

        mockMvc.perform(delete("/api/movies/1"))
                .andExpect(status().isNoContent());
    }
}
