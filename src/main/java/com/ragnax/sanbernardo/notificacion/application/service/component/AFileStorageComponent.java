package com.ragnax.sanbernardo.notificacion.application.service.component;

import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarUpload;
import com.ragnax.sanbernardo.notificacion.application.service.model.exceptions.ImsbException;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class AFileStorageComponent {

    @Autowired
    ApiProperties apiProperties;

    private final boolean isLinux = System.getProperty("os.name").toLowerCase().contains("linux");

    private String BASE_SFTP;
    private Path BASE_DIR;
    private Path UPLOAD_DIR;
    private Path NORMALIZE_DIR;

    // 🔥 Alias amigables → carpeta real
    private final Map<String, String> aliasTipos = Map.of(
            "upload", "1_upload",
            "backup", "2_backup",
            "normalizado", "3_normalizado",
            "merge", "4_merge",
            "procesado", "5_procesado",
            "cobranza", "6_cobranza",
            "notificacion", "7_notificacion"
    );

    private final List<String> tiposPermitidos = List.of(
            "1_upload",
            "2_backup",
            "3_normalizado",
            "4_merge",
            "5_procesado",
            "6_cobranza",
            "7_notificacion"
    );

    private final List<String> unidadesPermitidas = List.of(
            "1juzgado",
            "2juzgado",
            "tesoreria"
    );

    @PostConstruct
    public void init() throws IOException {

        BASE_SFTP = isLinux
                ? "/var/www/sb_ope_001a/public_sftp"
                : "/Users/mac/Workspaces/WorkspaceSanBernardo/com-ragnax-sanbernardo-notificacion/src/main/resources/public_sftp";

        BASE_DIR = Paths.get(BASE_SFTP);
        UPLOAD_DIR = BASE_DIR.resolve("1_upload");
        NORMALIZE_DIR = BASE_DIR.resolve("4_normalizado");

        // Crear carpeta base
        Files.createDirectories(BASE_DIR);

        // Crear estructura base
        for (String tipo : tiposPermitidos) {
            Files.createDirectories(BASE_DIR.resolve(tipo));
        }
    }

    // =========================
    // 🔥 RESOLVER ALIAS
    // =========================
    public Path resolveDynamicPath(String subPath) {
        if (subPath == null || subPath.isEmpty()) {
            return getBaseDir();
        }

        // 1. Limpiar barras
        String cleanPath = subPath.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");

        // 2. Dividir la ruta en partes
        String[] parts = cleanPath.split("/");

        // 3. 🔥 TRADUCCIÓN POSICIONAL: Solo el primer nivel usa el alias
        // Ejemplo: "upload/cobranza" -> parts[0] es "upload", se traduce a "1_upload"
        // Los demás segmentos (parts[1], parts[2]...) se quedan como vienen.
        if (parts.length > 0) {
            parts[0] = resolveTipo(parts[0].toLowerCase().trim());
        }

        // 4. Reconstruir la ruta física
        String resolvedSubPath = String.join("/", parts);

        // 5. Resolver contra el directorio base
        Path path = getBaseDir().resolve(resolvedSubPath).normalize();

        // --- LOGS DE DEPURACIÓN ACTUALIZADOS ---
        System.out.println("--- Debug Path Resolution (Posicional) ---");
        System.out.println("Entrada Original : " + subPath);
        System.out.println("Ruta Traducida   : " + resolvedSubPath);
        System.out.println("Path Absoluto    : " + path.toAbsolutePath());
        System.out.println("------------------------------------------");

        // 6. Seguridad
        if (!path.startsWith(getBaseDir())) {
            throw new SecurityException("Intento de acceso fuera del límite permitido: " + path);
        }

        return path;
    }

    public String resolveTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) return tipo;
        return aliasTipos.getOrDefault(tipo.toLowerCase().trim(), tipo);
    }

    // =========================
    // 🔥 CONSTRUCCIÓN DINÁMICA DE PATH
    // =========================


    // =========================
    // ✅ VALIDACIONES
    // =========================

    public void validarTipo(String tipo) {
        String real = resolveTipo(tipo);
        if (!tiposPermitidos.contains(real)) {
            throw new RuntimeException("Tipo no permitido: " + tipo);
        }
    }

    public void validarUnidad(String unidad) {
        if (!unidadesPermitidas.contains(unidad)) {
            throw new RuntimeException("Unidad no permitida: " + unidad);
        }
    }

    public MultipartFile validarArchivo(MultipartFile file) {
        String nombre = file.getOriginalFilename();
        if (nombre == null || !(nombre.toLowerCase().endsWith(".xlsx") || nombre.toLowerCase().endsWith(".csv") )) {
            throw new RuntimeException("Formato de archivo no permitido. Solo se acepta .xlsx o csv");
        }
        return file;
    }

    public Map<String, Object> listarPaginado(Path path, String filtro, int page, int size) throws IOException {
        List<Path> todosLosFiltrados;

        // 1. Filtrado y Ordenamiento (Operación liviana sobre nombres y metadatos básicos)
        try (Stream<Path> stream = Files.list(path)) {
            todosLosFiltrados = stream
                    .filter(p -> {
                        if (filtro == null || filtro.isEmpty()) return true;
                        return p.getFileName().toString().toLowerCase().contains(filtro.toLowerCase());
                    })
                    .sorted((p1, p2) -> {
                        try {
                            // El ordenamiento por fecha es necesario, pero costoso en sftp/red
                            return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e) { return 0; }
                    })
                    .collect(Collectors.toList());
        }

        int totalElementos = todosLosFiltrados.size();
        int desde = Math.min(page * size, totalElementos);
        int hasta = Math.min(desde + size, totalElementos);

        // 2. PAGINACIÓN REAL: Solo procesamos el detalle de los archivos que se verán (ej. solo 10)
        List<Path> paginaPaths = todosLosFiltrados.subList(desde, hasta);

        List<Map<String, Object>> detalle = paginaPaths.stream().map(p -> {
            Map<String, Object> info = new HashMap<>();
            String nombre = p.getFileName().toString();
            boolean esDirectorio = Files.isDirectory(p);

            info.put("nombre", nombre);
            info.put("esDirectorio", esDirectorio);

            try {
                // Usamos readAttributes solo para los 10 archivos de la página actual
                BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                info.put("fechaCreacion", attr.creationTime().toInstant().toString());
                info.put("tamano", attr.size());
            } catch (IOException e) {
                info.put("fechaCreacion", "---");
            }

            // Lógica de metadatos (Solo para archivos .xlsx en la página actual)
            if (!esDirectorio && nombre.endsWith(".xlsx")) {
                // Tu método estático para leer el JSON compañero
                Map<String, String> meta = CrearJsonExcel.extraerMetadataJson(p);
                info.put("observacion", meta.getOrDefault("observacion", ""));
                info.put("usuario", meta.getOrDefault("usuario", ""));
            } else {
                info.put("observacion", "");
                info.put("usuario", "");
            }

            return info;
        }).collect(Collectors.toList());

        // 3. Preparar respuesta consistente para Angular
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("totalItems", totalElementos);
        respuesta.put("items", detalle); // Aquí van mezclados carpetas y archivos
        respuesta.put("paginaActual", page);
        respuesta.put("totalPaginas", (int) Math.ceil((double) totalElementos / size));
        respuesta.put("fechaAhora", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return respuesta;
    }

    // --- LÓGICA DE SUBIDA CENTRALIZADA ---
    public EjecutarUpload procesarSubida(String carpetaRaiz,
                                         String tipo,
                                         String unidad,
                                         String usuario,
                                         String observacion,
                                         MultipartFile file) throws IOException {
        // ✅ solo unidad
        validarUnidad(unidad);

        if (file.isEmpty()) {
            throw new ImsbException("Archivo vacío", null);
            //return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
        }

        LocalDateTime now = LocalDateTime.now();

        String fechaCreacion = now
                .format(DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss"));

        String fechaNombreCarpeta = now
                .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

        String strBaseNombre = observacion.concat("_").concat(fechaNombreCarpeta);

        String safeName = file.getOriginalFilename().replaceAll("\\s+", "_");

        // 🔥 SOLO carpetaRaiz se transforma
        Path base = getBaseDir()
                .resolve(resolveTipo(carpetaRaiz));
        Path targetDir = null;
        if(carpetaRaiz.equalsIgnoreCase("upload") ) {
            // 🔥 tipo NO se transforma
            targetDir = base
                    .resolve(tipo)        // ❌ sin resolveTipo
                    .resolve(unidad)
                    .resolve(strBaseNombre);
        }

        if(carpetaRaiz.equalsIgnoreCase("merge") ){
            targetDir = base
                    .resolve(tipo)        // ❌ sin resolveTipo
                    .resolve(unidad)
                    .resolve(observacion);
        }


        Files.createDirectories(targetDir);

        Path destino = targetDir.resolve(safeName);

        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        if(carpetaRaiz.equalsIgnoreCase("upload") ){
            CrearJsonExcel.crearJson1Upload(targetDir, usuario, fechaCreacion, safeName, observacion);
        }

        EjecutarUpload ejecutarNotificacion = new EjecutarUpload();
        ejecutarNotificacion.setFechaCreacionUpload(fechaCreacion);
        ejecutarNotificacion.setPathCarpetaUpload("1_upload");
        ejecutarNotificacion.setUsuarioUpload(usuario);
        ejecutarNotificacion.setTipo(tipo);
        ejecutarNotificacion.setUnidad(unidad);
        ejecutarNotificacion.setValor("");
        ejecutarNotificacion.setObservacion(observacion);
        ejecutarNotificacion.setBaseNombre(strBaseNombre);
        ejecutarNotificacion.setNombreArchivoUpload(safeName);
        ejecutarNotificacion.setPathArchivoUpload(destino.toString());

        return ejecutarNotificacion;

    }

    public String guardarCsvNormalizadoCorreos(MultipartFile file, String tipo, String unidad, String process) throws IOException {
        // 1. Definir la ruta base de trabajo (usando tus propiedades de ApiProperties si es necesario)
        // Ejemplo: /public_sftp/4_merge/cobranza/tesoreria/temp_csv/
        Path rutaDirectorio = Paths.get(
                apiProperties.getArchivoCreacionCarpeta(),
                "4_merge",
                tipo.toLowerCase(),
                unidad.toLowerCase(),
                process

        );

        // 2. Crear los directorios si no existen
        if (!Files.exists(rutaDirectorio)) {
            Files.createDirectories(rutaDirectorio);

        }

        // 3. Generar un nombre único para evitar colisiones si dos usuarios suben archivos al mismo tiempo
        //String nombreArchivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path rutaDestino = rutaDirectorio.resolve(file.getOriginalFilename());

        // 4. Copiar el archivo físicamente al disco
        // Usamos el InputStream del MultipartFile mientras el hilo principal esté vivo
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, rutaDestino, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Archivo CSV de Correos guardado físicamente para proceso asíncrono en: {}", rutaDestino);

        // 5. Retornar la ruta absoluta para que el hilo @Async sepa dónde leerlo
        return rutaDestino.toString();
    }


    // --- HELPER DE RUTAS ---
    // =========================
    // 📂 GETTERS
    // =========================

    public Path getBaseDir() {
        return BASE_DIR;
    }
}