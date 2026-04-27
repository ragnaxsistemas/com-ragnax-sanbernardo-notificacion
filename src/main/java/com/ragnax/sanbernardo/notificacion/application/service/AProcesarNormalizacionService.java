package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AProcesarNormalizacionService {

    private final AFileStorageComponent storageService;

    private final BUploadToDesnormalizadoService uploadToDesnormalizadoService;

    // --- LÓGICA DE SUBIDA CENTRALIZADA ---
    public ResponseEntity<?> procesarSubida(String carpetaRaiz,
                                            String tipo,
                                            String unidad,
                                            String usuario,
                                            String observacion,
                                            MultipartFile file) throws IOException {

        EjecutarUpload ejecutarUpload = storageService.procesarSubida(carpetaRaiz, tipo, unidad, usuario, observacion, file);

        ejecutarUpload.setFile(file);
        uploadToDesnormalizadoService.normalizarArchivo(ejecutarUpload);

        return ResponseEntity.ok(Map.of(
                "message", "Subida exitosa",
                "ruta", ejecutarUpload.getNombreArchivoUpload()
        ));
    }
}
