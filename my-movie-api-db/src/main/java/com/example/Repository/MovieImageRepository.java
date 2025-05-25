package com.example.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Model.Movie;
import com.example.Model.MovieImage;

@Repository
public interface MovieImageRepository extends JpaRepository<MovieImage, Long>{
    List<MovieImage> findByMovie (Movie movie);
    Optional<MovieImage> findByMovieAndImageType(Movie movie, String image);
    
}
