package com.ragnax.sanbernardo.notificacion.application.service.component;

import com.ragnax.sanbernardo.notificacion.application.service.DProcesarCartaCobranzaService;
import com.ragnax.sanbernardo.notificacion.application.service.DProcesarCartaNotificacionService;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.ExcelCobranzaMerge;
import com.ragnax.sanbernardo.notificacion.application.service.model.ExcelNotificacionMerge;
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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MergeScheduler {

    @Autowired
    private EjecutarProcesoCartaRepository ejecutarProcesoCartaRepository;

    @Autowired
    private DProcesarCartaCobranzaService procesarCartaCobranzaService;

    @Autowired
    private DProcesarCartaNotificacionService procesarCartaNotificacionService;

    @Autowired
    private ApiProperties apiProperties;

    // Ejecuta a las 12:00, 03:00 y 05:00 AM
    @Scheduled(cron = "0 0 0,3,5 * * *")
    // Ejecuta exactamente a las 11:34 AM
    //@Scheduled(cron = "30 30 11 * * *")
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

            procesar(ejecutarMerge, epc);
        }
        log.info(" FIN mergeScheduler");
        // Mapeas de tu entidad al DTO EjecutarMerge que recibe tu método Async
        /***CArgarArchivoExcel**/
    }

    private void procesar(EjecutarMerge ejecutarMerge, EjecutarProcesoCarta ejecutarProcesoCarta) throws Exception {

        String tipo = ejecutarMerge.getTipo();
        File archivoExcel = new File(ejecutarMerge.getPathArchivoMerge());

        // 1. Cláusula de guarda: Validamos primero si el archivo NO existe para salir de inmediato
        if (!archivoExcel.exists()) {
            throw new Exception("No se encontró el archivo Excel en: " + archivoExcel.getAbsolutePath());
        }

        int procesados = 0;
        String nombreHoja = apiProperties.getArchivoExcelNombreHojaMergeCobranza(); //HojaProcesar

        try (InputStream excelInputStream = new FileInputStream(archivoExcel)) {
            log.info("Abriendo Excel: {}", archivoExcel.getAbsolutePath());

            if ("COBRANZA".equalsIgnoreCase(tipo)) {
                List<ExcelCobranzaMerge> excelCobranzasMerge =
                        ObtenerExcel.obtenerExcelCobranzaMerge(excelInputStream, nombreHoja);

                ejecutarMerge.setListaExcelCobranzaMerge(excelCobranzasMerge);
                procesarCartaCobranzaService.processArchivoCobranza(ejecutarMerge);

                procesados = excelCobranzasMerge.size();

            } else if ("NOTIFICACION".equalsIgnoreCase(tipo)) {
                List<ExcelNotificacionMerge> excelNotificacionesMerge =
                        ObtenerExcel.obtenerExcelNotificacionMerge(excelInputStream, nombreHoja);

                ejecutarMerge.setListaExcelNotificacionMerge(excelNotificacionesMerge);
                procesarCartaNotificacionService.processArchivoNotificacion(ejecutarMerge);

                procesados = excelNotificacionesMerge.size();
            }

            // 3. Acciones comunes post-procesamiento
            ejecutarProcesoCarta.setEjecutado(true);

            log.info("Ejecucion de Proceso carta: {} - {}",
                    ejecutarProcesoCarta.getCodEjecutarProcesoCarta(), ejecutarProcesoCarta.getPathArchivoMerge());

            // Guardar el estado del proceso en la base de datos
            ejecutarProcesoCartaRepository.save(ejecutarProcesoCarta);

            log.info("Resultado procesamiento de registros: {}", procesados);

        } catch (Exception e) {
            log.error("❌ Error interno al procesar el archivo Excel del tipo {}: {}", tipo, e.getMessage(), e);
            throw new RuntimeException("Error en el procesamiento del archivo: " + e.getMessage(), e);
        }
    }

}