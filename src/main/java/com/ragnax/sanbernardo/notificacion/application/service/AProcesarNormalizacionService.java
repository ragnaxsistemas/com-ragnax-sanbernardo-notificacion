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
    private final AsyncTaskService asyncTaskService;

    public ResponseEntity<?> procesarSubida(String carpetaRaiz,
                                            String tipo,
                                            String unidad,
                                            String usuario,
                                            String observacion,
                                            MultipartFile file) throws IOException {

        // 1. PASO SÍNCRONO (Rápido):
        // Validamos y guardamos el archivo en el storage (Mac/Windows).
        // Esto asegura que el archivo ya existe en disco antes de responder.
        EjecutarUpload ejecutarUpload = storageService.procesarSubida(carpetaRaiz, tipo, unidad, usuario, observacion, file);

        asyncTaskService.ejecutarNormalizacionAsync(ejecutarUpload);
        // 2. PASO ASÍNCRONO (Segundo plano):
        // Disparamos la normalización sin esperar a que termine.
        //this.ejecutarNormalizacionAsync(ejecutarUpload, file);

        // 3. RESPUESTA INMEDIATA:
        // Enviamos un 202 Accepted para que Angular cierre el modal.
        return ResponseEntity.accepted().body(Map.of(
                "message", "Archivo recibido. Procesando en segundo plano...",
                "ruta", ejecutarUpload.getNombreArchivoUpload(),
                "carpeta", ejecutarUpload.getNombreArchivoUpload()
        ));
    }
}
