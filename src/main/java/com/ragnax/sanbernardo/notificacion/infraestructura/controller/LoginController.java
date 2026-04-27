package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.LoginService;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.JwtUtil;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.LoginRequest;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.LoginResponse;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class LoginController {

    private final LoginService usuarioService;

    // --- UPLOAD ---
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request){

        LoginResponse loginResponse =
                usuarioService.login(request.getUsername(), request.getPassword(), request.getCodEmpresa());

        TokenResponse tokenResponse = JwtUtil.generateToken(loginResponse);

        return ResponseEntity.ok(tokenResponse);
    }
}
