package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.PdfComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.CartaHtml;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.ExcelCobranzaNormalizado;
import com.ragnax.sanbernardo.notificacion.application.service.model.GeneracionCarta;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCargar;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCobranzas;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.ProcesoCarta;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.ProcesoCartaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DProcesarCartaCobranzaService {

    @Autowired
    private PdfComponent plantillaService;

    @Autowired
    private ProcesoCartaRepository procesoCartaRepository;

    @Autowired
    private ApiProperties apiProperties;

    private static final String TIPO = "COBRANZA";

    public EjecutarMerge processArchivoCobranza(//String proceso, String procesoGen, String procesoYMD,
                                        EjecutarMerge ejecutarMerge)  {
        List<ExcelCobranzaNormalizado> excelCobranzasMerge = ejecutarMerge.getListaExcelCobranzaNormalizado();

        try{
            log.info("excelCobranzasMerge", ejecutarMerge.getListaExcelCobranzaNormalizado());
            //Files.copy(pathOrigen, pathDestino, StandardCopyOption.REPLACE_EXISTING);
            //Cambiar el Excel de Carpeta, ya ha sido leido, eliminarlo de la carpeta

            ejecutarMerge = mapaRutPatenteFila(ejecutarMerge,
                    excelCobranzasMerge);

        }catch(Exception e){
            log.error("Exception error", e);
        }
        return ejecutarMerge;
    }

    //debe venir declarado o el ultimo se guarda en vacio.
    private EjecutarMerge mapaRutPatenteFila(EjecutarMerge ejecutarMerge,
                                    List<ExcelCobranzaNormalizado> excelCobranzas) throws Exception {

        Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapaRutPatenteIndividual = new HashMap<>();

        Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapaRutPatenteMasiva = new HashMap<>();

        Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapaLargoRutPatente =
                excelCobranzas.stream()
                        .filter(e -> e.getRut() != null && !e.getRut().isBlank()) // Filtra ruts nulos o vacíos
                        .collect(Collectors.groupingBy(
                                ExcelCobranzaNormalizado::getRut,
                                Collectors.groupingBy(e -> e.getPlacaPatente() == null ? "SIN_PATENTE" : e.getPlacaPatente())
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
        ejecutarMerge = ejecucionListaMapaRutPatente(
                ejecutarMerge,
                mapaRutPatenteIndividual, mapaRutPatenteMasiva);
        //6_cobranza/tesoreria/CD-FTX_2026_04_27_11_57_14/CARTAS
        plantillaService.unirPdfs(
                ejecutarMerge.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaConsolidado())
                        .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado()),
                ejecutarMerge.getListaPdfs());

        procesoCartaRepository.save(
                new ProcesoCarta(LocalDateTime.now(), ejecutarMerge.getCorrelativoInicio(),
                        ejecutarMerge.getCorrelativoHistorico(),
                        ejecutarMerge.getUsuarioMerge(),
                        ejecutarMerge.getPathArchivoUpload(),
                        ejecutarMerge.getPathArchivoMerge(),
                        ejecutarMerge.getBaseNombre(),
                        ejecutarMerge.getTipo()));

        /***Generacion del Reporte*/
        ejecutarMerge = generarReporte(ejecutarMerge,  excelCobranzas);

        return ejecutarMerge;
    }


    private EjecutarMerge ejecucionListaMapaRutPatente(EjecutarMerge ejecutarMerge,
                                                         Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapaRutPatenteIndividual,
                                                         Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapaRutPatenteMasiva) throws Exception {

        long correlativo = obtenerCorrelativo();
        ejecutarMerge.setCorrelativoInicio(correlativo);
        GeneracionCarta generacionCarta = new  GeneracionCarta( "",  1, 1, new ArrayList<>(), correlativo);
        log.info("individual", mapaRutPatenteIndividual.size());

        int contador = 0;

        generacionCarta = ejecutarMapaRutPatente(//proceso, procesoYMD,
                ejecutarMerge, generacionCarta, mapaRutPatenteIndividual);
        Integer cartInd= generacionCarta.getContTipoCartas()-1;
        ejecutarMerge.setTotalIndividuales(String.valueOf(cartInd));

        /***Crear Archivo Consolidado***/
        log.info("masivo", mapaRutPatenteMasiva.size());

        generacionCarta = ejecutarMapaRutPatente( //proceso, procesoYMD,
                ejecutarMerge,
                generacionCarta, mapaRutPatenteMasiva);
        Integer cartMas= generacionCarta.getContTipoCartas()-1;
        ejecutarMerge.setTotalMasivas(String.valueOf(cartMas));
        ejecutarMerge.setTotalCartas(String.valueOf(generacionCarta.getContFolioProceso()-1));
        ejecutarMerge.setTotalErroneas(String.valueOf( generacionCarta.getContFolioProceso()-cartInd - cartMas));
        ejecutarMerge.setCorrelativoHistorico(generacionCarta.getCorrelativoHistorico());

        ejecutarMerge.setListaPdfs(generacionCarta.getListaPdfs());

        return ejecutarMerge;
    }

    private GeneracionCarta ejecutarMapaRutPatente(EjecutarMerge ejecutarMerge,
                                                   GeneracionCarta generacionCarta,
                                                   Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapaRutPatente) throws Exception {
        //String procesoYMD = "aaa";
        // Extraemos valores iniciales del objeto de estado
        long correlativoHistorico = generacionCarta.getCorrelativoHistorico();
        int contFolioProceso = generacionCarta.getContFolioProceso();
        int contFolioTipo = 1;
        List<byte[]> listaPdfs = generacionCarta.getListaPdfs();
        String folioImpresoActual = generacionCarta.getProcesoGeneracionCarta();

        ejecutarMerge.setPathFolderCartas( apiProperties.getArchivoCreacionCarpeta()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza())  //contiene slash
                .concat(ejecutarMerge.getUnidad()).concat("/").concat(ejecutarMerge.getBaseNombre()).concat("/"));
        //
        // Rutas base
        String pathBaseCartas =
                ejecutarMerge.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaDocumento());

        CartaHtml cartaHtmlIndividual =  generarCartaHtmlIndividual();

        CartaHtml cartaHtmlMasiva =  generarCartaHtmlMasiva();

        // Iteramos sobre el mapa (Rut -> Patente -> List<Excel>)
        for (Map.Entry<String, Map<String, List<ExcelCobranzaNormalizado>>> entryRut : mapaRutPatente.entrySet()) {
            Map<String, List<ExcelCobranzaNormalizado>> mapaPatentes = entryRut.getValue();

            for (Map.Entry<String, List<ExcelCobranzaNormalizado>> entryPatente : mapaPatentes.entrySet()) {

                //if(entryRut.getKey().toString().equalsIgnoreCase("17174335")  && entryPatente.getKey().toString().equalsIgnoreCase("SCJK25")){
                    List<ExcelCobranzaNormalizado> listaExcelCobranza = entryPatente.getValue();

                    // Validar que el rut no esté vacío
                    if (listaExcelCobranza != null && !listaExcelCobranza.isEmpty() && !listaExcelCobranza.get(0).getRut().isBlank()) {

                        log.info("rut {} patente {} cantidad {}", entryRut.getKey() ,entryPatente.getKey(), listaExcelCobranza.size());
                        // Agrupamos en bloquesExcelCobranza de 7 para la grilla
                        List<List<ExcelCobranzaNormalizado>> bloquesExcelCobranza = new ArrayList<>();
                        for (int i = 0; i < listaExcelCobranza.size(); i += 7) {
                            bloquesExcelCobranza.add(listaExcelCobranza.subList(i, Math.min(i + 7, listaExcelCobranza.size())));
                        }

                        for (List<ExcelCobranzaNormalizado> bloqueActualExcelCobranza : bloquesExcelCobranza) {

                            ExcelCobranzaNormalizado excelCobranzaPrimero = bloqueActualExcelCobranza.get(0);
                            folioImpresoActual = String.valueOf(contFolioProceso);

                            byte[] pdfGenerado;

                            /***INDIVIDUAL***/
                            // Lógica de generación de Plantilla e iText 7
                            if (listaExcelCobranza.size() == 1) {
                                String html = PlantillaCobranzas.generarPlantillaCobranzaIndividual(
                                        ejecutarMerge.getBaseNombre().concat("-").concat(String.valueOf(correlativoHistorico)),
                                        String.valueOf(contFolioProceso),
                                        cartaHtmlIndividual,
                                        excelCobranzaPrimero
                                );

                                // Aquí usamos el método de iText 7 con el código de barras
                                pdfGenerado = plantillaService.generarPdffromHtmlCobranzaConBarcode(html,
                                        excelCobranzaPrimero.getCodigoBarra(), excelCobranzaPrimero.getCodigoSeguimiento());

                                //log.info("Folio Proceso: {} | CodigoSeguimiento: {} ", contFolioProceso, excelCobranzaPrimero.getCodigoSeguimiento());
                            } else {
                                String html = PlantillaCobranzas.generarPlantillaCobranzaMasiva(
                                        ejecutarMerge.getBaseNombre().concat("-").concat(String.valueOf(correlativoHistorico)),
                                        String.valueOf(contFolioProceso),
                                        cartaHtmlMasiva,
                                        excelCobranzaPrimero,
                                        bloqueActualExcelCobranza);

                                pdfGenerado = plantillaService.generarPdffromHtmlCodeEan(html, excelCobranzaPrimero.getCodigoSeguimiento());
                                //log.info("Folio Proceso: {} | CodigoSeguimiento: {} ", contFolioProceso, excelCobranzaPrimero.getCodigoSeguimiento());
                            }

                            String nombreArchivo = String.valueOf(correlativoHistorico).concat("_")
                                    .concat(String.format("%d_%d_%d_%s%s_%s.pdf",
                                    contFolioProceso, contFolioTipo, bloqueActualExcelCobranza.size(),
                                    excelCobranzaPrimero.getRut(),
                                    excelCobranzaPrimero.getDv(),
                                    excelCobranzaPrimero.getCodigoSeguimiento()));

                            //tipo cobranza - notificacion - procesados - upload
                            //carta documento - reporte - consolidado / cobranza - notificacion
                            //unidad 1juzgado 2juzgado tesoreria
                            //proceso 2026_01_01_01_01_01
                            //archivo xxxx.pdf
                            String archivo = plantillaService.guardarPdfIndividual(pdfGenerado, pathBaseCartas, nombreArchivo);

                            log.info("Correlativo proceso: {} | creado Archivo: {} ", correlativoHistorico, archivo);

                            // Actualización de contadores y lista
                            listaPdfs.add(pdfGenerado);
                            contFolioProceso++;
                            contFolioTipo++;
                            correlativoHistorico++;

                        }
                    }
                    break; //
            }
            break; //
        }
        // Retornamos el nuevo estado
        return new GeneracionCarta(folioImpresoActual, contFolioProceso, contFolioTipo, listaPdfs, correlativoHistorico);
    }

    private EjecutarMerge generarReporte(EjecutarMerge ejecutarMerge,
                                List<ExcelCobranzaNormalizado> excelCobranzas) throws Exception {


        log.info("generarReporte procesados: {} nombre: {}", ejecutarMerge.getTotalCartas(),
                ejecutarMerge.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()).concat("/"),
                apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte());

        String html = PlantillaCobranzas.generarPlantillaReporteCobranzas (
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlNombreArchivoReporte()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogo()),
                ejecutarMerge);

        byte[] pdf = plantillaService.generarPdffromHtml(html);

        //Crear Archivo Reporte
        //tipo cobranza - notificacion - procesados - upload
        //carta documento - reporte - consolidado / cobranza - notificacion
        //unidad 1juzgado 2juzgado tesoreria
        //proceso 2026_01_01_01_01_01
        //archivo xxxx.pdf
        plantillaService.guardarPdfIndividual(pdf,
                ejecutarMerge.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()).concat("/"),
                        apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()
                );
        ejecutarMerge.setPathReporte(ejecutarMerge.getPathFolderCartas()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()).concat("/")
                .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()));

        CrearJsonExcel.crearJson4Merge(ejecutarMerge);

        return ejecutarMerge;
    }

    private CartaHtml generarCartaHtmlIndividual() throws Exception {
        //Tres Logos Escudo, Firma y BCIx2
        List<String> logos = Arrays.asList(
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoEscudo()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoFirmaTesoreria()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoBciCupon())

        );

        ClassPathResource imgFileEsc = new ClassPathResource(logos.get(0)); //bci
        ClassPathResource imgFileFirma = new ClassPathResource(logos.get(1)); //firma
        ClassPathResource imgFileBci = new ClassPathResource(logos.get(2)); //escudo

        byte[] imageBytesEsc;
        try (InputStream is = imgFileEsc.getInputStream()) {
            imageBytesEsc = is.readAllBytes();
        }

        byte[] imageBytesFirma;
        try (InputStream is = imgFileFirma.getInputStream()) {
            imageBytesFirma = is.readAllBytes();
        }

        byte[] imageBytesBci;
        try (InputStream is = imgFileBci.getInputStream()) {
            imageBytesBci = is.readAllBytes();
        }

        String base64Esc = Base64.getEncoder().encodeToString(imageBytesEsc);
        String base64Firma = Base64.getEncoder().encodeToString(imageBytesFirma);
        String base64Bci = Base64.getEncoder().encodeToString(imageBytesBci);

        /***Cargar una vez el String de html**/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                apiProperties.getArchivoHtmlNombreArchivoCobranzaIndividual()));

        return new CartaHtml(htmlIndividual, Arrays.asList(base64Esc, base64Firma, base64Bci));
    }

    private CartaHtml generarCartaHtmlMasiva() throws Exception {
        //Dos Logos Escudo y Firma
        List<String> logos = Arrays.asList(
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoEscudo()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogoFirmaTesoreria())
                );

        ClassPathResource imgFileEsc = new ClassPathResource(logos.get(0)); //escudo
        ClassPathResource imgFileFirma = new ClassPathResource(logos.get(1)); //firma

        byte[] imageBytesEsc;
        try (InputStream is = imgFileEsc.getInputStream()) {
            imageBytesEsc = is.readAllBytes();
        }

        String base64Esc = Base64.getEncoder().encodeToString(imageBytesEsc);

        byte[] imageBytesFirma;
        try (InputStream is = imgFileFirma.getInputStream()) {
            imageBytesFirma = is.readAllBytes();
        }

        String base64Firma = Base64.getEncoder().encodeToString(imageBytesFirma);

        /***Cargar una vez el String de html**/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                apiProperties.getArchivoHtmlNombreArchivoCobranzaMasiva()));

        return new CartaHtml(htmlIndividual, Arrays.asList(base64Esc, base64Firma));
    }

    public long obtenerCorrelativo() {

        Optional<ProcesoCarta> optProcesoNotificacionCarta =
                procesoCartaRepository.findTopByTipoCartaOrderByFechaRegistroDesc(TIPO);

        if(optProcesoNotificacionCarta.isPresent()) {
            return optProcesoNotificacionCarta.get().getUltimoCorrelativo() + 1;
        }
        return 1;
    }

    public static String extraerIdentificador(String texto) {
        // Expresión regular: busca "CD", seguido de espacio(s) y números
        Pattern pattern = Pattern.compile("CD\\s+\\d+");
        Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            return matcher.group(); // Devuelve "CD 813" o "CD 815"
        }

        return ""; // O manejar si no se encuentra
    }
}
