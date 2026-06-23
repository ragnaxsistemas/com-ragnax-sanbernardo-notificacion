package com.ragnax.sanbernardo.notificacion.application.service.utilidades;

import com.opencsv.bean.CsvToBeanBuilder;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

public class ObtenerExcel {

    public static List<ExcelCobranza> obtenerExcelCobranza(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);
        //Sheet sheet = workbook.getSheet(apiProperties.getArchivoExcelNombreHojaCobranza()); // nombre de la hoja

        DataFormatter formatter = new DataFormatter();

        DateTimeFormatter entrada = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.DAY_OF_MONTH)
                .appendLiteral('/')
                .appendValue(ChronoField.MONTH_OF_YEAR)
                .appendLiteral('/')
                .appendValue(ChronoField.YEAR, 4)
                .toFormatter();

        List<ExcelCobranza> excelCobranzas = new ArrayList<>();
        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // saltar header

            int i=0;

            excelCobranzas.add(new
                    ExcelCobranza(formatter.formatCellValue(row.getCell(i)),
                    formatter.formatCellValue(row.getCell(++i)),

                    obtenerFecha(row.getCell(++i), formatter),
                    obtenerFecha(row.getCell(++i), formatter),

                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),

                    obtenerFecha(row.getCell(++i), formatter),

                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),

                    obtenerFecha(row.getCell(++i), formatter),  //fecha Citacion

                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i))
            ));
        }
        /***Cerrar EXCEL*/
        workbook.close();

        return excelCobranzas;
    }

    private static final DateTimeFormatter SALIDA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String obtenerFecha(Cell cell, DataFormatter formatter) {

        if (cell == null) {
            return "";
        }

        // Fecha real de Excel
        if (cell.getCellType() == CellType.NUMERIC &&
                DateUtil.isCellDateFormatted(cell)) {

            return cell.getLocalDateTimeCellValue()
                    .toLocalDate()
                    .format(SALIDA);
        }

        // Texto
        String valor = formatter.formatCellValue(cell).trim();

        if (valor.isEmpty()) {
            return "";
        }

        // dd/MM/yyyy o d/M/yyyy
        try {
            return LocalDate.parse(
                    valor,
                    DateTimeFormatter.ofPattern("d/M/yyyy")
            ).format(SALIDA);
        } catch (Exception ignored) {
        }

        // M/d/yy (ej: 8/19/26)
        try {
            return LocalDate.parse(
                    valor,
                    DateTimeFormatter.ofPattern("M/d/yy")
            ).format(SALIDA);
        } catch (Exception ignored) {
        }

        // dd-MMM-yyyy (ej: 19-ago.-2026)
        try {
            Locale es = new Locale("es", "CL");

            return LocalDate.parse(
                    valor.replace(".", ""),
                    DateTimeFormatter.ofPattern("d-MMM-yyyy", es)
            ).format(SALIDA);
        } catch (Exception ignored) {
        }

        throw new IllegalArgumentException(
                "Formato de fecha no soportado: " + valor
        );
    }

    public static List<ExcelCorreos> obtenerExcelCorreosCsv(InputStream inputStream) throws Exception {

        List<ExcelCorreos> result;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            List<String> cleanedLines = new ArrayList<>();

            String headerLine = br.readLine();
            if (headerLine == null) return Collections.emptyList();

            int expectedColumns = headerLine.split(";").length;
            cleanedLines.add(headerLine);

            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                lineNumber++;

                String[] parts = line.split(";", -1); // -1 mantiene vacíos

                if (parts.length > expectedColumns) {
                    // 👇 recorta columnas extra
                    parts = Arrays.copyOf(parts, expectedColumns);
                }

                // reconstruir línea limpia
                String cleaned = String.join(";", parts);
                cleanedLines.add(cleaned);
            }

            // 👇 ahora sí parseas limpio
            Reader cleanedReader = new StringReader(String.join("\n", cleanedLines));

            result = new CsvToBeanBuilder<ExcelCorreos>(cleanedReader)
                    .withType(ExcelCorreos.class)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            return result;
        }
    }


    public static List<ExcelCobranzaToNormalize> obtenerExcelCobranzaNormalizado(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);
        //Sheet sheet = workbook.getSheet(apiProperties.getArchivoExcelNombreHojaCobranza()); // nombre de la hoja

        DataFormatter formatter = new DataFormatter();

        List<ExcelCobranzaToNormalize> excelCobranzasToNormalize = new ArrayList<>();
        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // saltar header

            int i=0;

            excelCobranzasToNormalize.add(new
                    ExcelCobranzaToNormalize(formatter.formatCellValue(row.getCell(i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i))
            ));
        }
        /***Cerrar EXCEL*/
        workbook.close();

        return excelCobranzasToNormalize;
    }

    public static List<ExcelCobranzaMerge> obtenerExcelCobranzaMerge(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);

        DataFormatter formatter = new DataFormatter();

        List<ExcelCobranzaMerge> excelCobranzaMerges = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // saltar header
            int i=0;
            // Creamos el objeto pasando los 29 parámetros que pide tu constructor
            excelCobranzaMerges.add(new ExcelCobranzaMerge(
                    getVal(row, i, formatter),  // cert1
                    getVal(row, ++i, formatter),  // cert2
                    getVal(row, ++i, formatter),  // fechaCarta
                    getVal(row, ++i, formatter),  // vence
                    getVal(row, ++i, formatter),  // folio
                    getVal(row, ++i, formatter),  // apellidoPaterno
                    getVal(row, ++i, formatter),  // apellidoMaterno
                    getVal(row, ++i, formatter),  // nombres
                    getVal(row, ++i, formatter),  // rut
                    getVal(row, ++i, formatter),  // dv
                    getVal(row, ++i, formatter), // direccion
                    getVal(row, ++i, formatter), // comuna
                    getVal(row, ++i, formatter), // placaPatente
                    getVal(row, ++i, formatter), // dg
                    getVal(row, ++i, formatter), // tipoVehiculo
                    getVal(row, ++i, formatter), // rolMop
                    getVal(row, ++i, formatter), // fechaInfraccion
                    getVal(row, ++i, formatter), // horaInfraccion
                    getVal(row, ++i, formatter), // convenio1
                    getVal(row, ++i, formatter), // convenio2
                    getVal(row, ++i, formatter), // codigoBarra
                    getVal(row, ++i, formatter), // valorMulta
                    getVal(row, ++i, formatter), // lugarMulta
                    getVal(row, ++i, formatter), // fechaCitacion
                    getVal(row, ++i, formatter), // juzgado
                    getVal(row, ++i, formatter), // piso
                    getVal(row, ++i, formatter), // clientId
                    "",  //toNormalize
                    getVal(row, ++i, formatter),// codigoSeguimiento
                    getVal(row, ++i, formatter),        // codigoSeguimiento (inicialmente vacío)
                    getVal(row, ++i, formatter), //idSector
                    getVal(row, ++i, formatter), //idCuartel
                    getVal(row, ++i, formatter), //servicio
                    getVal(row, ++i, formatter)  //destino_clasificacion
                    //codigo_postal;id_sector;id_cuartel;id_cliente;servicio;orden_de_impresion;destino_clasificacion

            ));
        }
        workbook.close();

        return excelCobranzaMerges;
    }

    private static String getVal(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index);
        return (cell == null) ? "" : formatter.formatCellValue(cell);
    }

    public static List<ExcelNotificacion>  obtenerExcelNotificacion(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja); // nombre de la hoja

        DataFormatter formatter = new DataFormatter();

        List<ExcelNotificacion> excelNotificaciones = new ArrayList<>();
        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // saltar header
            int i=0;
            excelNotificaciones.add(new
                    ExcelNotificacion(
                        formatter.formatCellValue(row.getCell(i)), //row.getCell(0)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(1)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(2)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(3)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(4)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(5)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(6)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(7)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(8)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(9)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(10)), //F.Infraccion
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(11)), //
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(12)), //F.Citación
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(13)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(14)),
                        formatter.formatCellValue(row.getCell(++i)), //row.getCell(15)), //F.Vencimiento
                        formatter.formatCellValue(row.getCell(++i)) //row.getCell(16))
            ));
        }
        /***Cerrar EXCEL*/
        workbook.close();

        return excelNotificaciones;
    }

    public static List<ExcelNotificacionToNormalize> obtenerExcelNotificacionNormalizado(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);
        //Sheet sheet = workbook.getSheet(apiProperties.getArchivoExcelNombreHojaCobranza()); // nombre de la hoja

        DataFormatter formatter = new DataFormatter();

        List<ExcelNotificacionToNormalize> excelNotificacionesToNormalize = new ArrayList<>();
        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // saltar header

            int i=0;
            excelNotificacionesToNormalize.add(new
                    ExcelNotificacionToNormalize(
                    formatter.formatCellValue(row.getCell(i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i)),
                    formatter.formatCellValue(row.getCell(++i))));
        }
        /***Cerrar EXCEL*/
        workbook.close();

        return excelNotificacionesToNormalize;
    }

    public static List<ExcelNotificacionMerge> obtenerExcelNotificacionMerge(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);

        DataFormatter formatter = new DataFormatter();

        List<ExcelNotificacionMerge> excelNotificacionesMerge = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // saltar header

            int i=0;

            // Creamos el objeto pasando los 29 parámetros que pide tu constructor
            excelNotificacionesMerge.add(new ExcelNotificacionMerge(
                    getVal(row, i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    "",
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter),
                    getVal(row, ++i, formatter), //idSector
                    getVal(row, ++i, formatter), //idCuartel
                    getVal(row, ++i, formatter), //servicio
                    getVal(row, ++i, formatter)
            ));
        }
        workbook.close();

        return excelNotificacionesMerge;
    }
}