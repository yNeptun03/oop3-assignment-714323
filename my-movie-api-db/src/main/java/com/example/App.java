package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main application class that serves as the entry point for the Spring Boot application.
 * This class is annotated with {@link SpringBootApplication} to enable auto-configuration,
 * component scanning, and to define the configuration class.
 *
 * @author Your Name
 * @version 1.0
 * @since 1.0
 */
@SpringBootApplication
public class App 
{
    /**
     * The main method that starts the Spring Boot application.
     * This method uses SpringApplication.run() to bootstrap and launch the application.
     *
     * @param args Command line arguments passed to the application
     */
    public static void main( String[] args )
    {
        SpringApplication.run(App.class, args);
    }
}
