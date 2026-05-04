package com.ragnax.sanbernardo.notificacion.application.service.component;

import com.ragnax.sanbernardo.notificacion.application.service.DProcesarCartaCobranzaService;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.ExcelCobranzaMerge;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.CrearJsonExcel;
import com.ragnax.sanbernardo.notificacion.application.service.utilidades.ObtenerExcel;
import com.ragnax.sanbernardo.notificacion.infraestructura.configuration.ApiProperties;
import com.ragnax.sanbernardo.notificacion.infraestructura.entity.EjecutarProcesoCarta;
import com.ragnax.sanbernardo.notificacion.infraestructura.repository.EjecutarProcesoCartaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MergeScheduler {

    @Autowired
    private EjecutarProcesoCartaRepository ejecutarProcesoCartaRepository;

    @Autowired
    private DProcesarCartaCobranzaService procesarCartaCobranzaService;

    @Autowired
    private ApiProperties apiProperties;

    // Ejecuta a las 12:00, 03:00 y 05:00 AM
    @Scheduled(cron = "0 0 0,3,5 * * *")
    // Ejecuta exactamente a las 11:34 AM
    //@Scheduled(cron = "0 30 13 * * *")
    public void procesarPendientesCadaHora() throws Exception {
        log.info("⏰ Iniciando revisión horaria de procesos pendientes...");

        // Buscamos en la BD los que no han sido ejecutados aún
        List<EjecutarProcesoCarta> pendientes = ejecutarProcesoCartaRepository.
                findByEjecutado(false);

        if (pendientes.isEmpty()) {
            log.info("☕ No hay procesos pendientes por ejecutar.");
            return;
        }

        for(EjecutarProcesoCarta epc: pendientes) {
            log.info("🚀 Programando ejecución asíncrona para: {}", epc.getCodEjecutarProcesoCarta());

            EjecutarMerge ejecutarMerge = CrearJsonExcel.getEjecutarMergeFromJson(epc.getPathArchivoMerge());

            log.info("EjecutarMerge : {}", ejecutarMerge);

            procesar(ejecutarMerge, epc);
        }
        // Mapeas de tu entidad al DTO EjecutarMerge que recibe tu método Async
        /***CArgarArchivoExcel**/
    }

    private void procesar(EjecutarMerge ejecutarMerge, EjecutarProcesoCarta ejecutarProcesoCarta) throws Exception {

        //por el tipo realizar llamada a procesar
        if (ejecutarMerge.getTipo().equalsIgnoreCase("COBRANZA")) {

            File archivoExcel = new File(ejecutarMerge.getPathArchivoMerge());

            List<ExcelCobranzaMerge> excelCobranzaNormalizado = new ArrayList<>();

            if (archivoExcel.exists()) {
                try (InputStream excelInputStream = new FileInputStream(archivoExcel)) {

                    log.info("Abriendo Excel: " + archivoExcel.getAbsolutePath());

                    excelCobranzaNormalizado =
                            ObtenerExcel.obtenerExcelCobranzaMerge(excelInputStream, apiProperties.getArchivoExcelNombreHojaMergeCobranza());

                    ejecutarMerge.setListaExcelCobranzaMerge(excelCobranzaNormalizado);

                    procesarCartaCobranzaService.processArchivoCobranza(ejecutarMerge);

                    ejecutarProcesoCarta.setEjecutado(true);

                    log.info("Ejecucion de Proceso carta: {} - {}", ejecutarProcesoCarta.getCodEjecutarProcesoCarta(), ejecutarProcesoCarta.getPathArchivoMerge());

                    ejecutarProcesoCartaRepository.
                            save(ejecutarProcesoCarta);
                    //Salvar el roceso y guardar en processArchivoCobranza
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new Exception("No se encontró el archivo Excel en: " + archivoExcel.getAbsolutePath());
            }
            log.info(" resultadoValidacion {}", excelCobranzaNormalizado.size());
        }
    }
}