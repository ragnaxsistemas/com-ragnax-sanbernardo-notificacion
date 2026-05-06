package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.ECarpetaHabilitadaService;
import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.UnidadDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("") // Cambiado para evitar colisiones con "/"
@CrossOrigin(origins = "*")
@Slf4j
public class EListarController {

    private final AFileStorageComponent storageService;

    private final ECarpetaHabilitadaService carpetaHabilitadaService;

    @GetMapping("/carpetas-habilitadas/unidad/{codEmpresa}")
    public ResponseEntity<List<UnidadDTO>> buscarPorUnidad(@PathVariable String codEmpresa) {
        return ResponseEntity.ok(carpetaHabilitadaService.obtenerUnidadesFront(codEmpresa));
    }

    @GetMapping("/habilitar-imprenta/**")
    public ResponseEntity<?> habilitarImprenta(
            HttpServletRequest request,
            @RequestParam(required = false) String nombre
    ) throws IOException {

        // 1. Extraer la ruta dinámica capturada por el comodín **
        String pathPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String subPath = new AntPathMatcher().extractPathWithinPattern(pathPattern, fullPath);

        // 2. Resolver la ruta física usando el StorageService
        // subPath llegará como: "cobranza/tesoreria/CD-FTX_2026..." o "upload/cobranza/tesoreria"
        Path path = storageService.resolveDynamicPath(subPath);

        // LOG de diagnóstico para verificar qué está buscando el servidor
        log.info("Habilitando carpeta para imprenta en: {}" , path.toAbsolutePath());

        if (!Files.exists(path)) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "La ruta no existe en el servidor"
            ));
        }

        // 3. Retornar la lista paginada (carpetas y archivos)
        return ResponseEntity.ok(storageService.habilitarImprenta(path));
    }

    // --- LISTAR: Soporta todas tus variantes de URL --
    @GetMapping("/listar/**")
    public ResponseEntity<?> listarUniversal(
            HttpServletRequest request,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {

        // 1. Extraer la ruta dinámica capturada por el comodín **
        String pathPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String subPath = new AntPathMatcher().extractPathWithinPattern(pathPattern, fullPath);

        // 2. Resolver la ruta física usando el StorageService
        // subPath llegará como: "cobranza/tesoreria/CD-FTX_2026..." o "upload/cobranza/tesoreria"
        Path path = storageService.resolveDynamicPath(subPath);

        // LOG de diagnóstico para verificar qué está buscando el servidor
        log.info("Buscando en: {}" , path.toAbsolutePath());

        if (!Files.exists(path)) {
            return ResponseEntity.ok(Map.of(
                    "totalItems", 0,
                    "items", List.of(),
                    "mensaje", "La ruta no existe en el servidor"
            ));
        }

        // 3. Retornar la lista paginada (carpetas y archivos)
        return ResponseEntity.ok(storageService.listarPaginado(path, nombre, page, size));
    }

    // --- DOWNLOAD (Corregido para soportar puntos y extensiones) ---
    // El truco es :.+ para que capture el nombre completo del archivo con su extensión
   @CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"})
   @GetMapping("/download/**")
   public ResponseEntity<Resource> downloadUniversal(HttpServletRequest request) throws IOException {
       // 1. Extraer la ruta completa después de /download/
       String pathPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
       String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
       String subPath = new AntPathMatcher().extractPathWithinPattern(pathPattern, fullPath);

       // 2. Usar el resolveDynamicPath que ya corregimos (el que traduce el alias del primer segmento)
       Path filePath = storageService.resolveDynamicPath(subPath);

       // 3. Verificaciones de seguridad y existencia
       if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
           return ResponseEntity.notFound().build();
       }

       Resource resource = new UrlResource(filePath.toUri());
       String contentType = Files.probeContentType(filePath);
       if (contentType == null) contentType = "application/octet-stream";

       // 4. Preparar el nombre del archivo para la descarga
       String fileName = filePath.getFileName().toString();
       String headerValue = String.format("attachment; filename=\"%s\"", fileName);

       return ResponseEntity.ok()
               .contentType(MediaType.parseMediaType(contentType))
               .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
               .body(resource);
   }

}
