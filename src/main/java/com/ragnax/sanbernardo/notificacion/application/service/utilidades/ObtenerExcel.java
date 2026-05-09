package com.ragnax.sanbernardo.notificacion.application.service.utilidades;

import com.opencsv.bean.CsvToBeanBuilder;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ObtenerExcel {

    public static List<ExcelCobranza> obtenerExcelCobranza(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);
        //Sheet sheet = workbook.getSheet(apiProperties.getArchivoExcelNombreHojaCobranza()); // nombre de la hoja

        DataFormatter formatter = new DataFormatter();

        List<ExcelCobranza> excelCobranzas = new ArrayList<>();
        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // saltar header

            excelCobranzas.add(new
                    ExcelCobranza(formatter.formatCellValue(row.getCell(0)),
                    formatter.formatCellValue(row.getCell(1)),
                    formatter.formatCellValue(row.getCell(2)),
                    formatter.formatCellValue(row.getCell(3)),
                    formatter.formatCellValue(row.getCell(4)),
                    formatter.formatCellValue(row.getCell(5)),
                    formatter.formatCellValue(row.getCell(6)),
                    formatter.formatCellValue(row.getCell(7)),
                    formatter.formatCellValue(row.getCell(8)),
                    formatter.formatCellValue(row.getCell(9)),
                    formatter.formatCellValue(row.getCell(10)),
                    formatter.formatCellValue(row.getCell(11)),
                    formatter.formatCellValue(row.getCell(12)),
                    formatter.formatCellValue(row.getCell(13)),
                    formatter.formatCellValue(row.getCell(14)),
                    formatter.formatCellValue(row.getCell(15)),
                    formatter.formatCellValue(row.getCell(16)),
                    formatter.formatCellValue(row.getCell(17)),
                    formatter.formatCellValue(row.getCell(18)),
                    formatter.formatCellValue(row.getCell(19)),
                    formatter.formatCellValue(row.getCell(20)),
                    formatter.formatCellValue(row.getCell(21)),
                    formatter.formatCellValue(row.getCell(22)),
                    formatter.formatCellValue(row.getCell(23)),
                    formatter.formatCellValue(row.getCell(24)),
                    formatter.formatCellValue(row.getCell(25))
            ));
        }
        /***Cerrar EXCEL*/
        workbook.close();

        return excelCobranzas;
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

    /***public static List<ExcelCobranzaCorreos> obtenerExcelCorreosCsv(InputStream inputStream) throws Exception {
     List<ExcelCobranzaCorreos> beans = null;
     // Usar el Encoding correcto es vital para que los nombres de cabecera hagan match
     try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

     beans = new CsvToBeanBuilder<ExcelCobranzaCorreos>(reader)
     .withType(ExcelCobranzaCorreos.class)
     .withSeparator(';') // <--- CAMBIA ESTO según tu archivo (',' o ';')
     .withIgnoreLeadingWhiteSpace(true)
     .withVerifyReader(false) // 👈 importante
     .withThrowExceptions(false) // 👈 evita que se corte
     .withExceptionHandler(exception -> {
     System.err.println("Error en línea " + exception.getLineNumber() + ""+ exception.getMessage());
     return null;
     })
     .build()
     .parse();

     // Filtrar nulos si el ExceptionHandler devolvió alguno
     return beans.stream().filter(Objects::nonNull).collect(Collectors.toList());
     }
     }***/

    public static List<ExcelCobranzaToNormalize> obtenerExcelCobranzaNormalizado(InputStream inputStream, String hoja) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(hoja);
        //Sheet sheet = workbook.getSheet(apiProperties.getArchivoExcelNombreHojaCobranza()); // nombre de la hoja

        DataFormatter formatter = new DataFormatter();

        List<ExcelCobranzaToNormalize> excelCobranzasToNormalize = new ArrayList<>();
        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue; // saltar header


            excelCobranzasToNormalize.add(new
                    ExcelCobranzaToNormalize(formatter.formatCellValue(row.getCell(0)),
                    formatter.formatCellValue(row.getCell(1)),
                    formatter.formatCellValue(row.getCell(2)),
                    formatter.formatCellValue(row.getCell(3)),
                    formatter.formatCellValue(row.getCell(4)),
                    formatter.formatCellValue(row.getCell(5)),
                    formatter.formatCellValue(row.getCell(6)),
                    formatter.formatCellValue(row.getCell(7)),
                    formatter.formatCellValue(row.getCell(8)),
                    formatter.formatCellValue(row.getCell(9)),
                    formatter.formatCellValue(row.getCell(10)),
                    formatter.formatCellValue(row.getCell(11)),
                    formatter.formatCellValue(row.getCell(12)),
                    formatter.formatCellValue(row.getCell(13)),
                    formatter.formatCellValue(row.getCell(14)),
                    formatter.formatCellValue(row.getCell(15)),
                    formatter.formatCellValue(row.getCell(16)),
                    formatter.formatCellValue(row.getCell(17)),
                    formatter.formatCellValue(row.getCell(18)),
                    formatter.formatCellValue(row.getCell(19)),
                    formatter.formatCellValue(row.getCell(20)),
                    formatter.formatCellValue(row.getCell(21)),
                    formatter.formatCellValue(row.getCell(22)),
                    formatter.formatCellValue(row.getCell(23)),
                    formatter.formatCellValue(row.getCell(24)),
                    formatter.formatCellValue(row.getCell(25)),
                    formatter.formatCellValue(row.getCell(26)),
                    formatter.formatCellValue(row.getCell(27))
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

        List<ExcelCobranzaMerge> ExcelCobranzaMerges = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // saltar header

            // Creamos el objeto pasando los 29 parámetros que pide tu constructor
            ExcelCobranzaMerges.add(new ExcelCobranzaMerge(
                    getVal(row, 0, formatter),  // cert1
                    getVal(row, 1, formatter),  // cert2
                    getVal(row, 2, formatter),  // fechaCarta
                    getVal(row, 3, formatter),  // vence
                    getVal(row, 4, formatter),  // folio
                    getVal(row, 5, formatter),  // apellidoPaterno
                    getVal(row, 6, formatter),  // apellidoMaterno
                    getVal(row, 7, formatter),  // nombres
                    getVal(row, 8, formatter),  // rut
                    getVal(row, 9, formatter),  // dv
                    getVal(row, 10, formatter), // direccion
                    getVal(row, 11, formatter), // comuna
                    getVal(row, 12, formatter), // placaPatente
                    getVal(row, 13, formatter), // dg
                    getVal(row, 14, formatter), // tipoVehiculo
                    getVal(row, 15, formatter), // rolMop
                    getVal(row, 16, formatter), // fechaInfraccion
                    getVal(row, 17, formatter), // horaInfraccion
                    getVal(row, 18, formatter), // convenio1
                    getVal(row, 19, formatter), // convenio2
                    getVal(row, 20, formatter), // codigoBarra
                    getVal(row, 21, formatter), // valorMulta
                    getVal(row, 22, formatter), // lugarMulta
                    getVal(row, 23, formatter), // fechaCitacion
                    getVal(row, 24, formatter), // juzgado
                    getVal(row, 25, formatter), // piso
                    getVal(row, 26, formatter), // clientId
                    getVal(row, 27, formatter), // codigoSeguimiento toNormalize
                    getVal(row, 27, formatter)        // codigoSeguimiento (inicialmente vacío)
            ));
        }
        workbook.close();

        return ExcelCobranzaMerges;
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

            excelNotificaciones.add(new
                    ExcelNotificacion(formatter.formatCellValue(row.getCell(0)),
                    formatter.formatCellValue(row.getCell(1)),
                    formatter.formatCellValue(row.getCell(2)),
                    formatter.formatCellValue(row.getCell(3)),
                    formatter.formatCellValue(row.getCell(4)),
                    formatter.formatCellValue(row.getCell(5)),
                    formatter.formatCellValue(row.getCell(6)),
                    formatter.formatCellValue(row.getCell(7)),
                    formatter.formatCellValue(row.getCell(8)),
                    formatter.formatCellValue(row.getCell(9)),
                    formatter.formatCellValue(row.getCell(10)),
                    formatter.formatCellValue(row.getCell(11)),
                    formatter.formatCellValue(row.getCell(12)),
                    formatter.formatCellValue(row.getCell(13)),
                    formatter.formatCellValue(row.getCell(14))
            ));
        }
        /***Cerrar EXCEL*/
        workbook.close();

        return excelNotificaciones;
    }
}