package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.AFileStorageComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import com.ragnax.sanbernardo.notificacion.application.service.model.exceptions.ImsbException;
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
import org.springframework.http.HttpStatus;
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
        ejecutarMergePr = executeCorreosToMerge(ejecutarMerge, ejecutarMergePr);

        //Realizado el merge, enviar correo indicando procesamiento de archivo merge pendiente
        return ResponseEntity.ok(Map.of(
                "message", "Subida exitosa",
                "ruta", ejecutarMergePr.getPathArchivoMerge()
        ));
    }

    public EjecutarMerge executeCorreosToMerge(EjecutarMerge ejecutarMerge, EjecutarMerge ejecutarMergePr) throws Exception {

        ejecutarMergePr = executeCorreosToMerge(ejecutarMergePr);

        log.info("PathArchivoMerge: {}", ejecutarMergePr.getPathArchivoMerge());
        if(ejecutarMergePr.getTipo().equalsIgnoreCase("COBRANZA")) {
            ejecutarMerge.setListaExcelCobranzaMerge(ejecutarMergePr.getListaExcelCobranzaMerge());
        }else{
            ejecutarMerge.setListaExcelNotificacionMerge(ejecutarMergePr.getListaExcelNotificacionMerge());
        }
        CrearJsonExcel.crearJson4Merge(ejecutarMergePr);

        saveEjecutarProcesoCarta(ejecutarMergePr);
        //log.info("lista 4-2 {}", ejecutarMergePr.getListaExcelCobranzaNormalizado().size());
        //devolver a ejecutarMergePr
        //ejecutarMergePr.setListaExcelCobranzaMerge(ejecutarMerge.getListaExcelCobranzaMerge());
        return ejecutarMergePr;
    }



    // Inventar un correlativo que se obtenga del valor de las carpetas
    private EjecutarMerge executeCorreosToMerge(EjecutarMerge ejecutarMerge) throws Exception {

        log.info("processRequest {} tipo {} user {} archivo {}",
                apiProperties.getProfile(),
                ejecutarMerge.getTipo(),
                ejecutarMerge.getUsuarioMerge(),
                ejecutarMerge.getFileCorreosCsv());

        ResultadoValidacion resultadoValidacion = validarCsvCorreos(ejecutarMerge, ejecutarMerge.getTipo().toUpperCase());

        if(resultadoValidacion.getNoCoincidentes() > 0) {
            throw new ImsbException("No se pudo Generar Cartas Archivos disconformes", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        File archivoExcel = new File(ejecutarMerge.getPathArchivoNormalizado());
        if (!archivoExcel.exists()) {
            log.info("No se encontró el archivo Excel en: {} ", archivoExcel.getAbsolutePath());
            throw new ImsbException("No se encontró el archivo Excel en: " + archivoExcel.getAbsolutePath(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 3. Orquestación del procesamiento del Excel en un único bloque try-catch
        try (InputStream excelInputStream = new FileInputStream(archivoExcel)) {
            log.info("Abriendo Excel: " + archivoExcel.getAbsolutePath());
            String nombreHoja = apiProperties.getArchivoExcelNombreHojaNormalizada();

            if ("COBRANZA".equalsIgnoreCase(ejecutarMerge.getTipo())) {
                // Flujo específico de Cobranza
                List<ExcelCobranzaToNormalize> excelCobranzasToNormalize =
                        ObtenerExcel.obtenerExcelCobranzaNormalizado(excelInputStream, nombreHoja);

                try {
                    ejecutarMerge = exportarANuevoExcelCobranzaMerge(
                            excelCobranzasToNormalize, resultadoValidacion.getCoincidentes(), ejecutarMerge);
                    log.info("lista 2 {}", ejecutarMerge.getListaExcelCobranzaMerge().size());
                } catch (Exception e) {
                    throw new ImsbException("exportarANuevoExcelCobranzaMerge Error al leer el archivo Excel Normalizado", HttpStatus.INTERNAL_SERVER_ERROR);
                }

            } else if ("NOTIFICACION".equalsIgnoreCase(ejecutarMerge.getTipo())) {
                // Flujo específico de Notificación
                List<ExcelNotificacionToNormalize> excelNotificacionesToNormalize =
                        ObtenerExcel.obtenerExcelNotificacionNormalizado(excelInputStream, nombreHoja);

                try {
                    ejecutarMerge = exportarANuevoExcelNotificacionMerge(
                            excelNotificacionesToNormalize, resultadoValidacion.getCoincidentes(), ejecutarMerge);
                    log.info("lista 2 {}", ejecutarMerge.getListaExcelNotificacionMerge().size());
                } catch (Exception e) {
                    throw new ImsbException("exportarANuevoExcelNotificacionMerge Error al leer el archivo Excel Normalizado", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

        } catch (IOException e) {
            log.info("Error al leer el archivo Excel Normalizado {}", archivoExcel.getPath());
            throw new ImsbException("Error al leer el archivo Excel Normalizado", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 4. Seteo común de metadatos finales
        ejecutarMerge.setFechaCreacionMerge(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));

        return ejecutarMerge;
    }

    private EjecutarMerge exportarANuevoExcelCobranzaMerge(List<ExcelCobranzaToNormalize> listaToNormalize,
                                                           List<ExcelCorreos> listCsvSeguimiento,
                                                           EjecutarMerge ejecutarMerge) {
        int totalFilasGeneradas = 0;
        String[] metadatosRuta = null;
        Map<String, ExcelCorreos> mapaSeguimiento = generarMapaSeguimiento(listCsvSeguimiento);

        // Mapear y cruzar datos con el mapa de seguimiento
        List<ExcelCobranzaMerge> listaFiltrada = listaToNormalize.stream()
                .map(original -> {
                    String key = original.getClientId() != null ? normalizeClientId(original.getClientId()) : "";
                    ExcelCobranzaMerge nuevo = new ExcelCobranzaMerge(
                            original.getCert1(), original.getCert2(), original.getFechaCarta(),
                            original.getVence(), original.getFolio(), original.getApellidoPaterno(),
                            original.getApellidoMaterno(), original.getNombres(), original.getRut(),
                            original.getDv(), original.getDireccion(), original.getComuna(),
                            original.getPlacaPatente(), original.getDg(), original.getTipoVehiculo(),
                            original.getRolMop(), original.getFechaInfraccion(), original.getHoraInfraccion(),
                            original.getConvenio1(), original.getConvenio2(), original.getCodigoBarra(),
                            original.getValorMulta(), original.getLugarMulta(), original.getFechaCitacion(),
                            original.getJuzgado(), original.getPiso(), original.getClientId(),
                            original.getToNormalize(), "", "" , "" , "", "", ""
                    );

                    if (mapaSeguimiento.containsKey(key)) {
                        ExcelCorreos correoInfo = mapaSeguimiento.get(key);
                        nuevo.setCodigoSeguimiento(correoInfo.getCodigo_seguimiento());
                        nuevo.setCodigoPostal(correoInfo.getCodigo_postal());
                        nuevo.setIdSector(correoInfo.getIdSector());
                        nuevo.setIdCuartel(correoInfo.getIdCuartel());
                        nuevo.setServicio(correoInfo.getServicio());
                        nuevo.setDestinoClasificacion(correoInfo.getDestinoClasificacion());
                    }
                    return nuevo;
                })
                .filter(item -> item.getCodigoSeguimiento() != null && !item.getCodigoSeguimiento().isBlank())
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(apiProperties.getArchivoExcelNombreHojaMergeCobranza());

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] columnas = {
                    "Cert1", "Cert2", "Fecha Carta", "Vence", "Folio", "Ap_Paterno", "Ap_Materno",
                    "Nombres", "Rut", "Dv", "Direccion", "Comuna", "Placa", "Dg", "Tipo_Veh",
                    "RolMop", "Fecha_Infr", "Hora_Infr", "Conv1", "Conv2", "Codigo_Barra",
                    "Valor_Multa", "Lugar_Multa", "Fecha_Citacion", "Juzgado", "Piso", "ClientId",
                    "Seguimiento", "Codigo_Postal","SECTOR,","CUARTEL","SERVICIO","DESTINO.CLASIFICACION"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            log.info("Generando Merge Cobranza en Excel con {} registros...", listaFiltrada.size());

            int rowNum = 1;
            for (ExcelCobranzaMerge item : listaFiltrada) {
                if (item == null) continue;

                Row row = sheet.createRow(rowNum++);
                int i = 0;
                row.createCell(i).setCellValue(nvl(item.getCert1()));
                row.createCell(++i).setCellValue(nvl(item.getCert2()));
                row.createCell(++i).setCellValue(nvl(item.getFechaCarta()));
                row.createCell(++i).setCellValue(nvl(item.getVence()));
                row.createCell(++i).setCellValue(nvl(item.getFolio()));
                row.createCell(++i).setCellValue(nvl(item.getApellidoPaterno()));
                row.createCell(++i).setCellValue(nvl(item.getApellidoMaterno()));
                row.createCell(++i).setCellValue(nvl(item.getNombres()));
                row.createCell(++i).setCellValue(nvl(item.getRut()));
                row.createCell(++i).setCellValue(nvl(item.getDv()));
                row.createCell(++i).setCellValue(nvl(item.getDireccion()));
                row.createCell(++i).setCellValue(nvl(item.getComuna()));
                row.createCell(++i).setCellValue(nvl(item.getPlacaPatente()));
                row.createCell(++i).setCellValue(nvl(item.getDg()));
                row.createCell(++i).setCellValue(nvl(item.getTipoVehiculo()));
                row.createCell(++i).setCellValue(nvl(item.getRolMop()));
                row.createCell(++i).setCellValue(nvl(item.getFechaInfraccion()));
                row.createCell(++i).setCellValue(nvl(item.getHoraInfraccion()));
                row.createCell(++i).setCellValue(nvl(item.getConvenio1()));
                row.createCell(++i).setCellValue(nvl(item.getConvenio2()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoBarra()));
                row.createCell(++i).setCellValue(nvl(item.getValorMulta()));
                row.createCell(++i).setCellValue(nvl(item.getLugarMulta()));
                row.createCell(++i).setCellValue(nvl(item.getFechaCitacion()));
                row.createCell(++i).setCellValue(nvl(item.getJuzgado()));
                row.createCell(++i).setCellValue(nvl(item.getPiso()));
                row.createCell(++i).setCellValue(nvl(item.getClientId()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoSeguimiento()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoPostal()));

                row.createCell(++i).setCellValue(nvl(item.getIdSector()));
                row.createCell(++i).setCellValue(nvl(item.getIdCuartel()));
                row.createCell(++i).setCellValue(nvl(item.getServicio()));
                row.createCell(++i).setCellValue(nvl(item.getDestinoClasificacion()));
            }

            totalFilasGeneradas = rowNum - 1;
            validarTotalesLog(listaToNormalize.size(), totalFilasGeneradas, listaFiltrada.size());

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🚩 LLAMADA AL MÉTODO COMÚN EXTRAÍDO
            metadatosRuta = escribirArchivoExcelEnDisco(workbook, ejecutarMerge);

        } catch (IOException e) {
            log.error("❌ Error al generar el archivo Cobranza: {}", e.getMessage(), e);
        }

        if (metadatosRuta != null) {
            ejecutarMerge.setPathArchivoMerge(metadatosRuta[0]);
            ejecutarMerge.setNombreArchivoMerge(metadatosRuta[1]);
        }
        ejecutarMerge.setSizeArchivoMerge(String.valueOf(totalFilasGeneradas));
        ejecutarMerge.setTotalFilasGeneradasExcel(String.valueOf(totalFilasGeneradas));
        ejecutarMerge.setListaExcelCobranzaMerge(listaFiltrada);

        return ejecutarMerge;
    }

    private EjecutarMerge exportarANuevoExcelNotificacionMerge(List<ExcelNotificacionToNormalize> listaToNormalize,
                                                               List<ExcelCorreos> listCsvSeguimiento,
                                                               EjecutarMerge ejecutarMerge) {
        int totalFilasGeneradas = 0;
        String[] metadatosRuta = null;
        Map<String, ExcelCorreos> mapaSeguimiento = generarMapaSeguimiento(listCsvSeguimiento);

        // Mapear y cruzar datos con el mapa de seguimiento
        List<ExcelNotificacionMerge> listaFiltrada = listaToNormalize.stream()
                .map(original -> {
                    String key = original.getClientId() != null ? normalizeClientId(original.getClientId()) : "";
                    ExcelNotificacionMerge nuevo = new ExcelNotificacionMerge(
                            original.getJuzgado(), original.getNombreCompleto(), original.getDireccion(),
                            original.getComuna(), original.getRol(), original.getAnho(), original.getMac(),
                            original.getRut(), original.getPpu(), original.getVehiculo(),
                            original.getFechaInfraccion(), original.getHoraInfraccion(), original.getFechaCitacion(),
                            original.getHoraCitacion(), original.getCodigoInterno(), original.getFechaVencimiento(),
                            original.getFolio(), original.getClientId(), original.getToNormalize(), "", "", "", "", "", ""
                    );

                    if (mapaSeguimiento.containsKey(key)) {
                        ExcelCorreos correoInfo = mapaSeguimiento.get(key);
                        nuevo.setCodigoSeguimiento(correoInfo.getCodigo_seguimiento());
                        nuevo.setCodigoPostal(correoInfo.getCodigo_postal());
                        nuevo.setIdSector(correoInfo.getIdSector());
                        nuevo.setIdCuartel(correoInfo.getIdCuartel());
                        nuevo.setServicio(correoInfo.getServicio());
                        nuevo.setDestinoClasificacion(correoInfo.getDestinoClasificacion());
                    }
                    return nuevo;
                })
                .filter(item -> item.getCodigoSeguimiento() != null && !item.getCodigoSeguimiento().isBlank())
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(apiProperties.getArchivoExcelNombreHojaMergeCobranza());

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] columnas = {
                    "JUZGADO", "NOMBRE", "DIRECCION", "COMUNA", "ROL", "AÑO", "MAC", "RUT", "PPU",
                    "VEHICULO", "F.INFRACCION", "HORA.INFRACCION", "F.CITACION", "HORA.CITACION",
                    "COD.INTERNO", "F.VENCIMIENTO.BCO", "FOLIO", "CLIENT_ID", "SEGUIMIENTO", "CODIGO_POSTAL"
                    ,"SECTOR,","CUARTEL","SERVICIO","DESTINO.CLASIFICACION"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            log.info("Generando Merge Notificación en Excel con {} registros...", listaFiltrada.size());

            int rowNum = 1;
            for (ExcelNotificacionMerge item : listaFiltrada) {
                if (item == null) continue;

                Row row = sheet.createRow(rowNum++);
                int i = 0;
                row.createCell(i).setCellValue(nvl(item.getJuzgado()));
                row.createCell(++i).setCellValue(nvl(item.getNombreCompleto()));
                row.createCell(++i).setCellValue(nvl(item.getDireccion()));
                row.createCell(++i).setCellValue(nvl(item.getComuna()));
                row.createCell(++i).setCellValue(nvl(item.getRol()));
                row.createCell(++i).setCellValue(nvl(item.getAnho()));
                row.createCell(++i).setCellValue(nvl(item.getMac()));
                row.createCell(++i).setCellValue(nvl(item.getRut()));
                row.createCell(++i).setCellValue(nvl(item.getPpu()));
                row.createCell(++i).setCellValue(nvl(item.getVehiculo()));
                row.createCell(++i).setCellValue(nvl(item.getFechaInfraccion()));
                row.createCell(++i).setCellValue(nvl(item.getHoraInfraccion()));
                row.createCell(++i).setCellValue(nvl(item.getFechaCitacion()));
                row.createCell(++i).setCellValue(nvl(item.getHoraCitacion()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoInterno()));
                row.createCell(++i).setCellValue(nvl(item.getFechaVencimiento()));
                row.createCell(++i).setCellValue(nvl(item.getFolio()));
                row.createCell(++i).setCellValue(nvl(item.getClientId()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoSeguimiento()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoPostal()));

                row.createCell(++i).setCellValue(nvl(item.getIdSector()));
                row.createCell(++i).setCellValue(nvl(item.getIdCuartel()));
                row.createCell(++i).setCellValue(nvl(item.getServicio()));
                row.createCell(++i).setCellValue(nvl(item.getDestinoClasificacion()));
            }

            totalFilasGeneradas = rowNum - 1;
            validarTotalesLog(listaToNormalize.size(), totalFilasGeneradas, listaFiltrada.size());

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 🚩 LLAMADA AL MÉTODO COMÚN EXTRAÍDO
            metadatosRuta = escribirArchivoExcelEnDisco(workbook, ejecutarMerge);

        } catch (IOException e) {
            log.error("❌ Error al generar el archivo Notificación: {}", e.getMessage(), e);
        }

        if (metadatosRuta != null) {
            ejecutarMerge.setPathArchivoMerge(metadatosRuta[0]);
            ejecutarMerge.setNombreArchivoMerge(metadatosRuta[1]);
        }
        ejecutarMerge.setSizeArchivoMerge(String.valueOf(totalFilasGeneradas));
        ejecutarMerge.setTotalFilasGeneradasExcel(String.valueOf(totalFilasGeneradas));
        ejecutarMerge.setListaExcelNotificacionMerge(listaFiltrada);

        return ejecutarMerge;
    }

    private Map<String, ExcelCorreos> generarMapaSeguimiento(List<ExcelCorreos> listCsvSeguimiento) {
        return listCsvSeguimiento.stream()
                .filter(c -> c.getClientId() != null && c.getCodigo_seguimiento() != null)
                .collect(Collectors.toMap(
                        c -> normalizeClientId(c.getClientId()),
                        c -> c,
                        (existente, reemplazo) -> existente
                ));
    }

    private String[] escribirArchivoExcelEnDisco(Workbook workbook, EjecutarMerge ejecutarMerge) throws IOException {
        String nombreArchivo = ejecutarMerge.getUnidad().concat(apiProperties.getArchivoExcelNombreArchivoMergeCobranza());

        String archivoExcelMerge = apiProperties.getArchivoCreacionCarpeta()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaMerge())
                .concat(ejecutarMerge.getTipo()).concat("/")
                .concat(ejecutarMerge.getUnidad()).concat("/")
                .concat(ejecutarMerge.getBaseNombre()).concat("/")
                .concat(ejecutarMerge.getBaseNombre()).concat("_")
                .concat(nombreArchivo);

        String nombreArchivoMerge = ejecutarMerge.getBaseNombre().concat("_").concat(nombreArchivo);
        log.info("Ruta archivo calculada: {}", archivoExcelMerge);

        File archivoFinal = new File(archivoExcelMerge);
        File directorio = archivoFinal.getParentFile();

        if (directorio != null && !directorio.exists()) {
            boolean creado = directorio.mkdirs();
            if (!creado) {
                log.warn("No se pudo crear el directorio físico (puede que ya exista o no existan permisos)");
            }
        }

        try (FileOutputStream fileOut = new FileOutputStream(archivoFinal)) {
            workbook.write(fileOut);
        }
        log.info("Archivo Excel Merge generado con éxito en: {}", archivoFinal);

        return new String[]{archivoExcelMerge, nombreArchivoMerge};
    }

    private void validarTotalesLog(int tamanoOriginal, int totalFilasExcel, int tamanoFiltrado) {
        log.info("Total filas Excel generadas: {}", totalFilasExcel);
        log.info("Total lista original: {}", tamanoOriginal);
        log.info("Total validadas (Filtradas): {}", tamanoFiltrado);

        if (totalFilasExcel != tamanoOriginal) {
            log.error("❌ Diferencia detectada! Lista Original: {} vs Excel: {}", tamanoOriginal, totalFilasExcel);
        } else {
            log.info("✅ Validación OK: no se perdieron registros durante el Merge");
        }
    }

    private ResultadoValidacion validarCsvCorreos(EjecutarMerge ejecutarMerge, String tipoProceso) {
        List<ExcelCorreos> listCsvSeguimiento;

        // 1. Cargar archivo de seguimiento (Idéntico para ambos)
        try (InputStream csvInputStream = Files.newInputStream(Paths.get(ejecutarMerge.getPathCsvCorreos()))) {
            listCsvSeguimiento = ObtenerExcel.obtenerExcelCorreosCsv(csvInputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el CSV de seguimiento: " + e.getMessage(), e);
        }

        // 2. Determinar la ruta del archivo original dinámicamente según el tipo de proceso
        String pathOriginalCsv;
        if ("NOTIFICACION".equalsIgnoreCase(tipoProceso)) {
            pathOriginalCsv = ejecutarMerge.getPathArchivoNormalizadoCsv();
        } else { // cobranza por defecto
            pathOriginalCsv = ejecutarMerge.getPathArchivoNormalizado().replace(".xlsx", ".csv");
            pathOriginalCsv = pathOriginalCsv.replace(".csv", "_EnviarCorreos.csv");
        }

        // 3. Cargar archivo original
        List<ExcelCorreos> listCsvOriginal;
        try (InputStream csvInputStream = Files.newInputStream(Paths.get(pathOriginalCsv))) {
            listCsvOriginal = ObtenerExcel.obtenerExcelCorreosCsv(csvInputStream);
        } catch (Exception e) {
            throw new RuntimeException("Error al leer el CSV original: " + e.getMessage(), e);
        }

        // 4. Indexar el seguimiento en un Mapa usando la clave compuesta
        Map<String, ExcelCorreos> mapaSeguimiento = listCsvSeguimiento.stream()
                .collect(Collectors.toMap(
                        this::generarClaveUnicaCsv,
                        item -> item,
                        (a, b) -> a // Mantenemos el primero en caso de duplicados
                ));

        List<ExcelCorreos> coincidentes = new ArrayList<>();
        int contadorNoCoincidentes = 0;

        // 5. Cruzar datos buscando coincidencias
        for (ExcelCorreos original : listCsvOriginal) {
            String claveOriginal = generarClaveUnicaCsv(original);
            ExcelCorreos seguimiento = mapaSeguimiento.get(claveOriginal);

            if (seguimiento != null) {
                coincidentes.add(seguimiento);
            } else {
                contadorNoCoincidentes++;
                log.info("No coincidente: {} v/s {}", original.getClientId(), original.getToNormalize());
            }
        }

        return new ResultadoValidacion(coincidentes, contadorNoCoincidentes);
    }

    /**
     * Genera una clave normalizada para evitar fallos por mayúsculas o espacios extra.
     */
    private String generarClaveUnicaCsv(ExcelCorreos item) {
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
    protected void saveEjecutarProcesoCarta(EjecutarMerge ejecutarMerge) {

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
