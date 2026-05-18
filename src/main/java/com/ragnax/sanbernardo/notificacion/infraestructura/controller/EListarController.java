package com.ragnax.sanbernardo.notificacion.infraestructura.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragnax.sanbernardo.notificacion.application.service.ECarpetaHabilitadaService;
import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarConsolidado;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.controller.dto.DescargasImprenta;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("") // Cambiado para evitar colisiones con "/"
//@CrossOrigin(origins = "*")
@Slf4j
public class EListarController {

    private final AFileStorageComponent storageService;

    private final ECarpetaHabilitadaService carpetaHabilitadaService;
    private final ApiProperties apiProperties;

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
   //@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"})
    @GetMapping("/download/**")
    public ResponseEntity<?> downloadUniversal(HttpServletRequest request) throws IOException { // 🚩 Cambiado a ResponseEntity<?>
        log.info("********** downloadUniversal **********");

        String pathPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String subPath = new AntPathMatcher().extractPathWithinPattern(pathPattern, fullPath);

        Path filePath = storageService.resolveDynamicPath(subPath);

        if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream"; // Pasar a Utilidad

        String fileName = filePath.getFileName().toString();
        String headerValue = String.format("attachment; filename=\"%s\"", fileName); // Pasar a Utilidad

        // 🚩 DETECTAR SI LA PETICIÓN PASÓ POR NGINX (Buscando cabeceras de proxy comunes)
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 🔥 PRODUCCIÓN (AWS con Nginx)
            String rootPathStr = apiProperties.getArchivoCreacionCarpeta();

            // Extraemos la parte relativa
            String relativePath = filePath.toString().replace(rootPathStr, "");
            // Pasar a Utilidad
            // 🚩 IMPORTANTE: Construimos la URI interna incluyendo el prefijo context-path que configuramos en Nginx
            String nginxInternalUrl = "/imsbcartas/internal-files/" + relativePath.replace("\\", "/").replaceAll("^/+", "");

            log.info("[PROD] Redireccionando a Nginx X-Accel: {}", nginxInternalUrl);
            // Pasar a Utilidad
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .header("X-Accel-Redirect", nginxInternalUrl) // Le pasa el control a Nginx de forma exacta
                    .build();
        } else {
            // 💻 ESTAMOS EN DESARROLLO (MacBook Local) -> Transmitir los bytes tradicionales
            log.info("[LOCAL] Transmitiendo bytes directamente desde Spring Boot");
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource); // Retorna los bytes reales
        }
    }

    // --- DOWNLOAD (Corregido para soportar puntos y extensiones) ---
    // El truco es :.+ para que capture el nombre completo del archivo con su extensión
    //@CrossOrigin(origins = "*", exposedHeaders = {"Content-Disposition"})
    @GetMapping("/download-imprenta/**")
    public ResponseEntity<Void> downloadImprenta( // 🚩 Cambiado a ResponseEntity<Void>
                                                  HttpServletRequest request,
                                                  @RequestHeader(value = "X-Download-Metadata", required = false) String metadataBase64
    ) throws IOException {

        log.info("********** downloadImprenta (Optimizado con Nginx X-Accel) **********");

        // 1. Extraer la ruta completa después de /download-imprenta/
        String pathPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String subPath = new AntPathMatcher().extractPathWithinPattern(pathPattern, fullPath);

        // 2. Usar el resolveDynamicPath (Traduce el alias del primer segmento)
        Path filePath = storageService.resolveDynamicPath(subPath);

        // 3. Verificaciones de seguridad y existencia
        if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            return ResponseEntity.notFound().build();
        }

        // 🚩 [TODA TU LÓGICA DE AUDITORÍA SE MANTIENE INTACTA]
        if (metadataBase64 != null) {
            byte[] decodedBytes = Base64.getDecoder().decode(metadataBase64);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> metadata = mapper.readValue(json, Map.class);

            log.info("Descarga autorizada para Imprenta: " + metadata.get("nombre"));

            EjecutarConsolidado ec = CrearJsonExcel.getEjecutarConsolidadoFromJson(filePath.toString().replace(".pdf", ".json"));
            if (ec.getDescargasImprenta() == null) ec.setDescargasImprenta(new ArrayList<>());

            ec.getDescargasImprenta().add(new DescargasImprenta(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
                    metadata.get("nombre").toString()
            ));

            // Guarda el JSON trackeando que la imprenta ya descargó el archivo
            CrearJsonExcel.crearJson5Consolidado(ec);
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        String fileName = filePath.getFileName().toString();
        String headerValue = String.format("attachment; filename=\"%s\"", fileName);

        // 🚩 4. MAQUILAR LA RUTA VIRTUAL PARA NGINX
        // Recuerda que esta ruta base debe coincidir exactamente con el 'alias' en /etc/nginx/conf.d/imsb-backend.conf
        String rootPathStr = apiProperties.getArchivoCreacionCarpeta();
        String relativePath = filePath.toString().replace(rootPathStr, "");

        // Construimos la URI de redirección interna que interceptará Nginx
        String nginxInternalUrl = "/internal-files/" + relativePath.replace("\\", "/");

        log.info("Imprenta autorizada. Redireccionando internamente a Nginx: {}", nginxInternalUrl);
        log.info("********************");

        // 🚩 Retornamos la respuesta con body vacío. Java termina aquí.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                .header("X-Accel-Redirect", nginxInternalUrl) // La cabecera mágica
                .build();
    }

    @GetMapping("/download/manual")
    public ResponseEntity<?> downloadManualUsuario(HttpServletRequest request) throws IOException {
        log.info("********** downloadManualUsuario **********");

        String rootPathStr = apiProperties.getArchivoCreacionCarpeta(); // /var/www/sb_ope_001a/public_sftp/ o la de tu Mac
        Path filePath = Paths.get(rootPathStr, "documentacion", "manual_usuario.pdf");

        log.info("Buscando manual en: {}", filePath.toString());

        if (!Files.exists(filePath) || !Files.isReadable(filePath) || Files.isDirectory(filePath)) {
            log.error("El archivo manual_usuario.pdf no existe o no se puede leer.");
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/pdf";

        String headerValue = "attachment; filename=\"manual_usuario.pdf\"";

        // 🚩 DETECCIÓN INFALIBLE POR RUTA FÍSICA
        if (rootPathStr.startsWith("/var/www")) {
            // 🔥 PRODUCCIÓN (AWS con Nginx)
            String relativePath = filePath.toString().replace(rootPathStr, "");
            String nginxInternalUrl = "/imsbcartas/internal-files/" + relativePath.replace("\\", "/").replaceAll("^/+", "");

            log.info("[PROD-MANUAL] Forzando engranaje Nginx X-Accel: {}", nginxInternalUrl);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .header("X-Accel-Redirect", nginxInternalUrl)
                    .build();
        } else {
            // 💻 DESARROLLO (MacBook Local)
            log.info("[LOCAL-MANUAL] Transmitiendo bytes directamente desde Spring Boot");
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);
        }
    }

}
