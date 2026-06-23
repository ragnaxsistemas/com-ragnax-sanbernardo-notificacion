/***package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.PdfComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.ObtenerExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCargar;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaNotificacion;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.ProcesoCarta;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.ProcesoCartaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DProcesarCartaNotificacionService {

    @Autowired
    private PdfComponent plantillaService;

    @Autowired
    private ProcesoCartaRepository procesoCartaRepository;

    @Autowired
    private ApiProperties apiProperties;

    private static final String TIPO = "NOTIFICACION";
    private static final String EAN_PRUEBA = "5901234123457";

    // Inventar un correlativo que se obtenga del valor de las carpetas
    public void execute(EjecutarNotificacion ejecutarNotificacion)  {

        log.info("processRequest {} archivo {}", apiProperties.getProfile(), ejecutarNotificacion.getNombreArchivo());

        LocalDateTime ahora = LocalDateTime.now();
        // 2. Definir el formato deseado
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
        DateTimeFormatter formateadorGen = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formateadorYMD = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 3. Formatear a String
        String proceso = ahora.format(formateador);
        String procesoGen = ahora.format(formateadorGen);
        String procesoYMD = ahora.format(formateadorYMD);

        processRequest(proceso, procesoGen, procesoYMD, ejecutarNotificacion);

    }

    private void processRequest(String proceso, String procesoGen, String procesoYMD, EjecutarNotificacion ejecutarNotificacion)  {
        List<ExcelNotificacion> excelNotificaciones = new ArrayList<>();
        try{

            String array[] = new String[10];

            Path pathOrigen = null;

            Path pathDestino = null;

            /**Viene el nombre hasta el upload---/
            String dirExcel = apiProperties.getArchivoExcelNombreCarpetaNormalizada().
                    concat(ejecutarNotificacion.getTipo()).concat("/").
                    concat(ejecutarNotificacion.getUnidad()).concat("/").
                    concat(ejecutarNotificacion.getNombreArchivo());

            if(apiProperties.getProfile().equalsIgnoreCase("dev")){
                Resource resource = new ClassPathResource(dirExcel);

                InputStream inputStream = resource.getInputStream();

                //excelNotificaciones = obtenerExcel(inputStream);
                excelNotificaciones = ObtenerExcel.obtenerExcelNotificacion(inputStream, apiProperties.getArchivoExcelNombreHojaNotificacion());
                pathOrigen = resource.getFile().toPath();

                array = ejecutarNotificacion.getNombreArchivo().split("/");

                pathDestino = Paths.get(apiProperties.getArchivoCreacionRespaldoNormalizado()
                        .concat("/").concat(ejecutarNotificacion.getTipo())
                        .concat("/").concat(ejecutarNotificacion.getUnidad())
                        .concat("/").concat(proceso)
                        .concat("/").concat(array[1]));
            }

            if(apiProperties.getProfile().equalsIgnoreCase("qa") || apiProperties.getProfile().equalsIgnoreCase("prod")) {

                log.info("processRequest Execution {}", apiProperties.getProfile());

                pathOrigen = Paths.get(dirExcel);

                if (!Files.exists(pathOrigen)) {
                    throw new RuntimeException("El archivo no existe: " + dirExcel);
                }
                try (InputStream inputStream = new FileInputStream(pathOrigen.toFile())) {
                    // Aquí abres el workbook con Apache POI
                    excelNotificaciones = ObtenerExcel.obtenerExcelNotificacion(inputStream, apiProperties.getArchivoExcelNombreHojaNotificacion());
                }
                array = ejecutarNotificacion.getNombreArchivo().split("/");

                pathDestino = Paths.get(apiProperties.getArchivoCreacionRespaldoNormalizado()
                        .concat("/").concat(ejecutarNotificacion.getTipo())
                        .concat("/").concat(ejecutarNotificacion.getUnidad())
                        .concat("/").concat(proceso)
                        .concat("/").concat(array[1]));

            }
            validar(excelNotificaciones, ejecutarNotificacion.getUnidad(), ejecutarNotificacion.getValor()); //1 o 2

            log.info("pathOrigen Execution {}", pathOrigen);

            log.info("pathDestino Execution {}", pathDestino);

            Files.createDirectories(pathDestino.getParent());

            Files.copy(pathOrigen, pathDestino, StandardCopyOption.REPLACE_EXISTING);
            //Cambiar el Excel de Carpeta, ya ha sido leido, eliminarlo de la carpeta
            mapaRutPatenteFila(ejecutarNotificacion, pathOrigen.toString(), pathDestino.toString(),
                    proceso, procesoGen, procesoYMD,  excelNotificaciones);

        }catch(Exception e){
            log.error("Exception error", e);
        }
    }

    /***private List<ExcelNotificacion>  obtenerExcel(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);

        Sheet sheet = workbook.getSheet(apiProperties.getArchivoExcelNombreHojaNotificacion()); // nombre de la hoja

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
                    formatter.formatCellValue(row.getCell(14)),
                    formatter.formatCellValue(row.getCell(15)),
                    formatter.formatCellValue(row.getCell(16))
            ));
        }
        /***Cerrar EXCEL---/
        workbook.close();

        return excelNotificaciones;
    }---/


    public boolean validar(List<ExcelNotificacion> excelNotificaciones, String unidad, String valor){
        Map<String, List<ExcelNotificacion>> mapaPorJuzgado = excelNotificaciones.stream()
                .collect(Collectors.groupingBy(ExcelNotificacion::getJuzgado));

        for (String juzgado : mapaPorJuzgado.keySet()) {
            if(juzgado.trim().contains(valor) && unidad.trim().contains(valor))
                return true;
        }
        return false;
    }

    //debe venir declarado o el ultimo se guarda en vacio.
    private void mapaRutPatenteFila(EjecutarNotificacion ejecutarNotificacion,String pathOrigen, String pathDestino,
                                    String proceso, String procesoGen, String procesoYMD,
                                    List<ExcelNotificacion> excelNotificaciones) throws Exception {

        Map<String, Map<String, List<ExcelNotificacion>>> mapaRutPatenteIndividual = new HashMap<>();
        Map<String, Map<String, List<ExcelNotificacion>>> mapaRutPatenteMasiva = new HashMap<>();

        Map<String, Map<String, List<ExcelNotificacion>>> mapaLargoRutPatente =
                excelNotificaciones.stream()
                        .collect(Collectors.groupingBy(
                                ExcelNotificacion::getRut,
                                Collectors.groupingBy(ExcelNotificacion::getPlacaPatente)
                        ));

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

        //Correlativo historico debe ser obtenido al comienzo
        GeneracionCarta generacionCarta = ejecucionListaMapaRutPatente(
                ejecutarNotificacion,
                pathOrigen, pathDestino,
                proceso,
                procesoYMD,
                mapaRutPatenteIndividual, mapaRutPatenteMasiva);

        plantillaService.unirPdfs(
                apiProperties.getArchivoCreacionCarpeta()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacion())
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacionConsolidado())
                        .concat(ejecutarNotificacion.getUnidad()).concat("/")
                        .concat(proceso).concat("/")
                        .concat(proceso).concat("_")
                        .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado()),
                generacionCarta.getListaPdfs());

        /***Generacion del Reporte---/
        generarReporte( procesoGen,  proceso, ejecutarNotificacion, excelNotificaciones,  generacionCarta.getContFolioProceso());
    }


    private GeneracionCarta ejecucionListaMapaRutPatente(EjecutarNotificacion ejecutarNotificacion,
                                                         String pathOrigen,
                                                         String pathDestino,
                                                         String proceso,
                                                         String procesoYMD,
                                                         Map<String, Map<String, List<ExcelNotificacion>>> mapaRutPatenteIndividual,
                                                         Map<String, Map<String, List<ExcelNotificacion>>> mapaRutPatenteMasiva) throws Exception {

        long correlativo = obtenerCorrelativo();

        GeneracionCarta generacionCarta = new  GeneracionCarta(ejecutarNotificacion.getUnidad(), "", 1, new ArrayList<>(), correlativo);

        generacionCarta = ejecutarMapaRutPatente(proceso, procesoYMD, generacionCarta, mapaRutPatenteIndividual);

        generacionCarta = ejecutarMapaRutPatente(proceso, procesoYMD, generacionCarta, mapaRutPatenteMasiva);

        procesoCartaRepository.save(
                new ProcesoCarta
                        (LocalDateTime.now(), correlativo, generacionCarta.getCorrelativoHistorico(), ejecutarNotificacion.getUsuario(),
                                pathOrigen,  pathDestino, proceso, TIPO));

        return generacionCarta;
    }


    private GeneracionCarta ejecutarMapaRutPatente(String proceso, String procesoYMD,
                                                   GeneracionCarta generacionCarta,
                                                   Map<String, Map<String, List<ExcelNotificacion>>> mapaRutPatente) throws Exception {

        // Extraemos valores iniciales del objeto de estado
        long correlativoHistorico = generacionCarta.getCorrelativoHistorico();
        int contFolioProceso = generacionCarta.getContFolioProceso();
        List<byte[]> listaPdfs = generacionCarta.getListaPdfs();
        String folioImpresoActual = generacionCarta.getProcesoGeneracionCarta();

        // Rutas base
        String pathBaseIndividual =
                apiProperties.getArchivoCreacionCarpeta()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacion())
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacionDocumento())
                        .concat(generacionCarta.getUnidad()).concat("/")
                        .concat(proceso).concat("/");

        CartaHtml cartaHtmlIndividual =  generarCartaHtmlIndividual(generacionCarta.getUnidad());

        CartaHtml cartaHtmlMasiva =  generarCartaHtmlMasiva(generacionCarta.getUnidad());

        /*** Buscar Caso Particular ***/
        /***
        for (Map.Entry<String, Map<String, List<ExcelNotificacion>>> entryRut : mapaRutPatente.entrySet()) {
            Map<String, List<ExcelNotificacion>> mapaPatentes = entryRut.getValue();
            for (Map.Entry<String, List<ExcelNotificacion>> entryPatente : mapaPatentes.entrySet()) {
                List<ExcelNotificacion> listaExcelNotificacion = entryPatente.getValue();
                if(listaExcelNotificacion.size()>4){
                    log.info("rut {} patente {} largo {}", entryRut.getKey() ,entryPatente.getKey(), listaExcelNotificacion.size());
                    if(entryRut.getKey().toString().equalsIgnoreCase("00187021006")  && entryPatente.getKey().toString().equalsIgnoreCase("FSYR48")){
                        log.info("rut {} patente {} largo {}", entryRut.getKey() ,entryPatente.getKey(), listaExcelNotificacion.size());
                    }
                }
            }
        }---/

        for (Map.Entry<String, Map<String, List<ExcelNotificacion>>> entryRut : mapaRutPatente.entrySet()) {
            Map<String, List<ExcelNotificacion>> mapaPatentes = entryRut.getValue();

            for (Map.Entry<String, List<ExcelNotificacion>> entryPatente : mapaPatentes.entrySet()) {

                //*** Buscar Caso Particular ***/
                /***
                if(entryRut.getKey().toString().equalsIgnoreCase("00166120578")  && entryPatente.getKey().toString().equalsIgnoreCase("FLKC57")){
                ---/
                    List<ExcelNotificacion> listaExcelNotificacion = entryPatente.getValue();
                    // Validar que el rut no esté vacío
                    if (listaExcelNotificacion != null && !listaExcelNotificacion.isEmpty() && !listaExcelNotificacion.get(0).getRut().isBlank()) {

                        // Agrupamos en bloques de 7 para la grilla
                        List<List<ExcelNotificacion>> bloques = new ArrayList<>();
                        for (int i = 0; i < listaExcelNotificacion.size(); i += 7) {
                            bloques.add(listaExcelNotificacion.subList(i, Math.min(i + 7, listaExcelNotificacion.size())));
                        }

                        for (List<ExcelNotificacion> bloqueActual : bloques) {
                            ExcelNotificacion excelNotificacionPrimero = bloqueActual.get(0);
                            folioImpresoActual = procesoYMD.concat(String.valueOf(contFolioProceso));

                            byte[] pdfGenerado;

                            /***INDIVIDUAL---/
                            if (listaExcelNotificacion.size() == 1) {
                                String htmlIndividual = PlantillaNotificacion.generarPlantillaNotificacionIndividual(
                                        procesoYMD.concat("-").concat(String.valueOf(correlativoHistorico)),
                                        cartaHtmlIndividual,
                                        excelNotificacionPrimero);

                                // Aquí usamos el método de iText 7 con el código de barras
                                //pdfGenerado = plantillaService.generarPdffromHtmlNotificacionConBarcode(htmlIndividual,
                                //        excelNotificacionPrimero.getFolio(), EAN_PRUEBA);
                                //pdfGenerado = plantillaService.generarPdffromHtmlNotificacionConBarcode(htmlIndividual,
                                //        "", EAN_PRUEBA);
                                pdfGenerado = plantillaService.
                                        generarPdffromHtmlCodeEanV2(htmlIndividual, EAN_PRUEBA);

                                log.info("Procesado Folio: {} | creado Archivo: {} ", contFolioProceso, excelNotificacionPrimero.getFolio());
                            } else {
                                String htmlMasivo = PlantillaNotificacion.generarPlantillaNotificacionMasiva(
                                        procesoYMD.concat("-").concat(String.valueOf(correlativoHistorico)),
                                        cartaHtmlMasiva,
                                        excelNotificacionPrimero,
                                        bloqueActual);

                                pdfGenerado = plantillaService.generarPdffromHtmlCodeEanV2(htmlMasivo, EAN_PRUEBA);
                            }

                            String nombreArchivo = String.valueOf(correlativoHistorico).concat("_").concat(String.format("%d-%s.pdf",
                                    contFolioProceso, excelNotificacionPrimero.getRut()));

                            //tipo cobranza - notificacion - procesados - upload
                            //carta documento - reporte - consolidado / cobranza - notificacion
                            //unidad 1juzgado 2juzgado tesoreria
                            //proceso 2026_01_01_01_01_01
                            //archivo xxxx.pdf
                            String archivo = plantillaService.guardarPdfIndividual(pdfGenerado, pathBaseIndividual, nombreArchivo);

                            log.info("Procesado Correlativo: {} | Folio: {} | creado Archivo: {} ", correlativoHistorico, contFolioProceso, archivo);

                            // Actualización de contadores y lista
                            listaPdfs.add(pdfGenerado);
                            contFolioProceso++;
                            correlativoHistorico++;
                            break;
                        }
                    }
                    break;
                /***}---/
            }
            break;
        }
        // Retornamos el nuevo estado
        return new GeneracionCarta(generacionCarta.getUnidad(), folioImpresoActual, contFolioProceso, listaPdfs, correlativoHistorico);
    }


    private void generarReporte(String procesoGen, String proceso,
                                EjecutarNotificacion ejecutarNotificacion,
                                List<ExcelNotificacion> excelNotificaciones, Integer numFolio) throws Exception {

        log.info("generarReporte procesados: {} nombre: {}", numFolio,
                apiProperties.getArchivoCreacionCarpeta()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacion())
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacionReporte())
                        .concat(ejecutarNotificacion.getUnidad()).concat("/")
                        .concat(proceso).concat("/")
                        .concat(proceso.concat("_").concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte())));


        Map<String, List<ExcelNotificacion>> mapaRut =
                excelNotificaciones.stream()
                        .collect(Collectors.groupingBy(ExcelNotificacion::getRut));

        Map<String, List<ExcelNotificacion>> mapaPatente =
                excelNotificaciones.stream()
                        .collect(Collectors.groupingBy(ExcelNotificacion::getPlacaPatente));

        Map<String, List<ExcelNotificacion>> mapaTipoVehiculo =
                excelNotificaciones.stream()
                        .collect(Collectors.groupingBy(ExcelNotificacion::getTipoVehiculo));

        Reporte reporte = new Reporte(procesoGen, proceso,  String.valueOf(numFolio-1),  String.valueOf(mapaRut.size()),
                String.valueOf(mapaPatente.size()), String.valueOf(mapaTipoVehiculo.size()));

        String html = PlantillaNotificacion.generarPlantillaReporteNotificacion (
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlNombreArchivoReporte()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogo()),
                reporte);

        byte[] pdf = plantillaService.generarPdffromHtml(html);

        //Crear Archivo Reporte
        //tipo cobranza - notificacion - procesados - upload
        //carta documento - reporte - consolidado / cobranza - notificacion
        //unidad 1juzgado 2juzgado tesoreria
        //proceso 2026_01_01_01_01_01
        //archivo xxxx.pdf
        plantillaService.guardarPdfIndividual(pdf,
                apiProperties.getArchivoCreacionCarpeta()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacion())
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaNotificacionReporte())
                        .concat(ejecutarNotificacion.getUnidad()).concat("/")
                        .concat(proceso).concat("/") ,
                proceso.concat("_").concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()));

    }

    private CartaHtml generarCartaHtmlIndividual(String unidad) throws Exception {

        String firma = (unidad.equalsIgnoreCase("1juzgado")) ?
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(apiProperties.getArchivoHtmlLogoFirma1Juzgado()) : null;

        if(firma == null)
            firma = (unidad.equalsIgnoreCase("2juzgado")) ?
                    apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(apiProperties.getArchivoHtmlLogoFirma2Juzgado()) : null;
        // Firma y Logo
        List<String> logos = Arrays.asList(
                firma,
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoBciCupon())
        );


        ClassPathResource imgFileFirma = new ClassPathResource(logos.get(0)); //firma
        ClassPathResource imgFileBci = new ClassPathResource(logos.get(1)); //bci

        byte[] imageBytesFirma;
        try (InputStream is = imgFileFirma.getInputStream()) {
            imageBytesFirma = is.readAllBytes();
        }

        byte[] imageBytesBci;
        try (InputStream is = imgFileBci.getInputStream()) {
            imageBytesBci = is.readAllBytes();
        }

        String base64Firma = Base64.getEncoder().encodeToString(imageBytesFirma);
        String base64Bci = Base64.getEncoder().encodeToString(imageBytesBci);

        /***Cargar una vez el String de html---/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                apiProperties.getArchivoHtmlNombreArchivoNotificacionIndividual()));

        return new CartaHtml(htmlIndividual, Arrays.asList(base64Firma, base64Bci));
    }

    private CartaHtml generarCartaHtmlMasiva(String unidad) throws Exception {

        String firma = (unidad.equalsIgnoreCase("1juzgado")) ?
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(apiProperties.getArchivoHtmlLogoFirma1Juzgado()) : null;

        if(firma == null)
            firma = (unidad.equalsIgnoreCase("2juzgado")) ?
                    apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(apiProperties.getArchivoHtmlLogoFirma2Juzgado()) : null;

        /***List<String> logos = Arrays.asList(
         firma);---/

        ClassPathResource imgFileFirma = new ClassPathResource(firma); //firma

        byte[] imageBytesFirma;
        try (InputStream is = imgFileFirma.getInputStream()) {
            imageBytesFirma = is.readAllBytes();
        }

        String base64Firma = Base64.getEncoder().encodeToString(imageBytesFirma);

        /***Cargar una vez el String de html---/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                apiProperties.getArchivoHtmlNombreArchivoNotificacionMasiva()));

        return new CartaHtml(htmlIndividual, Arrays.asList(base64Firma));
    }

    public long obtenerCorrelativo() {

        Optional<ProcesoCarta> optProcesoNotificacionCarta =
                procesoCartaRepository.findTopByTipoCartaOrderByFechaRegistroDesc(TIPO);

        if(optProcesoNotificacionCarta.isPresent()) {
            return optProcesoNotificacionCarta.get().getUltimoCorrelativo();
        }
        return 1;
    }
}***/
