package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
public class EListarController {


    private final AFileStorageComponent storageService;
    // --- LISTAR: Soporta todas tus variantes de URL ---
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
        System.out.println("Buscando en: " + path.toAbsolutePath());

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

    /***
    @GetMapping({
            "/listar/{t}/{s}",       // Para listar/upload/cobranza
            "/listar/{t}/{s}/{u}",    // Para listar/upload/cobranza/tesoreria
            "/listar/{t}/{s}/{u}/{p}" // Para listar/upload/cobranza/tesoreria/enero
    })
    public ResponseEntity<?> listar(
            @PathVariable String t,
            @PathVariable String s,
            @PathVariable(required = false) String u, // Ahora 'u' puede ser nulo
            @PathVariable(required = false) String p,
            @RequestParam(required = false) String nombre,
            @RequestParam(defaultValue = "0") int page, // Página actual (empieza en 0)
            @RequestParam(defaultValue = "10") int size // Registros por página
    ) throws IOException {

        Path path = storageService.resolvePath(t, s, u, p);

        if (!Files.exists(path)) {
            return ResponseEntity.ok(Map.of("total", 0, "carpetas", List.of()));
        }

        // Llamamos al nuevo método paginado
        return ResponseEntity.ok(storageService.listarPaginado(path, nombre, page, size));
    }

    @GetMapping({
            "/listar/{tipo}/{seccion}/{unidad}",
            "/listar/{tipo}/{seccion}/{unidad}/{proceso}"
    })
    public ResponseEntity<?> listar(
            @PathVariable String tipo,      // cobranza
            @PathVariable String seccion,   // REPORTES
            @PathVariable String unidad,    // tesoreria
            @PathVariable(required = false) String proceso, // CD-FTX_2026...
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws IOException {

        // Resolvemos el path físico basado en esta estructura
        Path path = storageService.resolvePath(tipo, seccion, unidad, proceso);

        if (!Files.exists(path)) {
            return ResponseEntity.ok(Map.of("total", 0, "carpetas", List.of(), "archivos", List.of()));
        }

        return ResponseEntity.ok(storageService.listarPaginado(path, null, page, size));
    }***/

   /*** // --- UPLOAD: URL http://localhost:9999/notificacion/upload/1juzgado ---
    @PostMapping("/{tipo}/upload/{unidad}")
    public ResponseEntity<?> upload(
            @PathVariable String tipo,
            @PathVariable String unidad,
            @RequestParam(value = "user", required = false, defaultValue = "") String user,
            @RequestParam(value = "header", required = false, defaultValue = "") String header,
            @RequestParam("archivo") MultipartFile file) throws IOException {

        return storageService.procesarSubida("upload", tipo, unidad, user, header, storageService.validarArchivo(file));
    }***/



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
