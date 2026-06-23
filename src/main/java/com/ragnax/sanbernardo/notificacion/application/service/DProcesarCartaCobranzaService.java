package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.MailComponent;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private MailComponent mailComponent;

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

        try{
            log.info("excelCobranzasMerge {}", ejecutarCartas.getListaExcelCobranzaMerge().size());

            ejecutarMerge = mapaRutPatenteFila(ejecutarCartas,
                    ejecutarCartas.getListaExcelCobranzaMerge());

            mailComponent.enviarCorreoProcesamiento(
                    ejecutarMerge.getObservacion(),
                    ejecutarMerge.getTipo(),
                    ejecutarMerge.getUnidad(),
                    Integer.parseInt(ejecutarMerge.getSizeArchivoMerge()),
                    ejecutarMerge.getNombreArchivoMerge());

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

        procesoCartaRepository.save(
                new ProcesoCarta(LocalDateTime.now(), ejecutarCartas.getCorrelativoInicio(),
                        ejecutarCartas.getCorrelativoHistorico(),
                        ejecutarCartas.getUsuarioMerge(),
                        ejecutarCartas.getPathArchivoUpload(),
                        ejecutarCartas.getPathArchivoMerge(),
                        ejecutarCartas.getBaseNombre(),
                        ejecutarCartas.getTipo()));

        /***Generacion del Reporte*/
        ejecutarCartas = generarReporte(ejecutarCartas);

        return ejecutarCartas;
    }


    private EjecutarCartas ejecucionListaMapaRutPatente(EjecutarCartas ejecutarCartas,
                                                        Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatenteIndividual,
                                                        Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatenteMasiva) throws Exception {

        long correlativo = obtenerCorrelativo();
        ejecutarCartas.setCorrelativoInicio(correlativo);
        GeneracionCarta generacionCarta = new  GeneracionCarta( "",  1, 1, 1, correlativo, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        log.info("individual {}", mapaRutPatenteIndividual.size());

        generacionCarta = ejecutarMapaRutPatente(//proceso, procesoYMD,
                ejecutarCartas, generacionCarta, mapaRutPatenteIndividual, 1);
        //saveCartas(generacionCarta.getListaExcelCobranzaBD());
        List<ExcelCobranzaImpresion> totalRegistrosImprimir = new ArrayList<>(generacionCarta.getListaExcelCobranzaImpresion());
        List<ExcelCobranzaBD> totalRegistrosBD = new ArrayList<>(generacionCarta.getListaExcelCobranzaBD());

        log.info("totalRegistrosImprimir", totalRegistrosImprimir);
        Integer cartInd= generacionCarta.getContFolioTipo()-1;
        ejecutarCartas.setTotalIndividuales(String.valueOf(cartInd));

        /***Crear Archivo Consolidado***/
        log.info("masivo {}", mapaRutPatenteMasiva.size());

        generacionCarta = ejecutarMapaRutPatente( //proceso, procesoYMD,
                ejecutarCartas, generacionCarta, mapaRutPatenteMasiva, 2);
        //saveCartas(generacionCarta.getListaExcelCobranzaBD());
        totalRegistrosImprimir.addAll(generacionCarta.getListaExcelCobranzaImpresion());
        totalRegistrosBD.addAll(generacionCarta.getListaExcelCobranzaBD());
        log.info("totalRegistrosImprimir", totalRegistrosImprimir);
        /*************************/
        //Archivo POSTAL
        /***Impresion en Reporte***/
        Integer cartMas= generacionCarta.getContFolioTipo()-1;
        ejecutarCartas.setTotalMasivas(String.valueOf(cartMas));

        ejecutarCartas.setTotalCartas(String.valueOf(generacionCarta.getContFolioProceso()-1));
        ejecutarCartas.setTotalErroneas(String.valueOf( generacionCarta.getContFolioProceso()- 1 - cartInd - cartMas));
        ejecutarCartas.setCorrelativoHistorico(generacionCarta.getCorrelativoHistorico());
        // ejecutarCartas.setActivarConsolidadoImprenta(false);

        // 3. Crear ZIP Final leyendo desde disco (No de memoria)

        crearPdfConsolidado(ejecutarCartas, totalRegistrosBD);

        exportarAExcelPostal(ejecutarCartas, totalRegistrosImprimir);
        //ejecutarCartas.setListaPdfs(generacionCarta.getListaPdfs());
        return ejecutarCartas;
    }

    private GeneracionCarta ejecutarMapaRutPatente(EjecutarCartas ejecutarCartas,
                                                   GeneracionCarta generacionCarta,
                                                   Map<String, Map<String, List<ExcelCobranzaMerge>>> mapaRutPatente,
                                                   int tipo) throws Exception {

        ejecutarCartas.setPathFolderCartas(apiProperties.getArchivoCreacionCarpeta()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza())  //contiene slash
                .concat(ejecutarCartas.getUnidad()).concat("/").concat(ejecutarCartas.getBaseNombre()).concat("/"));

        List<ExcelCobranzaImpresion> listaExcelCobranzaImpresion = new ArrayList<>();
        List<ExcelCobranzaBD> listaExcelCobranzaBD = new ArrayList<>();

        long correlativoHistorico = generacionCarta.getCorrelativoHistorico(); //historico
        int contFolioProceso = generacionCarta.getContFolioProceso();  //proceso
        int contFolioTipo = 1;  //proceso
        CartaHtml cartaHtmlIndividual = generarCartaHtmlIndividual();
        CartaHtml cartaHtmlMasiva = generarCartaHtmlMasiva();

        for (Map.Entry<String, Map<String, List<ExcelCobranzaMerge>>> entryRut : mapaRutPatente.entrySet()) {
            for (Map.Entry<String, List<ExcelCobranzaMerge>> entryPatente : entryRut.getValue().entrySet()) {
                ExcelCobranzaImpresion excelCobranzaImpresion = new ExcelCobranzaImpresion();
                ExcelCobranzaBD excelCobranzaBD = new ExcelCobranzaBD();

                List<ExcelCobranzaMerge> listaExcelCobranzaMerge = entryPatente.getValue();

                if (listaExcelCobranzaMerge == null || listaExcelCobranzaMerge.isEmpty() || listaExcelCobranzaMerge.get(0).getRut().isBlank()) continue;

                // Bloques de 7 para la grilla
                for (int i = 0; i < listaExcelCobranzaMerge.size(); i += 7) {
                    List<ExcelCobranzaMerge> bloque = listaExcelCobranzaMerge.subList(i, Math.min(i + 7, listaExcelCobranzaMerge.size()));
                    ExcelCobranzaMerge primero = bloque.get(0);

                    String procCorrelativoHistorico = String.format("%s_h%d_p%d_t%d_c%d",
                            ejecutarCartas.getBaseNombre(), correlativoHistorico, contFolioProceso, tipo, bloque.size());

                    // Generar HTML
                    String html = (listaExcelCobranzaMerge.size() == 1)
                            ? PlantillaCobranzas.generarPlantillaCobranzaIndividual(procCorrelativoHistorico, String.valueOf(contFolioProceso), cartaHtmlIndividual, primero)
                            : PlantillaCobranzas.generarPlantillaCobranzaMasiva(procCorrelativoHistorico, String.valueOf(contFolioProceso), cartaHtmlMasiva, primero, bloque);

                    // --- GENERACIÓN Y GUARDADO FÍSICO INMEDIATO ---
                    //byte[] pdfGenerado = (listaExcelCobranzaMerge.size() == 1)
                    //        ? plantillaService.generarPdffromHtmlCobranzaConBarcode(html, primero.getCodigoBarra(), primero.getCodigoSeguimiento())
                    //        : plantillaService.generarPdffromHtmlCodeEan(html, primero.getCodigoSeguimiento());

                    String nombreArchivo = String.format("%d_%d_%d_%d_%s%s_%s.pdf",
                            correlativoHistorico, contFolioProceso, tipo, bloque.size(), primero.getRut(), primero.getDv(), primero.getCodigoSeguimiento());

                    if (correlativoHistorico % 1000 == 0) {
                        log.info("procCorrelativo: {} | creado Archivo: {} ", procCorrelativoHistorico, nombreArchivo);
                    }

                    //del excelXXXMerge to Impresion
                    //Para Imprimir en el POSTAL
                    BeanUtils.copyProperties(primero, excelCobranzaImpresion);
                    excelCobranzaImpresion.setCorrelativoHistorico(String.valueOf(correlativoHistorico));
                    excelCobranzaImpresion.setProcCorrelativoHistorico(procCorrelativoHistorico);
                    excelCobranzaImpresion.setCorrelativoImpresion(String.valueOf(contFolioProceso));
                    excelCobranzaImpresion.setContFolioTipo(String.valueOf(contFolioProceso));
                    listaExcelCobranzaImpresion.add(excelCobranzaImpresion);

                    BeanUtils.copyProperties(excelCobranzaImpresion, excelCobranzaBD);
                    // Agregar a la lista para el saveCartas posterior (Base de Datos)
                    excelCobranzaBD.setNombreArchivo(ejecutarCartas.getNombreArchivoMerge());
                    excelCobranzaBD.setCodigoCd(ejecutarCartas.getObservacion());
                    excelCobranzaBD.setCantidadPatente(String.valueOf(listaExcelCobranzaMerge.size()));
                    excelCobranzaBD.setHtml(html);
                    excelCobranzaBD.setPdf(null);

                    //listaExcelCobranzaBD.add(excelCobranzaBD);
                            //pdfGenerado));

                    contFolioTipo++;
                    contFolioProceso++;
                    correlativoHistorico++;
                   // break;
                }
                //break;
            }
            //break;
        }
        // Actualizamos el estado del objeto generacionCarta
        generacionCarta.setContFolioTipo(contFolioTipo);  //contador en ind o masivo
        generacionCarta.setContFolioProceso(contFolioProceso);  // correlativo "Corr:"
        generacionCarta.setCorrelativoHistorico(correlativoHistorico); // correlativo "Corr:"
        generacionCarta.setListaExcelCobranzaImpresion(listaExcelCobranzaImpresion);
        generacionCarta.setListaExcelCobranzaBD(listaExcelCobranzaBD);

        return generacionCarta;
    }



    public EjecutarCartas crearPdfConsolidado(EjecutarCartas ejecutarCartas, List<ExcelCobranzaBD> registros) throws Exception {

        List<byte[]> listaPdfs = new ArrayList<>();

        byte[] pdfGenerado;
        Integer cantidad = 0;
        for (ExcelCobranzaBD reg : registros) {
            cantidad = Integer.parseInt(reg.getCantidadPatente());

            // Obtenemos el correlativo (asegúrate de que sea un tipo numérico, ej: int o Integer)
            int correlativo = Integer.parseInt(reg.getCorrelativoHistorico());

            if (cantidad == 1) {
                // Solo escribe en el log si es divisible por 1000
                if (correlativo % 1000 == 0) {
                    log.info("ind {} - {} - {} - {}", reg.getProcCorrelativoHistorico(), reg.getCorrelativoHistorico(),
                            reg.getCorrelativoImpresion(), reg.getContFolioTipo());
                }
                listaPdfs.add(reg.getPdf());

            } else if (cantidad > 1) {
                // Solo escribe en el log si es divisible por 1000
                if (correlativo % 1000 == 0) {
                    log.info("masivo {} - {} - {} - {}", reg.getProcCorrelativoHistorico(), reg.getCorrelativoHistorico(),
                            reg.getCorrelativoImpresion(), reg.getContFolioTipo());
                }
                listaPdfs.add(reg.getPdf());
            }
        }
        plantillaService.unirPdfs(
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaConsolidado())
                        .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado()),
                listaPdfs);

        ejecutarCartas.setPathArchivoConsolidado(ejecutarCartas.getPathFolderCartas()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaConsolidado())
                .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado()));

        CrearJsonExcel.crearJson5Consolidado(new EjecutarConsolidado(
                ejecutarCartas.getTipo(), ejecutarCartas.getUnidad(), ejecutarCartas.getPathArchivoConsolidado(), false));

        return ejecutarCartas;
    }

    private void saveCartas(List<ExcelCobranzaBD> impresiones){
        log.info("saveCartas {} registros en impresiones", impresiones.size());
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

    private CdnSeguimiento mapToEntity(ExcelCobranzaBD hijo) {
        return CdnSeguimiento.builder()
                .codigoImpresion(hijo.getProcCorrelativoHistorico())
                .nombreArchivo(hijo.getNombreArchivo())
                .codigoCd(hijo.getCodigoCd()) // Usando correlativo como ID
                .correlativoHistorico(Integer.parseInt( hijo.getCorrelativoHistorico()))
                .contFolioProceso(Integer.parseInt(hijo.getCorrelativoImpresion()))
                .contFolioTipo(Integer.parseInt(hijo.getContFolioTipo()))
                // OJO: Tu entidad pide Integer para nombre_archivo
                .rut(hijo.getRut())
                .patente(hijo.getPlacaPatente())
                .cantidadPatente(Integer.parseInt(hijo.getCantidadPatente()))
                .codigoSeguimiento(hijo.getCodigoSeguimiento())
                .archivoPdf(hijo.getPdf())
                .build();
    }

    private EjecutarCartas generarReporte(EjecutarCartas ejecutarCartas) throws Exception {


        log.info("generarReporte procesados: {} nombre: {}", ejecutarCartas.getTotalCartas(),
                ejecutarCartas.getPathFolderCartas()
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaReporte()).concat("/"),
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
                        .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaReporte()).concat("/"),
                apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()
        );
        ejecutarCartas.setPathReporte(ejecutarCartas.getPathFolderCartas()
                .concat(apiProperties.getArchivoCreacionAdjuntoSubCarpetaReporte())
                .concat(apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte()));

        /**Crear Json ejecutar Cartas aca se obtiene el Boton de Validado**/
        CrearJsonExcel.crearJson6Carta(ejecutarCartas);

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

    public EjecutarUpload exportarAExcelPostal(EjecutarCartas ejecutarCartas, List<ExcelCobranzaImpresion> totalRegistrosImprimir) {

        String dirArchivoExcelPostal = null;

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
                    "Orden_Impresion", "ClientId", "Destinatario", "DireccionOriginal", "Comuna_Propuesta", "Codigo_Seguimiento", "Codigo_Postal", "Sector", "Cuartel", "Servicio", "Destino_Clasificacion"
            };

            // 3. Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            log.info("Generando Excel con {} registros...", totalRegistrosImprimir.size());

            // 4. Datos
            int rowNum = 1;
            int contador = 0;

            for (ExcelCobranzaImpresion item : totalRegistrosImprimir) {

                if (item == null) {
                    log.warn("⚠️ Item nulo detectado, se omite");
                    continue;
                }

                Row row = sheet.createRow(rowNum++);
                contador++;
                int i =0;
                //Orden de impresión	clientId	Destinatario	Dirección Original	Comuna Propuesta	Código Seguimiento	Código Postal	Sector	Cuartel	Servicio	Destino clasificación
                row.createCell(i).setCellValue(nvl(item.getCorrelativoImpresion()));
                row.createCell(++i).setCellValue(nvl(item.getClientId()));
                row.createCell(++i).setCellValue(nvl(item.getNombres().concat(" ").concat(item.getApellidoPaterno()).concat(" ").concat(item.getApellidoMaterno())));
                row.createCell(++i).setCellValue(nvl(item.getDireccion()));
                row.createCell(++i).setCellValue(nvl(item.getComuna()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoSeguimiento()));
                row.createCell(++i).setCellValue(nvl(item.getCodigoPostal()));
                row.createCell(++i).setCellValue(nvl(item.getIdSector()));
                row.createCell(++i).setCellValue(nvl(item.getIdCuartel()));
                row.createCell(++i).setCellValue(nvl(item.getServicio()));
                row.createCell(++i).setCellValue(nvl(item.getDestinoClasificacion()));
            }

            // 5. Validación
            totalFilasGeneradas = rowNum - 1;

            log.info("Total iterados: {}", contador);
            log.info("Total filas Excel: {}", totalFilasGeneradas);
            log.info("Total lista original: {}", totalRegistrosImprimir.size());

            if (totalFilasGeneradas != totalRegistrosImprimir.size()) {
                log.error("❌ Diferencia detectada! Lista: {} vs Excel: {}",
                        totalRegistrosImprimir.size(), totalFilasGeneradas);
            } else {
                log.info("✅ Validación OK: no se perdieron registros");
            }

            // 6. AutoSize (opcional)
            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 7. Archivo
            //nombreArchivo = proceso.concat("_").concat(ejecutarUpload.getUnidad()).concat("_").concat(apiProperties.getArchivoExcelNombreArchivoNormalizadaCobranza());
            // 7. Dreccion
            dirArchivoExcelPostal = ejecutarCartas.getPathArchivoConsolidado().replace("consolidado.pdf", "postal.xlsx");

            log.info("Ruta archivo: {}", dirArchivoExcelPostal);

            File archivoFinal = new File(dirArchivoExcelPostal);

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

        //ejecutarUpload.setPathArchivoNormalizado(dirArchivoExcelToNormalize);
        //ejecutarUpload.setSizeArchivoNormalizado(String.valueOf(totalFilasGeneradas));

        return null;
    }

    private String nvl(Object value) {
        return value == null ? "" : value.toString();
    }
}