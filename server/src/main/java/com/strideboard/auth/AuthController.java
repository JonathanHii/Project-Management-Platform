package com.strideboard.auth;

import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.strideboard.data.user.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final TokenService tokenService;
    private final JpaUserDetailsService userDetailsService;

    public AuthController(TokenService tokenService, JpaUserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public String token(Authentication auth) {
        return tokenService.generateToken(auth);
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest registration) {
        User user = userDetailsService.registerUser(registration);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.emptyList());

        return tokenService.generateToken(authentication);
    }

    @GetMapping("/check")
    public boolean checkTokenStatus(Authentication authentication) {
        // If the code reaches here, roken validated
        return true;
    }

}