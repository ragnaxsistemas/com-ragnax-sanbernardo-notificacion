package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.MailComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.ObtenerExcel;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BUploadToDesnormalizadoService {

    @Autowired
    private ApiProperties apiProperties;

    @Autowired
    private MailComponent mailComponent;

    private static final String COBRANZA = "COBRANZA";

    private static final String NOTIFICACION = "NOTIFICACION";

    // Inventar un correlativo que se obtenga del valor de las carpetas
    public void normalizarArchivo(EjecutarUpload ejecutarUpload)  {

        log.info("processRequest {} usuario {} unidad {} archivo upload {}", apiProperties.getProfile(),
                ejecutarUpload.getUsuarioUpload(),
                ejecutarUpload.getUnidad(),
                ejecutarUpload.getNombreArchivoUpload());

        if(ejecutarUpload.getTipo().equalsIgnoreCase(COBRANZA)){
            ejecutarUpload =processRequestCobranza( ejecutarUpload);
        }
        if(ejecutarUpload.getTipo().equalsIgnoreCase(NOTIFICACION)){
            processRequestNotificacion( ejecutarUpload);
        }

        log.info("path {}", ejecutarUpload.getPathArchivoUpload());

        CrearJsonExcel.crearJson3Normalizado(ejecutarUpload);
    }

    private EjecutarUpload processRequestCobranza(EjecutarUpload ejecutarUpload)  {
        List<ExcelCobranza> excelCobranzas = new ArrayList<>();
        try{

            Path pathDestinoRespaldo = null;

            /**Viene el nombre hasta el upload**/
            // 1. Obtenemos la ruta donde storageService ya guardó el archivo
            String dirExcelUpload = ejecutarUpload.getPathArchivoUpload();
            Path pathOrigenUpload = Paths.get(dirExcelUpload);

            // 2. VERIFICACIÓN CRÍTICA
            if (!Files.exists(pathOrigenUpload)) {
                throw new RuntimeException("El archivo físico no se encontró en la ruta: " + dirExcelUpload);
            }

            // 1. Lógica específica de QA/PROD
            try (InputStream inputStream = Files.newInputStream(pathOrigenUpload)) {
                excelCobranzas = ObtenerExcel.obtenerExcelCobranza(
                        inputStream,
                        apiProperties.getArchivoExcelNombreHojaCobranza()
                );
            }

            pathDestinoRespaldo = Paths.get(apiProperties.getArchivoCreacionRespaldo()
                    .concat("/").concat(ejecutarUpload.getTipo())
                    .concat("/").concat(ejecutarUpload.getUnidad())
                    .concat("/").concat(ejecutarUpload.getBaseNombre())
                    .concat("/").concat(ejecutarUpload.getBaseNombre().concat("_").concat(ejecutarUpload.getNombreArchivoUpload())));
            log.info("pathOrigen Upload {} ", dirExcelUpload);

            log.info("pathDestino Respaldo {}", pathDestinoRespaldo);
            ejecutarUpload.setPathArchivoBackup(pathDestinoRespaldo.toString());
            Files.createDirectories(pathDestinoRespaldo.getParent());

            Files.copy( Paths.get(dirExcelUpload), pathDestinoRespaldo, StandardCopyOption.REPLACE_EXISTING);
            /*******Archivo Original IMSB obtenido y Respaldado*****************************/
            /*******************************************************************************/
            log.info(" excelCobranzas Upload{}", excelCobranzas.size() );
            ejecutarUpload.setSizeArchivoUpload(String.valueOf(excelCobranzas.size()));

            List<ExcelCobranzaToNormalize> listaProcesadaNormalizada = procesarListaExcelCobranza(excelCobranzas);

            log.info(" lista procesada normalizada", listaProcesadaNormalizada.size() );

            //Este es el excel que queda en la carpeta 3... para hacer el luego hacer 4 Merge
            ejecutarUpload = exportarANuevoExcelCobranza(ejecutarUpload.getBaseNombre(), listaProcesadaNormalizada, ejecutarUpload);

            //Este es el Csv que queda en la carpeta 3... para hacer el luego hacer 4 Merge
            ejecutarUpload = generarCSVUnicosCobranza(
                    listaProcesadaNormalizada,
                    ejecutarUpload
                    );

            mailComponent.enviarCorreoResend(
                    "julio.ignacio.cornejo.sb@gmail.com",
                    ejecutarUpload.getObservacion(),
                    ejecutarUpload.getTipo(),
                    ejecutarUpload.getUnidad(),
                    Integer.parseInt(ejecutarUpload.getRegistrosUnicos()),
                    ejecutarUpload.getContenidoCsv(),
                    ejecutarUpload.getNombreArchivoCsvToNormalize());

        }catch(Exception e){
            log.error("Exception error", e);
        }
        return ejecutarUpload;
    }

    private EjecutarUpload processRequestNotificacion(EjecutarUpload ejecutarUpload)  {
        List<ExcelNotificacion> excelNotificacion = new ArrayList<>();
        try{

            Path pathDestinoRespaldo = null;

            /**Viene el nombre hasta el upload**/
            // 1. Obtenemos la ruta donde storageService ya guardó el archivo
            String dirExcelUpload = ejecutarUpload.getPathArchivoUpload();
            Path pathOrigenUpload = Paths.get(dirExcelUpload);

            // 2. VERIFICACIÓN CRÍTICA
            if (!Files.exists(pathOrigenUpload)) {
                throw new RuntimeException("El archivo físico no se encontró en la ruta: " + dirExcelUpload);
            }

            // 1. Lógica específica de QA/PROD
            try (InputStream inputStream = Files.newInputStream(pathOrigenUpload)) {
                excelNotificacion = ObtenerExcel.obtenerExcelNotificacion(
                        inputStream,
                        apiProperties.getArchivoExcelNombreHojaNotificacion()
                );
            }

            pathDestinoRespaldo = Paths.get(apiProperties.getArchivoCreacionRespaldo()
                    .concat("/").concat(ejecutarUpload.getTipo())
                    .concat("/").concat(ejecutarUpload.getUnidad())
                    .concat("/").concat(ejecutarUpload.getBaseNombre())
                    .concat("/").concat(ejecutarUpload.getBaseNombre().concat("_").concat(ejecutarUpload.getNombreArchivoUpload())));
            log.info("pathOrigen Upload {} ", dirExcelUpload);

            log.info("pathDestino Respaldo {}", pathDestinoRespaldo);
            ejecutarUpload.setPathArchivoBackup(pathDestinoRespaldo.toString());
            Files.createDirectories(pathDestinoRespaldo.getParent());

            Files.copy( Paths.get(dirExcelUpload), pathDestinoRespaldo, StandardCopyOption.REPLACE_EXISTING);
            /*******Archivo Original IMSB obtenido y Respaldado*****************************/
            /*******************************************************************************/
            log.info(" excelNotificacion Upload{}", excelNotificacion.size() );
            ejecutarUpload.setSizeArchivoUpload(String.valueOf(excelNotificacion.size()));

            List<ExcelNotificacionToNormalize> listaProcesadaNormalizada = procesarListaExcelNotificacion(excelNotificacion);

            log.info(" lista procesada normalizada", listaProcesadaNormalizada.size() );

            //Este es el excel que queda en la carpeta 3... para hacer el luego hacer 4 Merge
            ejecutarUpload = exportarANuevoExcelNotificacion(ejecutarUpload.getBaseNombre(), listaProcesadaNormalizada, ejecutarUpload);

            //Este es el Csv que queda en la carpeta 3... para hacer el luego hacer 4 Merge
            ejecutarUpload = generarCSVUnicosNotificacion(
                    listaProcesadaNormalizada,
                    ejecutarUpload
            );

            mailComponent.enviarCorreoResend(
                    "julio.ignacio.cornejo.sb@gmail.com",
                    ejecutarUpload.getObservacion(),
                    ejecutarUpload.getTipo(),
                    ejecutarUpload.getUnidad(),
                    Integer.parseInt(ejecutarUpload.getRegistrosUnicos()),
                    ejecutarUpload.getContenidoCsv(),
                    ejecutarUpload.getNombreArchivoCsvToNormalize());

        }catch(Exception e){
            log.error("Exception error", e);
        }
        return ejecutarUpload;
    }

    private List<ExcelCobranzaToNormalize> procesarListaExcelCobranza(List<ExcelCobranza> excelCobranzas) {

        Map<String, Map<String, List<ExcelCobranza>>> mapaLargoRutPatente =
                excelCobranzas.stream()
                        .collect(Collectors.groupingBy(
                                ExcelCobranza::getRut,
                                Collectors.groupingBy(ExcelCobranza::getPlacaPatente)
                        ))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(entry ->
                                entry.getValue().values().stream()
                                        .mapToInt(List::size)
                                        .sum()
                        ))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        Map<String, Map<String, List<ExcelCobranza>>> mapaRutPatenteIndividual = new HashMap<>();
        Map<String, Map<String, List<ExcelCobranza>>> mapaRutPatenteMasiva = new HashMap<>();

        mapaLargoRutPatente.forEach((rut, mapaPatentes) -> {
            mapaPatentes.forEach((patente, listaExcel) -> {
                if (listaExcel.size() == 1) {
                    mapaRutPatenteIndividual
                            .computeIfAbsent(rut, k -> new HashMap<>())
                            .put(patente, listaExcel);

                } else {
                    mapaRutPatenteMasiva
                            .computeIfAbsent(rut, k -> new HashMap<>())
                            .put(patente, listaExcel);
                }
            });
        });


        long total = mapaLargoRutPatente.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(List::size)
                .sum();

        long totalInd = mapaRutPatenteIndividual.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(List::size)
                .sum();

        long totalMas = mapaRutPatenteMasiva.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(List::size)
                .sum();

        log.info("Total registros agrupados: {}", total);
        log.info("Total Ind: {}", totalInd);
        log.info("Total Mas: {}", totalMas);

        AtomicInteger idClienteCounter = new AtomicInteger(1);
        List<ExcelCobranzaToNormalize> listaProcesadaNormalizada = new ArrayList<>();

        int totalRegistros = excelCobranzas.size();
        int digitos = String.valueOf(totalRegistros).length();
        String formatoId = "%0" + Math.max(6, digitos) + "d";

        for (Map.Entry<String, Map<String, List<ExcelCobranza>>> entryRut : mapaLargoRutPatente.entrySet()) {

            Map<String, List<ExcelCobranza>> mapaPatentes = entryRut.getValue();

            for (Map.Entry<String, List<ExcelCobranza>> entryPatente : mapaPatentes.entrySet()) {

                List<ExcelCobranza> lista = entryPatente.getValue();

                int contadorPatente = 0;
                int idActual = 0;
                String stringId = "";

                for (ExcelCobranza reg : lista) {

                    // 👉 cada 7 registros o inicio genera nuevo ID
                    if (contadorPatente % 7 == 0) {
                        idActual = idClienteCounter.getAndIncrement();
                        stringId = String.format(formatoId, idActual);
                    }

                    contadorPatente++;

                    String nombreCompleto = String.format("%s %s %s",
                            reg.getApellidoPaterno(),
                            reg.getApellidoMaterno(),
                            reg.getNombres());

                    String concatenado = String.format("%s, %s, %s, %s",
                            stringId,
                            nombreCompleto,
                            reg.getDireccion(),
                            reg.getComuna()
                    );

                    if(entryRut.getKey().equals("97004000")){
                        log.info("rut {} - patente {} - stringId {} ", entryRut.getKey(), entryPatente.getKey(), stringId);
                    }

                    ExcelCobranzaToNormalize nuevo = new ExcelCobranzaToNormalize(
                            reg.getCert1(),
                            reg.getCert2(),
                            reg.getFechaCarta(),
                            reg.getVence(),
                            reg.getFolio(),
                            reg.getApellidoPaterno(),
                            reg.getApellidoMaterno(),
                            reg.getNombres(),
                            reg.getRut(),
                            reg.getDv(),
                            reg.getDireccion(),
                            reg.getComuna(),
                            reg.getPlacaPatente(),
                            reg.getDg(),
                            reg.getTipoVehiculo(),
                            reg.getRolMop(),
                            reg.getFechaInfraccion(),
                            reg.getHoraInfraccion(),
                            reg.getConvenio1(),
                            reg.getConvenio2(),
                            reg.getCodigoBarra(),
                            reg.getValorMulta(),
                            reg.getLugarMulta(),
                            reg.getFechaCitacion(),
                            reg.getJuzgado(),
                            reg.getPiso(),
                            stringId,
                            concatenado
                    );

                    listaProcesadaNormalizada.add(nuevo);
                }
            }
        }

        return listaProcesadaNormalizada;
    }


    private List<ExcelNotificacionToNormalize> procesarListaExcelNotificacion(List<ExcelNotificacion> excelNotificaciones){
        Map<String, Map<String, List<ExcelNotificacion>>> mapaLargoRutPatente =
                excelNotificaciones.stream()
                        .collect(Collectors.groupingBy(
                                ExcelNotificacion::getRut,
                                Collectors.groupingBy(ExcelNotificacion::getPlacaPatente)
                        ))
                        .entrySet()
                        .stream()
                        .sorted(Comparator.comparingInt(entry ->
                                entry.getValue().values().stream()
                                        .mapToInt(List::size)
                                        .sum()
                        ))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> a,
                                LinkedHashMap::new
                        ));

        AtomicInteger idClienteCounter = new AtomicInteger(1);
        List<ExcelNotificacionToNormalize> listaProcesadaNormalizada = new ArrayList<>();

        int totalRegistros = excelNotificaciones.size();
        int digitos = String.valueOf(totalRegistros).length();
        String formatoId = "%0" + Math.max(6, digitos) + "d";

        for (Map.Entry<String, Map<String, List<ExcelNotificacion>>> entryRut : mapaLargoRutPatente.entrySet()) {

            int idActual = idClienteCounter.getAndIncrement();
            String stringId = String.format(formatoId, idActual);

            Map<String, List<ExcelNotificacion>> mapaPatentes = entryRut.getValue();

            for (Map.Entry<String, List<ExcelNotificacion>> entryPatente : mapaPatentes.entrySet()) {

                int contadorPatente = 1;

                for (ExcelNotificacion reg : entryPatente.getValue()) {
                    
                    if (contadorPatente > 7) {
                        idActual = idClienteCounter.getAndIncrement();
                        stringId = String.format(formatoId, idActual);
                        contadorPatente = 1;
                    }

                    contadorPatente++;

                    // 1. Preparamos el campo concatenado
                    String nombreCompleto = String.format("%s",
                            reg.getNombreCompleto());

                    // 1. Preparamos el campo concatenado usando el stringId formateado
                    String concatenado = String.format("%s, %s, %s, %s",
                            stringId, // Ahora usa "000001"
                            nombreCompleto,
                            reg.getDireccion(),
                            reg.getComuna()
                    );

                    // 2. Usamos el constructor con todos los atributos
                    // Pasamos los campos de ExcelCobranza + los 2 campos nuevos
                    ExcelNotificacionToNormalize nuevo = new ExcelNotificacionToNormalize
                            (reg.getJuzgado(), reg.getNombreCompleto(), reg.getRut(), reg.getDireccion(),
                                    reg.getComuna(), reg.getAnho(), reg.getRol(), reg.getFechaTramite(),
                                    reg.getFechaCitacion(), reg.getPlacaPatente(), reg.getCodigoInterno(),
                                    reg.getFechaVencimiento(), reg.getFechaInfraccion(), reg.getHoraInfraccion(),
                                    reg.getFolio(), stringId,      // clientId
                                    concatenado    // toNormalize
                            );
                    // 3. Agregamos a la lista final
                    listaProcesadaNormalizada.add(nuevo);
                }
            };
        };

        return listaProcesadaNormalizada;
    }

    public EjecutarUpload exportarANuevoExcelCobranza(String proceso,
                                              List<ExcelCobranzaToNormalize> listaToNormalizace,
                                                           EjecutarUpload ejecutarUpload) {

        String dirArchivoExcelToNormalize = null;
        String nombreArchivo = null;

        int totalFilasGeneradas = 0;
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(apiProperties.getArchivoExcelNombreHojaNormalizada());

            // 1. Estilo encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 2. Columnas (corregidas para coincidir con createCell)
            String[] columnas = {
                    "Cert1", "Cert2", "Fecha Carta", "Vence", "Folio",
                    "Ap_Paterno", "Ap_Materno", "Nombres", "Rut", "Dv",
                    "Direccion", "Comuna", "Placa", "Dg", "Tipo_Veh",
                    "RolMop", "Fecha_Infr", "Hora_Infr", "Conv1", "Conv2",
                    "Codigo_Barra", "Valor_Multa", "Lugar_Multa", "Fecha_Citacion",
                    "Juzgado", "Piso", "ClientId", "Concatenado"
            };

            // 3. Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            log.info("Generando Excel con {} registros...", listaToNormalizace.size());

            // 4. Datos
            int rowNum = 1;
            int contador = 0;

            for (ExcelCobranzaToNormalize item : listaToNormalizace) {

                if (item == null) {
                    log.warn("⚠️ Item nulo detectado, se omite");
                    continue;
                }

                Row row = sheet.createRow(rowNum++);
                contador++;

                row.createCell(0).setCellValue(nvl(item.getCert1()));
                row.createCell(1).setCellValue(nvl(item.getCert2()));
                row.createCell(2).setCellValue(nvl(item.getFechaCarta()));
                row.createCell(3).setCellValue(nvl(item.getVence()));
                row.createCell(4).setCellValue(nvl(item.getFolio()));
                row.createCell(5).setCellValue(nvl(item.getApellidoPaterno()));
                row.createCell(6).setCellValue(nvl(item.getApellidoMaterno()));
                row.createCell(7).setCellValue(nvl(item.getNombres()));
                row.createCell(8).setCellValue(nvl(item.getRut()));
                row.createCell(9).setCellValue(nvl(item.getDv()));
                row.createCell(10).setCellValue(nvl(item.getDireccion()));
                row.createCell(11).setCellValue(nvl(item.getComuna()));
                row.createCell(12).setCellValue(nvl(item.getPlacaPatente()));
                row.createCell(13).setCellValue(nvl(item.getDg()));
                row.createCell(14).setCellValue(nvl(item.getTipoVehiculo()));
                row.createCell(15).setCellValue(nvl(item.getRolMop()));
                row.createCell(16).setCellValue(nvl(item.getFechaInfraccion()));
                row.createCell(17).setCellValue(nvl(item.getHoraInfraccion()));
                row.createCell(18).setCellValue(nvl(item.getConvenio1()));
                row.createCell(19).setCellValue(nvl(item.getConvenio2()));
                row.createCell(20).setCellValue(nvl(item.getCodigoBarra()));
                row.createCell(21).setCellValue(nvl(item.getValorMulta()));
                row.createCell(22).setCellValue(nvl(item.getLugarMulta()));
                row.createCell(23).setCellValue(nvl(item.getFechaCitacion()));
                row.createCell(24).setCellValue(nvl(item.getJuzgado()));
                row.createCell(25).setCellValue(nvl(item.getPiso()));
                row.createCell(26).setCellValue(nvl(item.getClientId()));
                row.createCell(27).setCellValue(nvl(item.getToNormalize()));
            }

            // 5. Validación
            totalFilasGeneradas = rowNum - 1;

            log.info("Total iterados: {}", contador);
            log.info("Total filas Excel: {}", totalFilasGeneradas);
            log.info("Total lista original: {}", listaToNormalizace.size());

            if (totalFilasGeneradas != listaToNormalizace.size()) {
                log.error("❌ Diferencia detectada! Lista: {} vs Excel: {}",
                        listaToNormalizace.size(), totalFilasGeneradas);
            } else {
                log.info("✅ Validación OK: no se perdieron registros");
            }

            // 6. AutoSize (opcional)
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 7. Archivo
            nombreArchivo = proceso.concat("_").concat(ejecutarUpload.getUnidad()).concat("_").concat(apiProperties.getArchivoExcelNombreArchivoNormalizadaCobranza());
            // 7. Dreccion
            dirArchivoExcelToNormalize = apiProperties.getArchivoCreacionCarpeta()
                    .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNormalizado())
                    .concat(ejecutarUpload.getTipo()).concat("/")
                    .concat(ejecutarUpload.getUnidad()).concat("/")
                    .concat(proceso).concat("/")
                    .concat(nombreArchivo);

            log.info("Ruta archivo: {}", dirArchivoExcelToNormalize);

            File archivoFinal = new File(dirArchivoExcelToNormalize);

            File directorio = archivoFinal.getParentFile();
            if (!directorio.exists()) {
                boolean creado = directorio.mkdirs();
                if (!creado) {
                    log.warn("No se pudo crear el directorio (puede existir)");
                }
            }

            // 8. Escritura
            try (FileOutputStream fileOut = new FileOutputStream(archivoFinal)) {
                workbook.write(fileOut);
            }

            log.info("Archivo Excel generado con éxito.");

        } catch (IOException e) {
            log.error("❌ Error al generar el archivo: {}", e.getMessage(), e);
        }

        ejecutarUpload.setPathArchivoNormalizado(dirArchivoExcelToNormalize);
        ejecutarUpload.setSizeArchivoNormalizado(String.valueOf(totalFilasGeneradas));

        return ejecutarUpload;
    }

    private String nvl(Object value) {
        return value == null ? "" : value.toString();
    }

    /************************************************************************************************/
    /************************************************************************************************/
    /************************************************************************************************/

    public EjecutarUpload generarCSVUnicosCobranza(List<ExcelCobranzaToNormalize> listaNormalizada,
                                         EjecutarUpload ejecutarUpload) {

        String archivoCsvToNormalize = ejecutarUpload.getPathArchivoNormalizado().replace(".xlsx", apiProperties.getArchivoExcelNombreArchivoNormalizadaCorreos());
        String nombreArchivoCsvToNormalize = "";
        log.info("listaNormalizada. Csv {}", listaNormalizada.size());
        log.info("To Correos. Csv {}", archivoCsvToNormalize);
        File archivoFinal = new File(archivoCsvToNormalize);

        File directorio = archivoFinal.getParentFile();
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        // Usamos un Map para filtrar duplicados basados en una "llave" compuesta
        // Llave: RUT + Direccion + Comuna
        Map<String, ExcelCobranzaToNormalize> registrosUnicos = new LinkedHashMap<>();
        for (ExcelCobranzaToNormalize item : listaNormalizada) {
            if (!registrosUnicos.containsKey(item.getClientId())) {
                registrosUnicos.put(item.getClientId(), item);
            }
        }

        byte[] contenidoCsv = null;

        // 2. Generar CSV en disco y en memoria simultáneamente
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             BufferedWriter memoryWriter = new BufferedWriter(osw);
             BufferedWriter fileWriter = new BufferedWriter(new FileWriter(archivoCsvToNormalize, StandardCharsets.UTF_8))) {

            String encabezado = "clientId;rut;nombre;direccion;comuna;toNormalize";

            // Escribir encabezado en ambos
            fileWriter.write(encabezado); fileWriter.newLine();
            memoryWriter.write(encabezado); memoryWriter.newLine();

            for (ExcelCobranzaToNormalize unico : registrosUnicos.values()) {
                String fila = String.format("%s;%s;%s;%s;%s;%s",
                        unico.getClientId(),
                        unico.getRut(),
                        unico.getNombres() + " " + unico.getApellidoPaterno(),
                        unico.getDireccion(),
                        unico.getComuna(),
                        unico.getToNormalize()
                );
                // Escribir fila en ambos
                fileWriter.write(fila); fileWriter.newLine();
                memoryWriter.write(fila); memoryWriter.newLine();
            }

            // Importante hacer flush para asegurar que todo pase al ByteArrayOutputStream
            memoryWriter.flush();
            contenidoCsv = baos.toByteArray();

            log.info("CSV generado con éxito: {}", archivoCsvToNormalize);

        } catch (IOException e) {
            log.error("Error al generar el CSV: {}", e.getMessage());
        }

        try{
            Path path = Paths.get(archivoCsvToNormalize);
            int count = path.getNameCount();

// Obtenemos el penúltimo (carpeta) y el último (archivo)
            nombreArchivoCsvToNormalize = path.getName(count - 1).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ejecutarUpload.setContenidoCsv(contenidoCsv);
        ejecutarUpload.setRegistrosUnicos(String.valueOf( registrosUnicos.size()));
        ejecutarUpload.setPathArchivoNormalizadoCsv(archivoCsvToNormalize);
        ejecutarUpload.setNombreArchivoCsvToNormalize(nombreArchivoCsvToNormalize);

        return ejecutarUpload;

    }

    public EjecutarUpload exportarANuevoExcelNotificacion(String proceso,
                                                      List<ExcelNotificacionToNormalize> listaToNormalize,
                                                      EjecutarUpload ejecutarUpload) {

        String dirArchivoExcelToNormalize = null;
        String nombreArchivo = null;

        int totalFilasGeneradas = 0;
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(apiProperties.getArchivoExcelNombreHojaNormalizada());

            // 1. Estilo encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // 2. Columnas (corregidas para coincidir con createCell)
            String[] columnas = {
                    "JUZGADO", "NOMBRE", "RUT", "DIRECCION", "COMUNA", "AÑO", "ROL", "FECHA TRAMITE", "FECHA CITACION",
                    "PLACA PATENTE", "COD. INTERNO", "F.VENC BANCO", "FECHA INFRACCION", "HORA", "FOLIO",
                    "clientId", "toNormalize"
            };

            // 3. Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            log.info("Generando Excel con {} registros...", listaToNormalize.size());

            // 4. Datos
            int rowNum = 1;
            int contador = 0;

            for (ExcelNotificacionToNormalize item : listaToNormalize) {

                if (item == null) {
                    log.warn("⚠️ Item nulo detectado, se omite");
                    continue;
                }

                Row row = sheet.createRow(rowNum++);
                contador++;

                row.createCell(0).setCellValue(item.getJuzgado());
                row.createCell(1).setCellValue(item.getNombreCompleto());
                row.createCell(2).setCellValue(item.getRut());
                row.createCell(3).setCellValue(item.getDireccion());
                row.createCell(4).setCellValue(item.getComuna());
                row.createCell(5).setCellValue(item.getAnho());
                row.createCell(6).setCellValue(item.getRol());
                row.createCell(7).setCellValue(item.getFechaTramite());
                row.createCell(8).setCellValue(item.getFechaCitacion());
                row.createCell(9).setCellValue(item.getPlacaPatente());
                row.createCell(10).setCellValue(item.getCodigoInterno());
                row.createCell(11).setCellValue(item.getFechaVencimiento());
                row.createCell(12).setCellValue(item.getFechaInfraccion());
                row.createCell(13).setCellValue(item.getHoraInfraccion());
                row.createCell(14).setCellValue(item.getFolio());
                row.createCell(15).setCellValue(item.getClientId());
                row.createCell(16).setCellValue(item.getToNormalize());

            }

            // 5. Validación
            totalFilasGeneradas = rowNum - 1;

            log.info("Total iterados: {}", contador);
            log.info("Total filas Excel: {}", totalFilasGeneradas);
            log.info("Total lista original: {}", listaToNormalize.size());

            if (totalFilasGeneradas != listaToNormalize.size()) {
                log.error("❌ Diferencia detectada! Lista: {} vs Excel: {}",
                        listaToNormalize.size(), totalFilasGeneradas);
            } else {
                log.info("✅ Validación OK: no se perdieron registros");
            }

            // 6. AutoSize (opcional)
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 7. Archivo
            nombreArchivo = proceso.concat("_").concat(ejecutarUpload.getUnidad()).concat("_").concat(apiProperties.getArchivoExcelNombreArchivoNormalizadaNotificacion());
            // 7. Dreccion
            dirArchivoExcelToNormalize = apiProperties.getArchivoCreacionCarpeta()
                    .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNormalizado())
                    .concat(ejecutarUpload.getTipo()).concat("/")
                    .concat(ejecutarUpload.getUnidad()).concat("/")
                    .concat(proceso).concat("/")
                    .concat(nombreArchivo);

            log.info("Ruta archivo: {}", dirArchivoExcelToNormalize);

            File archivoFinal = new File(dirArchivoExcelToNormalize);

            File directorio = archivoFinal.getParentFile();
            if (!directorio.exists()) {
                boolean creado = directorio.mkdirs();
                if (!creado) {
                    log.warn("No se pudo crear el directorio (puede existir)");
                }
            }

            // 8. Escritura
            try (FileOutputStream fileOut = new FileOutputStream(archivoFinal)) {
                workbook.write(fileOut);
            }

            log.info("Archivo Excel generado con éxito.");

        } catch (IOException e) {
            log.error("❌ Error al generar el archivo: {}", e.getMessage(), e);
        }

        ejecutarUpload.setPathArchivoNormalizado(dirArchivoExcelToNormalize);
        ejecutarUpload.setSizeArchivoNormalizado(String.valueOf(totalFilasGeneradas));

        return ejecutarUpload;
    }

    public EjecutarUpload generarCSVUnicosNotificacion(List<ExcelNotificacionToNormalize> listaNormalizada,
                                                   EjecutarUpload ejecutarUpload) {

        String archivoCsvToNormalize = ejecutarUpload.getPathArchivoNormalizado().replace(".xlsx", apiProperties.getArchivoExcelNombreArchivoNormalizadaCorreos());
        String nombreArchivoCsvToNormalize = "";
        log.info("listaNormalizada. Csv {}", listaNormalizada.size());
        log.info("To Correos. Csv {}", archivoCsvToNormalize);
        File archivoFinal = new File(archivoCsvToNormalize);

        File directorio = archivoFinal.getParentFile();
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        // Usamos un Map para filtrar duplicados basados en una "llave" compuesta
        // Llave: RUT + Direccion + Comuna
        Map<String, ExcelNotificacionToNormalize> registrosUnicos = new LinkedHashMap<>();
        for (ExcelNotificacionToNormalize item : listaNormalizada) {
            if (!registrosUnicos.containsKey(item.getClientId())) {
                registrosUnicos.put(item.getClientId(), item);
            }
        }

        byte[] contenidoCsv = null;

        // 2. Generar CSV en disco y en memoria simultáneamente
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             BufferedWriter memoryWriter = new BufferedWriter(osw);
             BufferedWriter fileWriter = new BufferedWriter(new FileWriter(archivoCsvToNormalize, StandardCharsets.UTF_8))) {

            String encabezado = "clientId;rut;nombre;direccion;comuna;toNormalize";

            // Escribir encabezado en ambos
            fileWriter.write(encabezado); fileWriter.newLine();
            memoryWriter.write(encabezado); memoryWriter.newLine();

            for (ExcelNotificacionToNormalize unico : registrosUnicos.values()) {
                String fila = String.format("%s;%s;%s;%s;%s;%s",
                        unico.getClientId(),
                        unico.getRut(),
                        unico.getNombreCompleto(),
                        unico.getDireccion(),
                        unico.getComuna(),
                        unico.getToNormalize()
                );
                // Escribir fila en ambos
                fileWriter.write(fila); fileWriter.newLine();
                memoryWriter.write(fila); memoryWriter.newLine();
            }

            // Importante hacer flush para asegurar que todo pase al ByteArrayOutputStream
            memoryWriter.flush();
            contenidoCsv = baos.toByteArray();

            log.info("CSV generado con éxito: {}", archivoCsvToNormalize);

        } catch (IOException e) {
            log.error("Error al generar el CSV: {}", e.getMessage());
        }

        try{
            Path path = Paths.get(archivoCsvToNormalize);
            int count = path.getNameCount();

// Obtenemos el penúltimo (carpeta) y el último (archivo)
            nombreArchivoCsvToNormalize = path.getName(count - 1).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ejecutarUpload.setContenidoCsv(contenidoCsv);
        ejecutarUpload.setRegistrosUnicos(String.valueOf( registrosUnicos.size()));
        ejecutarUpload.setPathArchivoNormalizadoCsv(archivoCsvToNormalize);
        ejecutarUpload.setNombreArchivoCsvToNormalize(nombreArchivoCsvToNormalize);

        return ejecutarUpload;

    }
}
