/***package com.ragnax.sanbernardo.notificacion.application.service;

import com.ragnax.sanbernardo.notificacion.application.service.component.PdfComponent;
import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.ObtenerExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCargar;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.PlantillaCobranzas;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DProcesarCartaCobranzaServiceV2 {

    @Autowired
    private PdfComponent plantillaService;

    @Autowired
    private ProcesoCartaRepository procesoCartaRepository;

    @Autowired
    private ApiProperties apiProperties;

    private static final String TIPO = "COBRANZA";

    // 🔥 THREAD POOL CONTROLADO
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void executeNormalizacion(EjecutarNotificacion ejecutarNotificacion) {

        LocalDateTime ahora = LocalDateTime.now();

        String proceso = ahora.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
        String procesoGen = ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String procesoYMD = ahora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        processRequest(proceso, procesoGen, procesoYMD, ejecutarNotificacion);
    }

    private void processRequest(String proceso, String procesoGen, String procesoYMD,
                                EjecutarNotificacion ejecutarNotificacion) {

        try {

            Path pathOrigen;
            Path pathDestino;

            String dirExcel = apiProperties.getArchivoExcelNombreCarpetaNormalizada()
                    + ejecutarNotificacion.getTipo() + "/"
                    + ejecutarNotificacion.getUnidad() + "/"
                    + ejecutarNotificacion.getNombreArchivo();

            List<ExcelCobranzaNormalizado> excel;

            if (apiProperties.getProfile().equalsIgnoreCase("dev")) {

                Resource resource = new ClassPathResource(dirExcel);
                excel = ObtenerExcel.obtenerExcelCobranzaNormalizado(
                        resource.getInputStream(),
                        apiProperties.getArchivoExcelNombreHojaCobranzaNormalizada()
                );

                pathOrigen = resource.getFile().toPath();

            } else {
                pathOrigen = Paths.get(dirExcel);

                try (InputStream is = new FileInputStream(pathOrigen.toFile())) {
                    excel = ObtenerExcel.obtenerExcelCobranzaNormalizado(
                            is,
                            apiProperties.getArchivoExcelNombreHojaCobranzaNormalizada()
                    );
                }
            }

            String[] array = ejecutarNotificacion.getNombreArchivo().split("/");

            pathDestino = Paths.get(
                    apiProperties.getArchivoCreacionRespaldoNormalizado() + "/"
                            + ejecutarNotificacion.getTipo() + "/"
                            + ejecutarNotificacion.getUnidad() + "/"
                            + proceso + "/"
                            + array[array.length - 1]
            );

            Files.createDirectories(pathDestino.getParent());
            Files.copy(pathOrigen, pathDestino, StandardCopyOption.REPLACE_EXISTING);

            mapaRutPatenteFila(ejecutarNotificacion,
                    pathOrigen.getFileName().toString(),
                    pathDestino.getFileName().toString(),
                    proceso, procesoGen, procesoYMD, excel);

        } catch (Exception e) {
            log.error("Error proceso", e);
        }
    }

    private void mapaRutPatenteFila(EjecutarNotificacion ejecutarNotificacion,
                                    String pathOrigen,
                                    String pathDestino,
                                    String proceso,
                                    String procesoGen,
                                    String procesoYMD,
                                    List<ExcelCobranzaNormalizado> excel) throws Exception {

        Map<String, Map<String, List<ExcelCobranzaNormalizado>>> mapa =
                excel.stream()
                        .filter(e -> e.getRut() != null && !e.getRut().isBlank())
                        .collect(Collectors.groupingBy(
                                ExcelCobranzaNormalizado::getRut,
                                Collectors.groupingBy(e ->
                                        e.getPlacaPatente() == null ? "SIN_PATENTE" : e.getPlacaPatente())
                        ));

        long correlativoInicial = obtenerCorrelativo();

        AtomicLong correlativo = new AtomicLong(correlativoInicial);
        AtomicInteger contador = new AtomicInteger(1);

        List<String> rutasPdf = Collections.synchronizedList(new ArrayList<>());

        CartaHtml cartaIndividual = generarCartaHtmlIndividual();
        CartaHtml cartaMasiva = generarCartaHtmlMasiva();

        List<Future<?>> futures = new ArrayList<>();

        for (var entryRut : mapa.entrySet()) {
            for (var entryPatente : entryRut.getValue().entrySet()) {

                futures.add(executor.submit(() -> {

                    try {
                        List<ExcelCobranzaNormalizado> lista = entryPatente.getValue();

                        log.info("rut {} patente {} correlativo {} cantidad {}", entryRut.getKey() ,entryPatente.getKey(), correlativo.get(), lista.size());

                        for (int i = 0; i < lista.size(); i += 7) {

                            List<ExcelCobranzaNormalizado> bloque =
                                    lista.subList(i, Math.min(i + 7, lista.size()));

                            ExcelCobranzaNormalizado primero = bloque.get(0);

                            long correl = correlativo.getAndIncrement();
                            int folio = contador.getAndIncrement();

                            String html;
                            byte[] pdf;

                            if (lista.size() == 1) {
                                html = PlantillaCobranzas.generarPlantillaCobranzaIndividual(
                                        procesoYMD + "-" + correl,
                                        cartaIndividual,
                                        primero
                                );

                                pdf = plantillaService.generarPdffromHtmlCobranzaConBarcode(
                                        html,
                                        primero.getCodigoBarra(),
                                        primero.getCodigoSeguimiento()
                                );

                            } else {

                                html = PlantillaCobranzas.generarPlantillaCobranzaMasiva(
                                        procesoYMD + "-" + correl,
                                        cartaMasiva,
                                        primero,
                                        bloque
                                );

                                pdf = plantillaService.generarPdffromHtmlCodeEan(
                                        html,
                                        primero.getCodigoSeguimiento()
                                );
                            }

                            String nombre = correl + "_" +
                                    String.format("%d_%s%s_%s.pdf",
                                            folio,
                                            primero.getRut(),
                                            primero.getDv(),
                                            primero.getCodigoSeguimiento());

                            String ruta = plantillaService.guardarPdfIndividual(
                                    pdf,
                                    apiProperties.getArchivoCreacionCarpeta()
                                            + apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza()
                                            + apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaDocumento()
                                            + ejecutarNotificacion.getUnidad() + "/"
                                            + proceso + "/",
                                    nombre
                            );

                            rutasPdf.add(ruta);

                        }

                    } catch (Exception e) {
                        log.error("Error en hilo", e);
                    }
                }));
            }
        }

        // esperar todos los hilos
        for (Future<?> f : futures) {
            f.get();
        }

        // 🔥 unir desde rutas (NO byte[])
        plantillaService.unirPdfsV2(
                apiProperties.getArchivoCreacionCarpeta()
                        + apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza()
                        + apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaConsolidado()
                        + ejecutarNotificacion.getUnidad() + "/"
                        + proceso + "/"
                        + proceso + "_"
                        + apiProperties.getArchivoCreacionAdjuntoNombreArchivoConsolidado(),
                rutasPdf
        );

        procesoCartaRepository.save(new ProcesoCarta(
                LocalDateTime.now(),
                correlativoInicial,
                correlativo.get(),
                ejecutarNotificacion.getUsuario(),
                pathOrigen,
                pathDestino,
                proceso,
                TIPO
        ));

        generarReporte(procesoGen, proceso, ejecutarNotificacion, excel, contador.get());
    }

    private void generarReporte(String procesoGen,
                                String proceso,
                                EjecutarNotificacion ejecutarNotificacion,
                                List<ExcelCobranzaNormalizado> excelCobranzas,
                                Integer numFolio) throws Exception {

        log.info("generarReporte procesados: {}", numFolio);

        // 🔥 Agrupaciones (se mantienen pero optimizadas en una pasada)
        Map<String, List<ExcelCobranzaNormalizado>> mapaRut = new HashMap<>();
        Map<String, List<ExcelCobranzaNormalizado>> mapaPatente = new HashMap<>();
        Map<String, List<ExcelCobranzaNormalizado>> mapaTipoVehiculo = new HashMap<>();

        for (ExcelCobranzaNormalizado e : excelCobranzas) {

            mapaRut.computeIfAbsent(e.getRut(), k -> new ArrayList<>()).add(e);

            String patente = e.getPlacaPatente() == null ? "SIN_PATENTE" : e.getPlacaPatente();
            mapaPatente.computeIfAbsent(patente, k -> new ArrayList<>()).add(e);

            mapaTipoVehiculo.computeIfAbsent(e.getTipoVehiculo(), k -> new ArrayList<>()).add(e);
        }

        Reporte reporte = new Reporte(
                procesoGen,
                proceso,
                String.valueOf(numFolio - 1),
                String.valueOf(mapaRut.size()),
                String.valueOf(mapaPatente.size()),
                String.valueOf(mapaTipoVehiculo.size())
        );

        String html = PlantillaCobranzas.generarPlantillaReporteCobranzas(
                apiProperties.getArchivoHtmlNombreCarpetaTemplate()
                        + apiProperties.getArchivoHtmlNombreArchivoReporte(),
                apiProperties.getArchivoHtmlNombreCarpetaTemplate()
                        + apiProperties.getArchivoHtmlLogo(),
                reporte
        );

        byte[] pdf = plantillaService.generarPdffromHtml(html);

        String rutaReporte =
                apiProperties.getArchivoCreacionCarpeta()
                        + apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranza()
                        + apiProperties.getArchivoCreacionAdjuntoSubCarpetaCobranzaReporte()
                        + ejecutarNotificacion.getUnidad() + "/"
                        + proceso + "/";

        String nombreArchivo = proceso + "_"
                + apiProperties.getArchivoCreacionAdjuntoNombreArchivoReporte();

        plantillaService.guardarPdfIndividual(pdf, rutaReporte, nombreArchivo);

        log.info("Reporte generado: {}", rutaReporte + nombreArchivo);
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

        /***Cargar una vez el String de html---/
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

        /***Cargar una vez el String de html---/
        String htmlIndividual = PlantillaCargar.cargarPlantilla(apiProperties.getArchivoHtmlNombreCarpetaTemplate().concat(
                apiProperties.getArchivoHtmlNombreArchivoCobranzaMasiva()));

        return new CartaHtml(htmlIndividual, Arrays.asList(base64Esc, base64Firma));
    }

    public long obtenerCorrelativo() {
        return procesoCartaRepository
                .findTopByTipoCartaOrderByFechaRegistroDesc(TIPO)
                .map(ProcesoCarta::getUltimoCorrelativo)
                .orElse(1L);
    }
}***/
