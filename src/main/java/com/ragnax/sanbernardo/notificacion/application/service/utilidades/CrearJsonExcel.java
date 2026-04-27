package com.ragnax.sanbernardo.notificacion.application.service.utilidades;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarUpload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CrearJsonExcel {


    public static Map<String, String> extraerMetadataJson(Path excelPath) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("observacion", "Sin observación");
        metadata.put("usuario", "Sistema");

        // Cambiamos la extensión .xlsx por .json
        String nombreJson = excelPath.getFileName().toString().replace(".xlsx", ".json");
        Path jsonPath = excelPath.getParent().resolve(nombreJson);

        if (Files.exists(jsonPath)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonPath.toFile());

                metadata.put("observacion", root.path("observacion").asText("Sin observación"));
                metadata.put("usuario", root.path("usuario").asText("Sistema"));
            } catch (Exception e) {
                System.err.println("⚠️ Error leyendo JSON: " + jsonPath);
            }
        }
        return metadata;
    }

    public static void crearJson1Upload(
            Path destino,
            String usuario,
            String fechaCreacion,
            String nombreArchivo,
            String observacion ) {
        // 1a. Crear el objeto JSON
        // 1b. Datos a guardar
        Map<String, Object> data = new HashMap<>();
        data.put("usuario", usuario);
        data.put("fechaCreacion", fechaCreacion);
        data.put("nombreArchivo", nombreArchivo);
        data.put("observacion", observacion);

        String nombreJson = nombreArchivo.replace(".xlsx", "").concat(".json");

        try {

            Path path = destino.resolve(nombreJson);
            // Usamos Jackson para convertir el Map a String JSON
            ObjectMapper mapper = new ObjectMapper();
            String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);

            // Escribimos el archivo en la misma carpeta
            Files.write(path, jsonContent.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void crearJson3Upload(EjecutarUpload ejecutarUpload) {
        ejecutarUpload.setFile(null);
        ejecutarUpload.setContenidoCsv(null);
        // 1. Validar que el objeto y el nombre no sean nulos
        if (ejecutarUpload == null || ejecutarUpload.getPathArchivoNormalizado() == null) {
            System.err.println("EjecutarUpload o el nombre del archivo es nulo");
            return;
        }

        // 2. Determinar el nombre del archivo JSON (reemplazando .xlsx por .json)
        String nombreJson = ejecutarUpload.getPathArchivoNormalizado().replace(".xlsx", "").concat(".json");

        try {
            // 3. Obtener la ruta del directorio donde está el archivo original
            // Asumiendo que ejecutarUpload.getRutaArchivo() contiene el path completo del Excel
            Path rutaArchivoOriginal = Paths.get(ejecutarUpload.getPathArchivoNormalizado());
            Path directorioDestino = rutaArchivoOriginal.getParent(); // Esto obtiene la carpeta contenedora

            if (directorioDestino == null) {
                throw new IOException("No se pudo determinar el directorio de destino desde la ruta: " + ejecutarUpload.getPathArchivoNormalizado());
            }

            Path pathFinalJson = directorioDestino.resolve(nombreJson);

            // 4. Configurar ObjectMapper y serializar
            ObjectMapper mapper = new ObjectMapper();

            // Escribimos el objeto directamente como JSON con formato "Pretty"
            String jsonContent = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ejecutarUpload);

            // 5. Escribir el archivo físicamente
            Files.write(pathFinalJson, jsonContent.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            System.err.println("Error al crear el JSON de Upload para: " + ejecutarUpload.getPathArchivoNormalizado());
            e.printStackTrace();
        }
    }

    public static void crearJson4Merge(EjecutarMerge ejecutarMerge) {
        ejecutarMerge.setFile(null);
        ejecutarMerge.setListaExcelCobranzaNormalizado(null);
        ejecutarMerge.setFileCorreosCsv(null);
        ejecutarMerge.setListaPdfs(null);
        // 1. Validar que el objeto y el nombre no sean nulos
        if (ejecutarMerge == null || ejecutarMerge.getPathReporte()== null) {
            System.err.println("EjecutarMerge o el nombre del archivo es nulo");
            return;
        }

        // 2. Determinar el nombre del archivo JSON (reemplazando .xlsx por .json)
        String nombreJson = ejecutarMerge.getPathReporte().replace(".pdf", "").concat(".json");

        try {
            // 3. Obtener la ruta del directorio donde está el archivo original
            // Asumiendo que ejecutarUpload.getRutaArchivo() contiene el path completo del Excel
            Path rutaArchivoOriginal = Paths.get(ejecutarMerge.getPathArchivoMerge());
            Path directorioDestino = rutaArchivoOriginal.getParent(); // Esto obtiene la carpeta contenedora

            if (directorioDestino == null) {
                throw new IOException("No se pudo determinar el directorio de destino desde la ruta: " + ejecutarMerge.getPathArchivoMerge());
            }

            Path pathFinalJson = directorioDestino.resolve(nombreJson);

            // 4. Configurar ObjectMapper y serializar
            ObjectMapper mapper = new ObjectMapper();

            // Escribimos el objeto directamente como JSON con formato "Pretty"
            String jsonContent = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ejecutarMerge);

            // 5. Escribir el archivo físicamente
            Files.write(pathFinalJson, jsonContent.getBytes(StandardCharsets.UTF_8));

            System.out.println("JSON de Upload creado exitosamente en: " + pathFinalJson.toString());

        } catch (Exception e) {
            System.err.println("Error al crear el JSON de Upload para: " + ejecutarMerge.getPathArchivoNormalizado());
            e.printStackTrace();
        }
    }

    public static EjecutarUpload getEjecutarUploadFromJson(String pathXlsx) throws IOException {
        // 1. Construir la ruta del JSON basándonos en el path del Excel
        // Reemplazamos la extensión para apuntar al archivo .json
        Path jsonPath = Paths.get(pathXlsx.replace(".xlsx", ".json"));

        // 2. Verificar si el archivo físico existe
        if (Files.exists(jsonPath)) {
            try {
                ObjectMapper mapper = new ObjectMapper();

                // 3. Leer el archivo y convertirlo directamente al objeto EjecutarUpload
                EjecutarUpload objetoRecuperado = mapper.readValue(jsonPath.toFile(), EjecutarUpload.class);

                System.out.println("JSON cargado exitosamente desde: " + jsonPath.toString());
                return objetoRecuperado;

            } catch (IOException e) {
                System.err.println("Error al deserializar el archivo JSON en: " + jsonPath);
                e.printStackTrace();
                throw e;
            }
        } else {
            // Opción A: Retornar null o Opción B: Lanzar una excepción si es crítico
            System.err.println("No se encontró el archivo JSON asociado a: " + pathXlsx);
            return null;
        }
    }
}

