package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.ObtenerExcel;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.EjecutarProcesoCarta;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.EjecutarProcesoCartaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CCorreosToNormalizeService {

    private final DProcesarCartaCobranzaService procesarCartaCobranzaService;

    private final EjecutarProcesoCartaRepository ejecutarProcesoCartaRepository;

    private final ApiProperties apiProperties;

    private final AFileStorageComponent storageComponent;

    /***Primer Metodo de Procesar***/
    public ResponseEntity<?> fromCorreosToMerge(EjecutarMerge ejecutarMerge) throws Exception {

        String[] carpetaSlipt = ejecutarMerge.getRutaExcelUnion().split("/");

        String pathCarpetaNormalizado = Paths.get(
                apiProperties.getArchivoCreacionCarpeta(), // Ruta base (ej. /public_sftp/)
                "3_normalizado",
                ejecutarMerge.getTipo().toLowerCase(),
                ejecutarMerge.getUnidad().toLowerCase(),
                carpetaSlipt[0]
        ).toString();

        String dirJsonNormalizado = pathCarpetaNormalizado.concat("/").
                concat(carpetaSlipt[1]).replace(".xlsx", ".json");

        EjecutarMerge ejecutarMergePr = new EjecutarMerge();
        EjecutarUpload ejecutarUpload = CrearJsonExcel.getEjecutarUploadFromJson(dirJsonNormalizado);
        BeanUtils.copyProperties(ejecutarUpload, ejecutarMergePr);
        // 2. PASO CRÍTICO: Guardar el archivo CSV de correos en disco AHORA
        // Debes implementar un método en tu storageService para guardar este archivo específico
        // antes de que el request termine.
        String pathCsvFisico = storageComponent.guardarCsvNormalizadoCorreos(
                ejecutarMerge.getFileCorreosCsv(),
                ejecutarMerge.getTipo(),
                ejecutarMerge.getUnidad(),
                carpetaSlipt[0]
        );

        ejecutarMergePr.setUsuarioMerge(ejecutarMerge.getUsuarioMerge());
        ejecutarMergePr.setRutaExcelUnion(ejecutarMerge.getRutaExcelUnion());
        ejecutarMergePr.setTipo(ejecutarMerge.getTipo());
        ejecutarMergePr.setUnidad(ejecutarMerge.getUnidad());
        ejecutarMergePr.setPathCsvCorreos(pathCsvFisico);
        ejecutarMergePr.setFileCorreosCsv(ejecutarMerge.getFileCorreosCsv());
        // 3. DISPARAR ASÍNCRONO
        ejecutarMergePr = executeCorreosToMerge(ejecutarMergePr);

        //pasar a ejecutarMerge
        ejecutarMerge.setListaExcelCobranzaMerge(ejecutarMergePr.getListaExcelCobranzaMerge());

        CrearJsonExcel.crearJson4Merge(ejecutarMergePr);
        saveListMerge(ejecutarMergePr);
        //log.info("lista 4-2 {}", ejecutarMergePr.getListaExcelCobranzaNormalizado().size());

        //devolver a ejecutarMergePr
        ejecutarMergePr.setListaExcelCobranzaMerge(ejecutarMerge.getListaExcelCobranzaMerge());

        /******************
         File archivoExcel = new File(ejecutarMergePr.getPathArchivoNormalizado());

         if (ejecutarMerge.getTipo().equalsIgnoreCase("COBRANZA")) {

         List<ExcelCobranzaToNormalize> excelCobranzasToNormalize = new ArrayList<>();

         if (archivoExcel.exists()) {
         try (InputStream excelInputStream = new FileInputStream(archivoExcel)) {

         log.info("Abriendo Excel: " + archivoExcel.getAbsolutePath());

         excelCobranzasToNormalize =
         ObtenerExcel.obtenerExcelCobranzaNormalizado(excelInputStream, apiProperties.getArchivoExcelNombreHojaNormalizadaCobranza());
         }
         }
         }********/
        //ejecutarMergePr = procesarCartaCobranzaService.processArchivoCobranza(ejecutarMergePr);
        return ResponseEntity.ok(Map.of(
                "message", "Subida exitosa",
                "ruta", ejecutarMergePr.getPathArchivoMerge()
        ));
    }

    // Inventar un correlativo que se obtenga del valor de las carpetas
    private EjecutarMerge executeCorreosToMerge(EjecutarMerge ejecutarMerge) throws Exception {

        log.info("processRequest {} tipo {} user {} archivo {}",
                apiProperties.getProfile(),
                ejecutarMerge.getTipo(),
                ejecutarMerge.getUsuarioMerge(),
                ejecutarMerge.getFileCorreosCsv());



        if (ejecutarMerge.getTipo().equalsIgnoreCase("COBRANZA")) {

            ResultadoValidacion resultadoValidacion = validarCsvCorreosCobranza(ejecutarMerge);

            File archivoExcel = new File(ejecutarMerge.getPathArchivoNormalizado());

            List<ExcelCobranzaToNormalize> excelCobranzasToNormalize = new ArrayList<>();

            if (archivoExcel.exists()) {
                try (InputStream excelInputStream = new FileInputStream(archivoExcel)) {

                    log.info("Abriendo Excel: " + archivoExcel.getAbsolutePath());

                    excelCobranzasToNormalize =
                            ObtenerExcel.obtenerExcelCobranzaNormalizado(excelInputStream, apiProperties.getArchivoExcelNombreHojaNormalizada());
                    try{
                        ejecutarMerge = exportarANuevoExcelCobranzaMerge(
                                excelCobranzasToNormalize,
                                resultadoValidacion.getCoincidentes(),
                                ejecutarMerge);
                        log.info("lista 2 {}", ejecutarMerge.getListaExcelCobranzaMerge().size());
                    } catch (Exception e) {
                        throw new Exception("Error al leer el archivo Excel Normalizado", e);
                    }
                } catch (IOException e) {
                    throw new Exception("Error al leer el archivo Excel Normalizado", e);
                }
            } else {
                throw new Exception("No se encontró el archivo Excel en: " + archivoExcel.getAbsolutePath());
            }

            log.info(" resultadoValidacion {}", resultadoValidacion.getCoincidentes().size());
        }
        else if (ejecutarMerge.getTipo().equalsIgnoreCase("NOTIFICACION")) {

            ResultadoValidacion resultadoValidacion = validarCsvCorreosNotificacion(ejecutarMerge);

            File archivoExcel = new File(ejecutarMerge.getPathArchivoNormalizado());

            List<ExcelCobranzaToNormalize> excelCobranzasToNormalize = new ArrayList<>();

            if (archivoExcel.exists()) {
                try (InputStream excelInputStream = new FileInputStream(archivoExcel)) {

                    log.info("Abriendo Excel: " + archivoExcel.getAbsolutePath());

                    excelCobranzasToNormalize =
                            ObtenerExcel.obtenerExcelCobranzaNormalizado(excelInputStream, apiProperties.getArchivoExcelNombreHojaNormalizada());
                    try{
                        ejecutarMerge = exportarANuevoExcelCobranzaMerge(
                                excelCobranzasToNormalize,
                                resultadoValidacion.getCoincidentes(),
                                ejecutarMerge);
                        log.info("lista 2 {}", ejecutarMerge.getListaExcelCobranzaMerge().size());
                    } catch (Exception e) {
                        throw new Exception("Error al leer el archivo Excel Normalizado", e);
                    }
                } catch (IOException e) {
                    throw new Exception("Error al leer el archivo Excel Normalizado", e);
                }
            } else {
                throw new Exception("No se encontró el archivo Excel en: " + archivoExcel.getAbsolutePath());
            }

            log.info(" resultadoValidacion {}", resultadoValidacion.getCoincidentes().size());
        }

        ejecutarMerge.setFechaCreacionMerge(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));

        return ejecutarMerge;
    }

    private EjecutarMerge exportarANuevoExcelCobranzaMerge(List<ExcelCobranzaToNormalize> listaToNormalize,
                                                           List<ExcelCorreos> listCsvSeguimiento,
                                                           EjecutarMerge ejecutarMerge) {

        int totalFilasGeneradas = 0;
        List<ExcelCobranzaMerge> listaFiltrada = null;
        Map<String, String> mapaSeguimiento = listCsvSeguimiento.stream()
                .filter(c -> c.getClientId() != null && c.getCodigo_seguimiento() != null)
                .collect(Collectors.toMap(
                        c -> normalizeClientId(c.getClientId()), // Llave normalizada
                        ExcelCorreos::getCodigo_seguimiento, // Valor: el código
                        (existente, reemplazo) -> existente // En caso de duplicados, mantenemos el primero
                ));
        /***Excel respaldado en 3_normalizado se mapaSeguimiento*/
        List<ExcelCobranzaMerge> listaExcelCobranzaMerge = listaToNormalize.stream()
                .map(original -> {
                    String key = original.getClientId() != null ? original.getClientId().replaceFirst("^0+(?!$)", "") : "";

                    ExcelCobranzaMerge nuevo = new ExcelCobranzaMerge(
                            original.getCert1(), original.getCert2(), original.getFechaCarta(),
                            original.getVence(), original.getFolio(),
                            original.getApellidoPaterno(), original.getApellidoMaterno(),
                            original.getNombres(), original.getRut(),
                            original.getDv(),
                            original.getDireccion(),
                            original.getComuna(), original.getPlacaPatente(),
                            original.getDg(), original.getTipoVehiculo(), original.getRolMop(),
                            original.getFechaInfraccion(), original.getHoraInfraccion(),
                            original.getConvenio1(),
                            original.getConvenio2(),
                            original.getCodigoBarra(),
                            original.getValorMulta(),
                            original.getLugarMulta(),
                            original.getFechaCitacion(), original.getJuzgado(),
                            original.getPiso(), original.getClientId(), original.getToNormalize(),
                            ""
                    );

                    // Si existe en el mapa, le asignamos el valor
                    if (mapaSeguimiento.containsKey(key)) {
                        nuevo.setCodigoSeguimiento(mapaSeguimiento.get(key));
                    }

                    return nuevo;
                })
                // FILTRO: Solo dejamos los que tengan un código de seguimiento asignado (no nulo y no vacío)
                .filter(item -> item.getCodigoSeguimiento() != null && !item.getCodigoSeguimiento().isBlank())
                .collect(Collectors.toList());

        String archivoExcelMerge = null;

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(apiProperties.getArchivoExcelNombreHojaMergeCobranza());

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
                    "Juzgado", "Piso", "ClientId", "Seguimiento"
            };

            // 3. Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            log.info("Generando Merge en Excel con {} registros...", listaExcelCobranzaMerge.size());

            // 4. Datos
            int rowNum = 1;
            int contador = 0;

            listaFiltrada = listaExcelCobranzaMerge.stream()
                    .filter(item -> item.getCodigoSeguimiento() != null && !item.getCodigoSeguimiento().isBlank())
                    .collect(Collectors.toList());
            /***List<ExcelCobranzaMerge> listaFiltrada hacerla excel*/
            for (ExcelCobranzaMerge item : listaFiltrada) {

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
                row.createCell(27).setCellValue(nvl(item.getCodigoSeguimiento()));
            }

            // 5. Validación
            totalFilasGeneradas = rowNum - 1;

            log.info("Total iterados: {}", contador);
            log.info("Total filas Excel: {}", totalFilasGeneradas);
            log.info("Total lista original: {}", listaToNormalize.size());
            log.info("Total validadas: {}", listaFiltrada.size());

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
            // 7. Ruta archivo
            String nombreArchivo = ejecutarMerge.getUnidad().concat(apiProperties.getArchivoExcelNombreArchivoMergeCobranza());

            archivoExcelMerge = apiProperties.getArchivoCreacionCarpeta()
                    .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaMerge())
                    .concat(ejecutarMerge.getTipo()).concat("/")
                    .concat(ejecutarMerge.getUnidad()).concat("/")
                    .concat(ejecutarMerge.getBaseNombre()).concat("/")
                    .concat(ejecutarMerge.getBaseNombre()).concat("_")
                    //  .concat(proceso).concat("_")
                    .concat(nombreArchivo);

            log.info("Ruta archivo: {}", archivoExcelMerge);

            File archivoFinal = new File(archivoExcelMerge);

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

            log.info("Archivo Excel Merge generado con éxito.");


        } catch (IOException e) {
            log.error("❌ Error al generar el archivo: {}", e.getMessage(), e);
        }

        ejecutarMerge.setPathArchivoMerge(archivoExcelMerge);
        ejecutarMerge.setSizeArchivoMerge(String.valueOf(totalFilasGeneradas));
        ejecutarMerge.setNombreArchivoMerge(archivoExcelMerge);
        ejecutarMerge.setTotalFilasGeneradasExcel(String.valueOf(totalFilasGeneradas));
        //Aqui esta la base para realizar
        ejecutarMerge.setListaExcelCobranzaMerge(listaFiltrada);
        log.info("lista {}", ejecutarMerge.getListaExcelCobranzaMerge().size());
        return ejecutarMerge;
    }

    private ResultadoValidacion validarCsvCorreosCobranza(EjecutarMerge ejecutarMerge) {

        List<ExcelCorreos> listCsvSeguimiento;

        try (InputStream csvInputStream = Files.newInputStream(Paths.get(ejecutarMerge.getPathCsvCorreos()))) {
            //Seguimiento csv COBRANZA - NOTIFICACION
            listCsvSeguimiento = ObtenerExcel.obtenerExcelCorreosCsv(csvInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<ExcelCorreos> listCsvOriginal;

        String pathNormalizadoCsv = ejecutarMerge.getPathArchivoNormalizado().replace(".xlsx", ".csv");

        pathNormalizadoCsv = pathNormalizadoCsv.replace(".csv", "_EnviarCorreos.csv");

        try (InputStream csvInputStream = Files.newInputStream(Paths.get(pathNormalizadoCsv))) {
            // 2. Cargamos la lista desde el InputStream del archivo normalizado
            //ORIGINAL csv COBRANZA - NOTIFICACION
            listCsvOriginal = ObtenerExcel.obtenerExcelCorreosCsv(csvInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // 1. Mapa usando clave compuesta: toNormalize
        Map<String, ExcelCorreos> mapaSeguimiento = listCsvSeguimiento.stream()
                .collect(Collectors.toMap(
                        item -> generarClaveUnicaCsv(item),
                        item -> item,
                        (a, b) -> a // En caso de duplicados en seguimiento, mantenemos el primero
                ));

        List<ExcelCorreos> coincidentes = new ArrayList<>();
        int contadorNoCoincidentes = 0;

        // 2. Recorremos la lista original buscando por la misma clave compuesta - toNormalize
        for (ExcelCorreos original : listCsvOriginal) {
            String claveOriginal = generarClaveUnicaCsv(original);
            ExcelCorreos seguimiento = mapaSeguimiento.get(claveOriginal);

            if (seguimiento != null) {
                // Si la clave coincide, lo consideramos coincidente
                coincidentes.add(seguimiento);
            } else {
                contadorNoCoincidentes++;
                log.info("no coincidente {} v/s{ }", original.getClientId(), seguimiento.getClientId());
            }
        }

        return new ResultadoValidacion(coincidentes, contadorNoCoincidentes);
        /*** return new ResultadoValidacion(listCsvSeguimiento, 0);***/
    }

    private ResultadoValidacion validarCsvCorreosNotificacion(EjecutarMerge ejecutarMerge) {

        List<ExcelCorreos> listCsvSeguimiento;

        try (InputStream csvInputStream = Files.newInputStream(Paths.get(ejecutarMerge.getPathCsvCorreos()))) {
            //Seguimiento csv COBRANZA - NOTIFICACION
            listCsvSeguimiento = ObtenerExcel.obtenerExcelCorreosCsv(csvInputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<ExcelCorreos> listCsvOriginal;

        String pathNormalizadoCsv = ejecutarMerge.getPathArchivoNormalizado().replace(".xlsx", ".csv");

        pathNormalizadoCsv = pathNormalizadoCsv.replace(".csv", "_EnviarCorreos.csv");

        try{
            try (InputStream csvInputStream = Files.newInputStream(Paths.get(pathNormalizadoCsv))) {
                // 2. Cargamos la lista desde el InputStream del archivo normalizado
                //ORIGINAL csv COBRANZA - NOTIFICACION
                listCsvOriginal = ObtenerExcel.obtenerExcelCorreosCsv(csvInputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }



        // 1. Mapa usando clave compuesta: toNormalize
        Map<String, ExcelCorreos> mapaSeguimiento = listCsvSeguimiento.stream()
                .collect(Collectors.toMap(
                        item -> generarClaveUnicaCsv(item),
                        item -> item,
                        (a, b) -> a // En caso de duplicados en seguimiento, mantenemos el primero
                ));

        List<ExcelCorreos> coincidentes = new ArrayList<>();
        int contadorNoCoincidentes = 0;

        // 2. Recorremos la lista original buscando por la misma clave compuesta - toNormalize
        for (ExcelCorreos original : listCsvOriginal) {
            String claveOriginal = generarClaveUnicaCsv(original);
            ExcelCorreos seguimiento = mapaSeguimiento.get(claveOriginal);

            if (seguimiento != null) {
                // Si la clave coincide, lo consideramos coincidente
                coincidentes.add(seguimiento);
            } else {
                contadorNoCoincidentes++;
                log.info("no coincidente {} v/s{ }", original.getClientId(), seguimiento.getClientId());
            }
        }

        return new ResultadoValidacion(coincidentes, contadorNoCoincidentes);
        /*** return new ResultadoValidacion(listCsvSeguimiento, 0);***/
    }

    /**
     * Genera una clave normalizada para evitar fallos por mayúsculas o espacios extra.
     */
    private String generarClaveUnicaCsv(ExcelCorreos item) {
        log.info(item.getToNormalize());
        return item.getToNormalize().trim();
    }

    private String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    //Quitar 0s a la izquierda
    private String normalizeClientId(String clientId) {
        if (clientId == null) return "";
        // Reemplaza todos los ceros al inicio, a menos que el número sea solo "0"
        return clientId.replaceFirst("^0+(?!$)", "");
    }

    private String nvl(Object value) {
        return value == null ? "" : value.toString();
    }

    @Transactional
    protected void saveListMerge(EjecutarMerge ejecutarMerge) {

        EjecutarProcesoCarta ejecutarProcesoCarta = new EjecutarProcesoCarta();

        // 2. Mapear los campos según tu solicitud
        // fechaRegistroCreacionMerge se llena solo si usaste @CreationTimestamp,
        // si no, lo seteamos manualmente:
        ejecutarProcesoCarta.setCodEjecutarProcesoCarta(ejecutarMerge.getBaseNombre());
        ejecutarProcesoCarta.setFechaRegistroCreacionMerge(LocalDateTime.now());

        // Carpeta del archivo (folderArchivoMerge)
        ejecutarProcesoCarta.setPathArchivoMerge(ejecutarMerge.getPathArchivoMerge());

        // Tipo de carta (Cobranza - Notificacion)
        // Aquí concatenamos o mapeamos el tipo según tu lógica
        // String tipoFinal = ejecutarMerge.getTipo();
        ejecutarProcesoCarta.setTipoCarta(ejecutarMerge.getTipo());
        ejecutarProcesoCarta.setUnidad(ejecutarMerge.getUnidad());
        // Estado inicial
        ejecutarProcesoCarta.setEjecutado(false);

        // 3. Guardar en la base de datos
        ejecutarProcesoCartaRepository.save(ejecutarProcesoCarta);
    }
}
/***return Map.of(
 // Útil para el correo
 "dirArchivoExcelMerge", archivoExcelMerge,
 "nombreArchivoExcelMerge", archivoExcelMerge,
 "totalFilasGeneradas", totalFilasGeneradas,
 "listaExcelMerge", listaExcelCobranzaMerge
 );***/
//dirExcelNormalizado = dirExcelNormalizado.replace("public_sftp/", apiProperties.getArchivoCreacionCarpeta());

/***CrearJsonExcel.crearJson4Merge(dirExcelNormalizado,
 (String) mapaMerge.get("archivoExcelMerge"),
 LocalDateTime.now()
 .format(DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss")),
 (String) mapaMerge.get("archivoExcelMerge"),
 (int) mapaMerge.get("totalFilasGeneradas"));***/

//return (String) mapaMerge.get("archivoExcelMerge");
/***String proceso = now
 .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));

 String fechaCreacionNormalizacion = now
 .format(DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss"));***/

/***if(ejecutarMergePr.getTipo().equalsIgnoreCase(COBRANZA)){
 toMerge(
 ejecutarMergePr,
 ejecutarMergePr.getFileCsv());
 //processToUnnormToMerge(proceso, fechaCreacionNormalizacion, user, observacion, ejecutarNotificacion, file);
 }
 if(ejecutarMergePr.getTipo().equalsIgnoreCase(NOTIFICACION)){
 //   processRequestNotificacion(proceso, fechaCreacionDesnormalizado, ejecutarNotificacion);
 }***/
//}

/***private String toMerge(
 EjecutarMerge ejecutarNotificacion,
 MultipartFile fileCorreosCsv)  {
 try{

 List<ExcelCobranzaCorreos>
 listCsvSeguimiento =  ObtenerExcel.obtenerExcelCorreosCsv(fileCorreosCsv.getInputStream());

 Resource resource = new ClassPathResource(
 apiProperties.getArchivoExcelNombreCarpetaNormalizada().
 concat(ejecutarNotificacion.getTipo()).concat("/").
 concat(ejecutarNotificacion.getUnidad()).concat("/").
 concat(ejecutarNotificacion.getNombreArchivoNormalizado().replace(".json", ".xlsx"))

 );

 InputStream inputStream = resource.getInputStream();

 List<ExcelCobranzaToNormalize> excelCobranzasToNormalize =
 ObtenerExcel.obtenerExcelCobranzaMerge(inputStream, apiProperties.getArchivoExcelNombreHojaNormalizadaCobranza());

 log.info(" excelCobranzasDesnormalizado {}", excelCobranzasToNormalize.size() );
 log.info(" listCsvSeguimiento {}", listCsvSeguimiento.size() );

 String archivoExcelMerge = exportarANuevoExcelCobranzaMerge(
 excelCobranzasToNormalize,
 listCsvSeguimiento,
 ejecutarNotificacion.getBaseNombre(),
 ejecutarNotificacion);

 CrearJsonExcel.crearJson4Merge(
 apiProperties.getArchivoCreacionCarpeta().concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNormalizado())
 .concat(ejecutarNotificacion.getTipo()).concat("/")
 .concat(ejecutarNotificacion.getUnidad()).concat("/")
 .concat(ejecutarNotificacion.getNombreArchivoNormalizado()),
 archivoExcelMerge,
 null,//user,
 null, //fechaCreacionMerge,
 archivoExcelMerge,
 //observacion,
 null, excelCobranzasToNormalize.size(),
 listCsvSeguimiento.size());

 return archivoExcelMerge;

 }catch(Exception e){
 log.error("Exception error", e);
 }
 throw new ProcesoNotificacionCartaException();
 }---/










 /************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/***public void generarCSVUnicosCobranza(List<ExcelCobranzaToNormalize> listaNormalizada,
 String usuarioDesnormalizado,
 String fechaCreacionDesnormalizado,
 Path pathOrigen,
 Path pathDestino,
 String archivoExcelToNormalize) {

 String nombreArchivoCsv = archivoExcelToNormalize.replace(".xlsx", "_EnviarCorreos.csv");

 log.info("listaNormalizada. Csv {}", listaNormalizada.size());
 log.info("To Correos. Csv {}", nombreArchivoCsv);
 File archivoFinal = new File(nombreArchivoCsv);

 File directorio = archivoFinal.getParentFile();
 if (!directorio.exists()) {
 directorio.mkdirs();
 }
 // Usamos un Map para filtrar duplicados basados en una "llave" compuesta
 // Llave: RUT + Direccion + Comuna
 Map<String, ExcelCobranzaToNormalize> registrosUnicos = new LinkedHashMap<>();

 for (ExcelCobranzaToNormalize item : listaNormalizada) {
 //String llave = item.getRut() + "|" + item.getDireccion() + "|" + item.getComuna();
 String llave = item.getClientId();
 // Si la llave no existe, la agregamos (así mantenemos el primero que aparezca)
 if (!registrosUnicos.containsKey(llave)) {
 registrosUnicos.put(llave, item);
 }
 }

 // Escribir el archivo CSV
 try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivoCsv, StandardCharsets.UTF_8))) {
 // Escribir Encabezado
 writer.write("clientId;rut;nombre;direccion;comuna;toNormalize");
 writer.newLine();

 // Escribir Datos
 for (ExcelCobranzaToNormalize unico : registrosUnicos.values()) {
 String fila = String.format("%s;%s;%s;%s;%s;%s",
 unico.getClientId(),
 unico.getRut(),
 unico.getNombres() + " " + unico.getApellidoPaterno(),
 unico.getDireccion(),
 unico.getComuna(),
 unico.getToNormalize()
 );
 writer.write(fila);
 writer.newLine();
 }
 log.info("CSV generado con éxito: " + nombreArchivoCsv);

 CrearJsonExcel.crearJson3Normalizado(
 usuarioDesnormalizado,
 fechaCreacionDesnormalizado,
 pathOrigen.toString(),
 pathDestino.toString(),
 archivoExcelToNormalize,
 nombreArchivoCsv);


 } catch (IOException e) {
 System.err.println("Error al escribir el CSV: " + e.getMessage());
 }
 }***/

/***
 public String exportarANuevoExcelNotificacion(List<ExcelNotificacionNormalizado> listaNormalizada,
 String proceso, String tipo, String unidad) {

 String archivoExcelToNormalize = null;
 // 1. Crear el libro de trabajo (.xlsx)
 try (Workbook workbook = new XSSFWorkbook()) {
 Sheet sheet = workbook.createSheet("ToNormalize");

 // 2. Estilo para el encabezado
 CellStyle headerStyle = workbook.createCellStyle();
 Font font = workbook.createFont();
 font.setBold(true);
 headerStyle.setFont(font);

 // 3. Definición de columnas solicitadas
 String[] columnas = {
 "juzgado", "nombre", "direccion", "comuna", "rol", "anho", "MAC", "rut", "placaPatente",
 "tipoVehiculo", "fechaInfraccion", "horaInfraccion", "fechaCitacion", "horaCitacion", "codInterno", "vence", "folio", "clientId", "toNormalize"
 };

 Row headerRow = sheet.createRow(0);
 for (int i = 0; i < columnas.length; i++) {
 Cell cell = headerRow.createCell(i);
 cell.setCellValue(columnas[i]);
 cell.setCellStyle(headerStyle);
 }
 // 4. Llenar los datos
 int rowNum = 1;
 for (ExcelNotificacionNormalizado item : listaNormalizada) {
 Row row = sheet.createRow(rowNum++);

 // Mapeo correlativo según el orden de tu lista de columnas
 row.createCell(0).setCellValue(item.getJuzgado());
 row.createCell(1).setCellValue(item.getNombre());
 row.createCell(2).setCellValue(item.getDireccion());
 row.createCell(3).setCellValue(item.getComuna());
 row.createCell(4).setCellValue(item.getRol());
 row.createCell(5).setCellValue(item.getAnho());
 row.createCell(6).setCellValue(item.getMAC());
 row.createCell(7).setCellValue(item.getRut());
 row.createCell(8).setCellValue(item.getPlacaPatente());
 row.createCell(9).setCellValue(item.getTipoVehiculo());
 row.createCell(10).setCellValue(item.getFechaInfraccion());
 row.createCell(11).setCellValue(item.getHoraInfraccion());
 row.createCell(12).setCellValue(item.getFechaCitacion());
 row.createCell(13).setCellValue(item.getHoraCitacion());
 row.createCell(14).setCellValue(item.getCodInterno());
 row.createCell(15).setCellValue(item.getVence());
 row.createCell(16).setCellValue(item.getFolio());
 row.createCell(17).setCellValue(item.getClientId());
 row.createCell(18).setCellValue(item.getToNormalize());
 break;
 }

 // 5. Ajustar ancho de columnas (Opcional: puede ser lento con miles de filas)
 for (int i = 0; i < columnas.length; i++) {
 sheet.autoSizeColumn(i);
 }
 String nombreArchivo = unidad.concat("_Notificacion_ToNormalize.xlsx");
 archivoExcelToNormalize = apiProperties.getArchivoCreacionCarpeta()
 .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaDesnormalizado())
 .concat(tipo).concat("/")
 .concat(unidad).concat("/")
 .concat(proceso).concat("/")
 .concat(proceso).concat("_")
 .concat(nombreArchivo);
 log.info("to save. {}", archivoExcelToNormalize);
 File archivoFinal = new File(archivoExcelToNormalize);

 File directorio = archivoFinal.getParentFile();
 if (!directorio.exists()) {
 directorio.mkdirs();
 }
 // 6. Escritura del archivo
 try (FileOutputStream fileOut = new FileOutputStream(archivoFinal)) {
 workbook.write(fileOut);
 }
 log.info("Archivo Excel generado con éxito. {}", archivoExcelToNormalize);
 } catch (IOException e) {
 System.err.println("Error al generar el archivo: " + e.getMessage());
 e.printStackTrace();
 }
 return archivoExcelToNormalize;
 }***/


/***
 public List<ExcelNotificacionNormalizado> generarCSVUnicosNotificacion(List<ExcelNotificacionNormalizado> listaNormalizada,
 String usuarioDesnormalizado,
 String fechaCreacionDesnormalizado,
 Path pathOrigen,
 Path pathDestino,
 String archivoExcelToNormalize) {

 String nombreArchivoCsv = archivoExcelToNormalize.replace(".xlsx", "_EnviarCorreos.csv");

 log.info("To Correos. Csv {}", nombreArchivoCsv);
 File archivoFinal = new File(nombreArchivoCsv);

 File directorio = archivoFinal.getParentFile();
 if (!directorio.exists()) {
 directorio.mkdirs();
 }
 // Usamos un Map para filtrar duplicados basados en una "llave" compuesta
 // Llave: RUT + Direccion + Comuna
 Map<String, ExcelNotificacionNormalizado> registrosUnicos = new LinkedHashMap<>();

 for (ExcelNotificacionNormalizado item : listaNormalizada) {
 String llave = item.getRut() + "|" + item.getDireccion() + "|" + item.getComuna();

 // Si la llave no existe, la agregamos (así mantenemos el primero que aparezca)
 if (!registrosUnicos.containsKey(llave)) {
 registrosUnicos.put(llave, item);
 }
 }

 // Escribir el archivo CSV
 try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivoCsv, StandardCharsets.UTF_8))) {
 // Escribir Encabezado
 writer.write("clientId;rut;nombre;direccion;comuna;toNormalize");
 writer.newLine();

 // Escribir Datos
 for (ExcelNotificacionNormalizado unico : registrosUnicos.values()) {
 String fila = String.format("%s;%s;%s;%s;%s;%s",
 unico.getClientId(),
 unico.getRut(),
 unico.getNombre(),
 unico.getDireccion(),
 unico.getComuna(),
 unico.getToNormalize()
 );
 writer.write(fila);
 writer.newLine();
 }

 log.info("CSV generado con éxito: " + nombreArchivoCsv);

 CrearJsonExcel.crearJson2Desnormalizado(
 usuarioDesnormalizado,
 fechaCreacionDesnormalizado,
 pathOrigen.toString(),
 pathDestino.toString(),
 archivoExcelToNormalize,
 nombreArchivoCsv);

 } catch (IOException e) {
 System.err.println("Error al escribir el CSV: " + e.getMessage());
 }

 return listaNormalizada;
 }---/
 }***/
