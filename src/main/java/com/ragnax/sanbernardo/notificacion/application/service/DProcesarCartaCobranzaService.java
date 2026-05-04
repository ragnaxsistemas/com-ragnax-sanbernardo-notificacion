package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.PdfComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCargar;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCobranzas;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.CdnSeguimiento;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.ProcesoCarta;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.CdnSeguimientoRepository;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.ProcesoCartaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private CdnSeguimientoRepository cdnSeguimientoRepository;


    @Autowired
    private ApiProperties apiProperties;

    private static final String TIPO = "COBRANZA";
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    public EjecutarMerge processArchivoCobranza(//String proceso, String procesoGen, String procesoYMD,
                                                EjecutarMerge ejecutarMerge)  {

        EjecutarCartas ejecutarCartas = new EjecutarCartas();
        BeanUtils.copyProperties(ejecutarMerge, ejecutarCartas);
        // 2. PASO CRÍTICO: Guardar el archivo CSV de correos en disco AHORA
        // Debes implementar un método en tu storageService para guardar este archivo específico
        // antes de que el request termine.
        List<ExcelCobranzaMerge> excelCobranzasMerge = ejecutarCartas.getListaExcelCobranzaMerge();

        try{
            log.info("excelCobranzasMerge {}", ejecutarCartas.getListaExcelCobranzaMerge().size());
            //Files.copy(pathOrigen, pathDestino, StandardCopyOption.REPLACE_EXISTING);
            //Cambiar el Excel de Carpeta, ya ha sido leido, eliminarlo de la carpeta

            ejecutarMerge = mapaRutPatenteFila(ejecutarCartas,
                    excelCobranzasMerge);

        }catch(Exception e){
            log.error("Exception error", e);
        }
        return ejecutarMerge;
    }

    //debe venir declarado o el ultimo se guarda en vacio.
    private EjecutarCartas mapaRutPatenteFila(EjecutarCartas ejecutarCartas,
                                    List<ExcelCobranzaMerge> excelCobranzaMerges) throws Exception {

        Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatenteIndividual = new HashMap<>();

        Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatenteMasiva = new HashMap<>();

        Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaLargoRutPatente =
                excelCobranzaMerges.stream()
                        .filter(e -> e.getRut() != null && !e.getRut().isBlank()) // Filtra ruts nulos o vacíos
                        .collect(Collectors.groupingBy(
                                ExcelCobranzaMerge::getRut,
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
        ejecutarCartas = ejecucionListaMapaRutPatente(
                ejecutarCartas,
                mapaRutPatenteIndividual, mapaRutPatenteMasiva);
        //6_cobranza/tesoreria/CD-FTX_2026_04_27_11_57_14/CARTAS

        /***plantillaService.unirPdfs(
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaConsolidado())
                        .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado()),
                ejecutarCartas.getListaPdfs());***/

        procesoCartaRepository.save(
                new ProcesoCarta(LocalDateTime.now(), ejecutarCartas.getCorrelativoInicio(),
                        ejecutarCartas.getCorrelativoHistorico(),
                        ejecutarCartas.getUsuarioMerge(),
                        ejecutarCartas.getPathArchivoUpload(),
                        ejecutarCartas.getPathArchivoMerge(),
                        ejecutarCartas.getBaseNombre(),
                        ejecutarCartas.getTipo()));

        /***Generacion del Reporte*/
        ejecutarCartas = generarReporte(ejecutarCartas,  excelCobranzaMerges);

        return ejecutarCartas;
    }


    private EjecutarCartas ejecucionListaMapaRutPatente(EjecutarCartas ejecutarCartas,
                                                         Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatenteIndividual,
                                                         Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatenteMasiva) throws Exception {

        long correlativo = obtenerCorrelativo();
        ejecutarCartas.setCorrelativoInicio(correlativo);
        GeneracionCarta generacionCarta = new  GeneracionCarta( "",  1, 1, correlativo, new ArrayList<>(), new ArrayList<>());
        log.info("individual", mapaRutPatenteIndividual.size());

        generacionCarta = ejecutarMapaRutPatente(//proceso, procesoYMD,
                ejecutarCartas, generacionCarta, mapaRutPatenteIndividual);

        saveCartas(generacionCarta.getListaExcelCobranzaImpresion());
        List<ExcelCobranzaImpresion> totalRegistros = new ArrayList<>(generacionCarta.getListaExcelCobranzaImpresion());

        Integer cartInd= generacionCarta.getContTipoCartas()-1;
        ejecutarCartas.setTotalIndividuales(String.valueOf(cartInd));

        /***Crear Archivo Consolidado***/
        log.info("masivo", mapaRutPatenteMasiva.size());

        generacionCarta = ejecutarMapaRutPatente( //proceso, procesoYMD,
                ejecutarCartas,
                generacionCarta, mapaRutPatenteMasiva);
        saveCartas(generacionCarta.getListaExcelCobranzaImpresion());
        totalRegistros.addAll(generacionCarta.getListaExcelCobranzaImpresion());

        /***Impresion en Reporte***/
        Integer cartMas= generacionCarta.getContTipoCartas()-1;
        ejecutarCartas.setTotalMasivas(String.valueOf(cartMas));
        ejecutarCartas.setTotalCartas(String.valueOf(generacionCarta.getContFolioProceso()-1));
        ejecutarCartas.setTotalErroneas(String.valueOf( generacionCarta.getContFolioProceso()-cartInd - cartMas));
        ejecutarCartas.setCorrelativoHistorico(generacionCarta.getCorrelativoHistorico());

        // 3. Crear ZIP Final leyendo desde disco (No de memoria)
        crearPdfConsolidado(ejecutarCartas, totalRegistros);
        //ejecutarCartas.setListaPdfs(generacionCarta.getListaPdfs());
        return ejecutarCartas;
    }

    public void crearPdfConsolidado(EjecutarCartas ejecutarCartas, List<ExcelCobranzaImpresion> registros) throws Exception {

        List<byte[]> listaPdfs = new ArrayList<>();
        //String pathDocumentos = ejecutarCartas.getPathFolderCartas().concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaDocumento());
        //String pathDestino = ejecutarCartas.getPathFolderCartas().concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaConsolidado());
        //String nombreFin = apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado().concat(".html");
        byte[] pdfGenerado;
        Integer cantidad = 0;
        for (ExcelCobranzaImpresion reg : registros) {
            cantidad = Integer.parseInt(reg.getCantidadPatente());

            // Obtenemos el correlativo (asegúrate de que sea un tipo numérico, ej: int o Integer)
            int correlativo = Integer.parseInt(reg.getCorrelativoHistorico());

            if (cantidad == 1) {
                // Solo escribe en el log si es divisible por 1000
                if (correlativo % 1000 == 0) {
                    log.info("ind {} - {} - {} - {}", reg.getProcCorrelativoImpresion(), correlativo,
                            reg.getContFolioProceso(), reg.getContFolioTipo());
                }
                listaPdfs.add(reg.getPdf());

            } else if (cantidad > 1) {
                // Solo escribe en el log si es divisible por 1000
                if (correlativo % 1000 == 0) {
                    log.info("mas {} - {} - {} - {}", reg.getProcCorrelativoImpresion(), correlativo,
                            reg.getContFolioProceso(), reg.getContFolioTipo());
                }
                listaPdfs.add(reg.getPdf());
            }
        }
        plantillaService.unirPdfs(
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaConsolidado())
                        .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado()),
                listaPdfs);
    }
        //try {
            // 1. Asegurar que la carpeta de destino existe
            //Files.createDirectories(Paths.get(pathDestino));
            //Path rutaFinal = Paths.get(pathDestino).resolve(nombreFin);

            /***try (BufferedWriter writer = Files.newBufferedWriter(rutaFinal, StandardCharsets.UTF_8)) {
                // 2. Escribir el encabezado del archivo único
                writer.write("<html><head><meta charset='UTF-8'><style>");
                writer.write(".page-break { page-break-after: always; } "); // Para que cada carta sea una hoja
                writer.write("body { margin: 0; padding: 0; }");
                writer.write("</style></head><body>");

                // 3. Iterar sobre los registros y leer sus archivos
                for (ExcelCobranzaImpresion reg : registros) {
                    File archivoCarta = new File(pathDocumentos, reg.getNombreArchivo());

                    if (archivoCarta.exists()) {
                        // Leer el contenido del HTML individual
                        String contenido = Files.readString(archivoCarta.toPath(), StandardCharsets.UTF_8);

                        // 4. Extraer solo el contenido dentro de <body> (limpieza básica)
                        String cuerpoHtml = extraerCuerpo(contenido);

                        writer.write("<div class='page-break'>");
                        writer.write(cuerpoHtml);
                        writer.write("</div>");
                    } else {
                        log.warn("No se encontró el archivo: {}", archivoCarta.getAbsolutePath());
                    }
                }

                // 5. Cerrar el HTML
                writer.write("</body></html>");
                log.info("Consolidado HTML creado exitosamente en: {}", rutaFinal);

            }
        } catch (IOException e) {
            log.error("Error al concatenar archivos HTML: {}", e.getMessage());
        }
    }

    /**
     * Método auxiliar para obtener solo lo que está dentro de <body>
     */
    /***private String extraerCuerpo(String htmlCompleto) {
        if (htmlCompleto.contains("<body") && htmlCompleto.contains("</body>")) {
            int inicio = htmlCompleto.indexOf(">", htmlCompleto.indexOf("<body")) + 1;
            int fin = htmlCompleto.lastIndexOf("</body>");
            return htmlCompleto.substring(inicio, fin);
        }
        // Si no tiene body, devolvemos el contenido tal cual (o un div)
        return htmlCompleto;
    }***/

    private GeneracionCarta ejecutarMapaRutPatente(EjecutarCartas ejecutarCartas,
                                                   GeneracionCarta generacionCarta,
                                                   Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatente) throws Exception {

        ejecutarCartas.setPathFolderCartas(apiProperties.getArchivoCreacionCarpeta()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza())  //contiene slash
                .concat(ejecutarCartas.getUnidad()).concat("/").concat(ejecutarCartas.getBaseNombre()).concat("/"));

        List<ExcelCobranzaImpresion> listaExcelCobranzaImpresion = new ArrayList<>();
        long correlativoHistorico = generacionCarta.getCorrelativoHistorico();
        int contFolioProceso = generacionCarta.getContFolioProceso();
        int contFolioTipo = 1;
        //String pathBaseCartas = ejecutarCartas.getPathFolderCartas().concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaDocumento());
        CartaHtml cartaHtmlIndividual = generarCartaHtmlIndividual();
        CartaHtml cartaHtmlMasiva = generarCartaHtmlMasiva();

        for (Map.Entry<String, Map<String, List<ExcelCobranzaMerge>>> entryRut : mapaRutPatente.entrySet()) {
            for (Map.Entry<String, List<ExcelCobranzaMerge>> entryPatente : entryRut.getValue().entrySet()) {
                List<ExcelCobranzaMerge> listaExcelCobranza = entryPatente.getValue();

                if (listaExcelCobranza == null || listaExcelCobranza.isEmpty() || listaExcelCobranza.get(0).getRut().isBlank()) continue;

                // Bloques de 7 para la grilla
                for (int i = 0; i < listaExcelCobranza.size(); i += 7) {
                    List<ExcelCobranzaMerge> bloque = listaExcelCobranza.subList(i, Math.min(i + 7, listaExcelCobranza.size()));
                    ExcelCobranzaMerge primero = bloque.get(0);

                    String procCorrelativo = String.format("%s_h%d_p%d_t%d_c%d",
                            ejecutarCartas.getBaseNombre(), correlativoHistorico, contFolioProceso, contFolioTipo, bloque.size());

                    // Generar HTML
                    String html = (listaExcelCobranza.size() == 1)
                            ? PlantillaCobranzas.generarPlantillaCobranzaIndividual(procCorrelativo, String.valueOf(contFolioProceso), cartaHtmlIndividual, primero)
                            : PlantillaCobranzas.generarPlantillaCobranzaMasiva(procCorrelativo, String.valueOf(contFolioProceso), cartaHtmlMasiva, primero, bloque);

                    // --- GENERACIÓN Y GUARDADO FÍSICO INMEDIATO ---
                    byte[] pdfGenerado = (listaExcelCobranza.size() == 1)
                            ? plantillaService.generarPdffromHtmlCobranzaConBarcode(html, primero.getCodigoBarra(), primero.getCodigoSeguimiento())
                            : plantillaService.generarPdffromHtmlCodeEan(html, primero.getCodigoSeguimiento());

                    String nombreArchivo = String.format("%d_%d_%d_%d_%s%s_%s.pdf",
                            correlativoHistorico, contFolioProceso, contFolioTipo, bloque.size(), primero.getRut(), primero.getDv(), primero.getCodigoSeguimiento());

                    if (correlativoHistorico % 1000 == 0) {
                        log.info("procCorrelativo: {} | creado Archivo: {} ", procCorrelativo, nombreArchivo);
                    }
                    // Guardar en disco y liberar el array de bytes
                    //plantillaService.guardarPdfIndividual(pdfGenerado, pathBaseCartas, nombreArchivo);
                    //pdfGenerado = null; // Sugerencia al GC para liberar memoria


                    // Agregar a la lista para el saveCartas posterior (Base de Datos)
                    listaExcelCobranzaImpresion.add(convertirAHijo(primero, ejecutarCartas.getObservacion(),
                            nombreArchivo, procCorrelativo, String.valueOf(correlativoHistorico),
                            String.valueOf(contFolioProceso), String.valueOf(contFolioTipo),
                            String.valueOf(listaExcelCobranza.size()), pdfGenerado));

                    contFolioProceso++;
                    correlativoHistorico++;
                }
               // break;
            }
            //break;
        }
        // Actualizamos el estado del objeto generacionCarta
        generacionCarta.setContFolioProceso(contFolioProceso);
        generacionCarta.setCorrelativoHistorico(correlativoHistorico);
        generacionCarta.setListaExcelCobranzaImpresion(listaExcelCobranzaImpresion);

        return generacionCarta;
    }

    /***private GeneracionCarta ejecutarMapaRutPatente(EjecutarCartas ejecutarCartas,
                                                   GeneracionCarta generacionCarta,
                                                   Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatente) throws Exception {
        List<ExcelCobranzaImpresion> listaExcelCobranzaImpresion = new ArrayList<>();
        //String procesoYMD = "aaa";
        // Extraemos valores iniciales del objeto de estado
        String procCorrelativo = "";
        String html = "";
        long correlativoHistorico = generacionCarta.getCorrelativoHistorico();
        int contFolioProceso = generacionCarta.getContFolioProceso();
        int contFolioTipo = 1;
        List<byte[]> listaPdfs = generacionCarta.getListaPdfs();
        String folioImpresoActual = generacionCarta.getProcesoGeneracionCarta();

        ejecutarCartas.setPathFolderCartas(apiProperties.getArchivoCreacionCarpeta()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza())  //contiene slash
                .concat(ejecutarCartas.getUnidad()).concat("/").concat(ejecutarCartas.getBaseNombre()).concat("/"));
        // Rutas base
        String pathBaseCartas =
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaDocumento());

        CartaHtml cartaHtmlIndividual =  generarCartaHtmlIndividual();

        CartaHtml cartaHtmlMasiva =  generarCartaHtmlMasiva();

        // Iteramos sobre el mapa (Rut -> Patente -> List<Excel>)
        for (Map.Entry<String, Map<String, List<ExcelCobranzaMerge>>> entryRut : mapaRutPatente.entrySet()) {
            Map<String, List<ExcelCobranzaMerge>> mapaPatentes = entryRut.getValue();

            for (Map.Entry<String, List<ExcelCobranzaMerge>> entryPatente : mapaPatentes.entrySet()) {

                //if(entryRut.getKey().toString().equalsIgnoreCase("17174335")  && entryPatente.getKey().toString().equalsIgnoreCase("SCJK25")){
                List<ExcelCobranzaMerge> listaExcelCobranza = entryPatente.getValue();

                // Validar que el rut no esté vacío
                if (listaExcelCobranza != null && !listaExcelCobranza.isEmpty() && !listaExcelCobranza.get(0).getRut().isBlank()) {

                    log.info("rut {} patente {} cantidad {}", entryRut.getKey() ,entryPatente.getKey(), listaExcelCobranza.size());
                    // Agrupamos en bloquesExcelCobranza de 7 para la grilla
                    List<List<ExcelCobranzaMerge>> bloquesExcelCobranza = new ArrayList<>();
                    for (int i = 0; i < listaExcelCobranza.size(); i += 7) {
                        bloquesExcelCobranza.add(listaExcelCobranza.subList(i, Math.min(i + 7, listaExcelCobranza.size())));
                    }

                    for (List<ExcelCobranzaMerge> bloqueActualExcelCobranzaMerge : bloquesExcelCobranza) {

                        ExcelCobranzaMerge excelCobranzaMergePrimero = bloqueActualExcelCobranzaMerge.get(0);
                        folioImpresoActual = String.valueOf(contFolioProceso);

                        byte[] pdfGenerado;

                        /***INDIVIDUAL---/
                        // Lógica de generación de Plantilla e iText 7
                        if (listaExcelCobranza.size() == 1) {
                            procCorrelativo = ejecutarCartas.getBaseNombre().concat("_h").concat(String.valueOf(correlativoHistorico)).concat("_p").concat(String.valueOf(contFolioProceso)).concat("_t").concat(String.valueOf(contFolioTipo)).concat("_c").concat(String.valueOf(bloqueActualExcelCobranzaMerge.size()));
                            html = PlantillaCobranzas.generarPlantillaCobranzaIndividual(
                                    procCorrelativo,
                                    String.valueOf(contFolioProceso),
                                    cartaHtmlIndividual,
                                    excelCobranzaMergePrimero
                            );

                            // Aquí usamos el método de iText 7 con el código de barras
                            //pdfGenerado = plantillaService.generarPdffromHtmlCobranzaConBarcode(html,
                            //        excelCobranzaMergePrimero.getCodigoBarra(), excelCobranzaMergePrimero.getCodigoSeguimiento());

                            //log.info("Folio Proceso: {} | CodigoSeguimiento: {} ", contFolioProceso, excelCobranzaPrimero.getCodigoSeguimiento());
                        } else {
                            procCorrelativo = ejecutarCartas.getBaseNombre().concat("_h").concat(String.valueOf(correlativoHistorico)).concat("_p").concat(String.valueOf(contFolioProceso)).concat("_t").concat(String.valueOf(contFolioTipo)).concat("_c").concat(String.valueOf(bloqueActualExcelCobranzaMerge.size()));
                            html = PlantillaCobranzas.generarPlantillaCobranzaMasiva(
                                    procCorrelativo,
                                    String.valueOf(contFolioProceso),
                                    cartaHtmlMasiva,
                                    excelCobranzaMergePrimero,
                                    bloqueActualExcelCobranzaMerge);

                            //pdfGenerado = plantillaService.generarPdffromHtmlCodeEan(html, excelCobranzaMergePrimero.getCodigoSeguimiento());
                            //log.info("Folio Proceso: {} | CodigoSeguimiento: {} ", contFolioProceso, excelCobranzaPrimero.getCodigoSeguimiento());
                        }

                        String nombreArchivo = String.valueOf(correlativoHistorico).concat("_")
                                .concat(String.format("%d_%d_%d_%s%s_%s.pdf",
                                contFolioProceso, contFolioTipo, bloqueActualExcelCobranzaMerge.size(),
                                        excelCobranzaMergePrimero.getRut(),
                                        excelCobranzaMergePrimero.getDv(),
                                        excelCobranzaMergePrimero.getCodigoSeguimiento()));

                        //tipo cobranza - notificacion - procesados - upload
                        //carta documento - reporte - consolidado / cobranza - notificacion
                        //unidad 1juzgado 2juzgado tesoreria
                        //proceso 2026_01_01_01_01_01
                        //archivo xxxx.pdf
                        //String archivo = plantillaService.guardarPdfIndividual(pdfGenerado, pathBaseCartas, nombreArchivo);

                        //log.info("Correlativo proceso: {} | creado Archivo: {} ", correlativoHistorico, archivo);

                        // Actualización de contadores y lista
                        //listaPdfs.add(pdfGenerado);

                        listaExcelCobranzaImpresion.add(
                                convertirAHijo(
                                        bloqueActualExcelCobranzaMerge.get(0),
                                        ejecutarCartas.getObservacion(),
                                        nombreArchivo,
                                        procCorrelativo,
                                        String.valueOf(correlativoHistorico),
                                        String.valueOf(contFolioProceso),
                                        String.valueOf(contFolioTipo),
                                        String.valueOf(listaExcelCobranza.size()),
                                        html));
                        contFolioProceso++;
                        contFolioTipo++;
                        correlativoHistorico++;
                    }
                }
                //break;
            }
            //break;
        }
        // Retornamos el nuevo estado
        return new GeneracionCarta(folioImpresoActual, contFolioProceso, contFolioTipo, correlativoHistorico, listaPdfs, listaExcelCobranzaImpresion);
    }***/

    public ExcelCobranzaImpresion convertirAHijo(ExcelCobranzaMerge padre,
                                                 String observacion,
                                                 String nombreArchivo,
                                                 String procCorrelativo,
                                                 String correlativoHistorico,
                                                 String contFolioProceso,
                                                 String contFolioTipo,
                                                 String cantidadPatente,
                                                 byte[] pdfGenerado) {
        ExcelCobranzaImpresion hijo = new ExcelCobranzaImpresion();
        BeanUtils.copyProperties(padre, hijo);

        hijo.setProcCorrelativoImpresion(procCorrelativo);
        hijo.setNombreArchivo(nombreArchivo);
        hijo.setCodigoCd(observacion);
        hijo.setCorrelativoHistorico(correlativoHistorico);
        hijo.setContFolioProceso(contFolioProceso);
        hijo.setContFolioTipo(contFolioTipo);
        hijo.setCantidadPatente(cantidadPatente);
        hijo.setPdf(pdfGenerado);

        return hijo;
    }

    private void saveCartas(List<ExcelCobranzaImpresion> impresiones){
        if (impresiones == null || impresiones.isEmpty()) {
            log.warn("Lista de impresiones vacía, nada que salvar.");
            return;
        }

        try {
            List<CdnSeguimiento> entities = impresiones.stream()
                    .map(this::mapToEntity)
                    .collect(Collectors.toList());

            cdnSeguimientoRepository.saveAll(entities);
            log.info("Se han salvado {} registros en cdn_seguimiento", entities.size());
        } catch (Exception e) {
            log.error("Error al salvar cartas en la base de datos: {}", e.getMessage());
            //throw e; // Lanza la excepción para que el @Transactional haga rollback
        }
    }

    private CdnSeguimiento mapToEntity(ExcelCobranzaImpresion hijo) {
        return CdnSeguimiento.builder()
                .codigoImpresion(hijo.getProcCorrelativoImpresion())
                .nombreArchivo(hijo.getNombreArchivo())
                .codigoCd(hijo.getCodigoCd()) // Usando correlativo como ID
                .correlativoHistorico(Integer.parseInt( hijo.getCorrelativoHistorico()))
                .contFolioProceso(Integer.parseInt(hijo.getContFolioProceso()))
                .contFolioTipo(Integer.parseInt(hijo.getContFolioTipo()))
                // OJO: Tu entidad pide Integer para nombre_archivo
                .rut(hijo.getRut())
                .patente(hijo.getPlacaPatente())
                .cantidadPatente(Integer.parseInt(hijo.getCantidadPatente()))
                .codigoSeguimiento(hijo.getCodigoSeguimiento())
                .archivoPdf(hijo.getPdf())
                .build();
    }

    private EjecutarCartas generarReporte(EjecutarCartas ejecutarCartas,
                                         List<ExcelCobranzaMerge> excelCobranzas) throws Exception {


        log.info("generarReporte procesados: {} nombre: {}", ejecutarCartas.getTotalCartas(),
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()).concat("/"),
                apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte());

        String html = PlantillaCobranzas.generarPlantillaReporteCobranzas (
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlNombreArchivoReporte()),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                        apiProperties.getArchivoHtmlLogo()),
                ejecutarCartas);

        byte[] pdf = plantillaService.generarPdffromHtml(html);

        //Crear Archivo Reporte
        //tipo cobranza - notificacion - procesados - upload
        //carta documento - reporte - consolidado / cobranza - notificacion
        //unidad 1juzgado 2juzgado tesoreria
        //proceso 2026_01_01_01_01_01
        //archivo xxxx.pdf
        plantillaService.guardarPdfIndividual(pdf,
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()).concat("/"),
                        apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()
                );
        ejecutarCartas.setPathReporte(ejecutarCartas.getPathFolderCartas()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()).concat("/")
                .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()));

        CrearJsonExcel.crearJson5Carta(ejecutarCartas);

        return ejecutarCartas;
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
