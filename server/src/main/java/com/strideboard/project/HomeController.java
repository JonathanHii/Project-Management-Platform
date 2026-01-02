package com.strideboard.project;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home(Principal principal) {
        // Returning a Map ensures the frontend receives valid JSON: {"message": "Welcome..."}
        String welcomeMsg = "Welcome to Strideboard, " + (principal != null ? principal.getName() : "Guest") + "!";
        return Collections.singletonMap("message", welcomeMsg);
    }
}