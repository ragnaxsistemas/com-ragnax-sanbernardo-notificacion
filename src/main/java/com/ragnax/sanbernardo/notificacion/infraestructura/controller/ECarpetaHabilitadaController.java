/***package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.ECarpetaHabilitadaService;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.UnidadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carpetas-habilitadas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ECarpetaHabilitadaController {

    private final ECarpetaHabilitadaService carpetaHabilitadaService;

    @GetMapping
    public ResponseEntity<List<CarpetaHabilitada>> listar() {
        return ResponseEntity.ok(carpetaHabilitadaService.listarTodo());
    }





}***/