package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.AProcesarNormalizacionService;
import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload") // Cambiado para evitar colisiones con "/"
//@CrossOrigin(origins = "*")
@Slf4j
public class AFileController {


    private final AFileStorageComponent storageService;

    private final AProcesarNormalizacionService procesarNormalizacion;

    @PostMapping("/{tipo}/{unidad}")
    public ResponseEntity<?> upload(
            @PathVariable String tipo,
            @PathVariable String unidad,
            @RequestParam(value = "user", required = false, defaultValue = "") String user,
            @RequestParam(value = "observacion", required = false, defaultValue = "") String observacion,
            @RequestParam("archivo") MultipartFile file) {

        try {
            // Validamos y procesamos. El servicio ahora responderá casi al instante.
            return procesarNormalizacion.procesarSubida("upload", tipo, unidad, user, observacion, file);

        } catch (Exception e) {
            log.error("Error crítico en upload: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "No se pudo recibir el archivo: " + e.getMessage()));
        }
    }
}
