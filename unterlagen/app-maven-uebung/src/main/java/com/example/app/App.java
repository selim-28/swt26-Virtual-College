package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {
    @GetMapping("/api/hello")
    public MessageResponse hello() {
        return new MessageResponse("Replace this starter module with your project implementation.");
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    public record MessageResponse(String message) {
    }
}
