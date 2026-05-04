package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.ECarpetaHabilitadaService;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.UnidadDTO;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.CarpetaHabilitada;
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

    @PostMapping("/registrar")
    public ResponseEntity<CarpetaHabilitada> registrar(@RequestBody CarpetaHabilitada carpeta) {
        return ResponseEntity.ok(carpetaHabilitadaService.registrarHabilitacion(carpeta));
    }

    @GetMapping
    public ResponseEntity<List<CarpetaHabilitada>> listar() {
        return ResponseEntity.ok(carpetaHabilitadaService.listarTodo());
    }

    @GetMapping("/unidad/{codEmpresa}")
    public ResponseEntity<List<UnidadDTO>> buscarPorUnidad(@PathVariable String codEmpresa) {
        return ResponseEntity.ok(carpetaHabilitadaService.obtenerUnidadesFront(codEmpresa));
    }


}