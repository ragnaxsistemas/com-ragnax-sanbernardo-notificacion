package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.AProcesarNormalizacionService;
import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload") // Cambiado para evitar colisiones con "/"
@CrossOrigin(origins = "*")
public class AFileController {


    private final AFileStorageComponent storageService;

    private final AProcesarNormalizacionService procesarNormalizacion;

    @PostMapping("/{tipo}/{unidad}")
    public ResponseEntity<?> upload(
            @PathVariable String tipo,
            @PathVariable String unidad,
            @RequestParam(value = "user", required = false, defaultValue = "") String user,
            @RequestParam(value = "observacion", required = false, defaultValue = "") String observacion,
            @RequestParam("archivo") MultipartFile file) throws IOException {

        return procesarNormalizacion.procesarSubida("upload", tipo, unidad, user, observacion, storageService.validarArchivo(file));
    }

    // --- LISTAR: Soporta todas tus variantes de URL ---
    /***@GetMapping({
            "/listar/{t}",
            "/listar/{t}/{s}",
            "/listar/{t}/{s}/{u}",
            "/listar/{t}/{s}/{u}/{p}"
    })
    public ResponseEntity<?> listar(
            @PathVariable String t,
            @PathVariable(required = false) String s,
            @PathVariable(required = false) String u,
            @PathVariable(required = false) String p) throws IOException {

        // 🔥 Validaciones
        storageService.validarTipo(t);

        if (u != null) {
            storageService.validarUnidad(u);
        }

        // 🔥 Construcción dinámica del path
        Path path = storageService.resolvePath(t, s, u, p);

        if (!Files.exists(path)) {
            return ResponseEntity.ok(Map.of(
                    "carpetas", List.of(),
                    "archivos", List.of()
            ));
        }

        Map<String, Object> resultado = storageService.listarConDetalle(path);
        return ResponseEntity.ok(resultado);
    }***/

    // --- UPLOAD: URL http://localhost:9999/notificacion/upload/1juzgado ---





    /***
    // --- DOWNLOAD (Corregido para soportar puntos y extensiones) ---
    // El truco es :.+ para que capture el nombre completo del archivo con su extensión
    @GetMapping("/download/{t}/{s}/{u}/{p}/{f:.+}")
    public ResponseEntity<Resource> download(
            @PathVariable String t,
            @PathVariable String s,
            @PathVariable String u,
            @PathVariable String p,
            @PathVariable String f) throws IOException {

        // 1. Construcción de la ruta base (solo transforma la raíz según tu lógica)
        Path base = storageService.getBaseDir()
                .resolve(storageService.resolveTipo(t));

        // 2. Construcción de la ruta completa al archivo específico
        Path filePath = base
                .resolve(s)
                .resolve(u)
                .resolve(p)
                .resolve(f); // Agregamos el nombre del archivo al final del path

        // 3. Verificación de existencia
        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        // 4. Detección dinámica del Content-Type (PDF o XML)
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            // Fallback genérico si no se detecta la extensión
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f + "\"")
                .body(resource);
    }***/




}
