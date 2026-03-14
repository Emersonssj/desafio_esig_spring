package com.esig.feed.controller;

import com.esig.feed.dto.AuthDTO;
import com.esig.feed.model.User;
import com.esig.feed.repository.UserRepository;
import com.esig.feed.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserRepository repository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);

            // Aqui você gera o token normalmente e retorna o 200 OK
            var token = tokenService.generateToken((User) auth.getPrincipal());
            return ResponseEntity.ok(Map.of("token", token));

        } catch (AuthenticationException e) {
            // Se a senha estiver errada ou o usuário não existir, cai aqui!
            // Retornamos o 401 Unauthorized com a estrutura exata que o seu Flutter espera:
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", 401,
                            "title", "Não Autorizado",
                            "userMessage", "Usuário ou senha incorretos."
                    ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody AuthDTO data) {
        if(this.repository.findByLogin(data.login()) != null) return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.login(), encryptedPassword);

        this.repository.save(newUser);
        return ResponseEntity.ok().build();
    }
}