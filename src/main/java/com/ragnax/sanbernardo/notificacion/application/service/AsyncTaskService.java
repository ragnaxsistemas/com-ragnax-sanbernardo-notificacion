package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTaskService {

    private final CCorreosToNormalizeService cCorreosToNormalizeService;
    private final BUploadToDesnormalizadoService uploadToDesnormalizadoService;

    @Async("taskExecutor")
    // 1. QUITAMOS el MultipartFile de los parámetros
    public void ejecutarNormalizacionAsync(EjecutarUpload ejecutarUpload) {
        try {
            log.info("Iniciando normalización asíncrona para: {}", ejecutarUpload.getNombreArchivoUpload());

            // 2. YA NO HACEMOS ejecutarUpload.setFile(file)
            // La lógica dentro de normalizarArchivo debe leer el archivo
            // usando la ruta (Path) que ya viene dentro de ejecutarUpload.

            uploadToDesnormalizadoService.normalizarArchivo(ejecutarUpload);

            log.info("Proceso asíncrono terminado con éxito.");
        } catch (Exception e) {
            log.error("Error crítico en segundo plano: {}", e.getMessage());
            // IMPORTANTE: Aquí podrías actualizar un estado en base de datos
            // indicando que el proceso falló para que el front lo sepa.
        }
    }

    @Async("taskExecutor")
    public void ejecutarMergeAsync(EjecutarMerge ejecutarMerge) {
        try {
            log.info("Iniciando Merge asíncrono para usuario: {}", ejecutarMerge.getUsuarioMerge());

            // 1. Ejecutar el cruce de datos (Merge)
            //EjecutarMerge resultadoMerge = cCorreosToNormalizeService.fromCorreosToMerge(ejecutarMerge);

            // 2. Ejecutar la generación de cartas (Proceso pesado)
            //procesarCartaCobranzaService.processArchivoCobranza(resultadoMerge);

            log.info("Merge y generación de cartas finalizado con éxito.");
        } catch (Exception e) {
            log.error("Error crítico en Merge asíncrono: {}", e.getMessage());
        }
    }
}