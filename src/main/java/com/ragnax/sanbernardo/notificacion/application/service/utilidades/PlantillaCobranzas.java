package com.ragnax.sanbernardo.notificacion.application.service.utilidades;

import com.ragnax.sanbernardo.notificacion.application.service.model.*;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class PlantillaCobranzas {

    public static String generarPlantillaCobranzaIndividual(String proc_correlativo, String contFolioProceso, CartaHtml cartaHtmlIndividual,
                                                            ExcelCobranzaNormalizado excelCobranza) {

        //Escudo
        String imgTag1 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(0) + "' style='width: 60px; height: auto;'/>";

        //Firma
        String imgTag2 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(1) + "' style='width: 180px; height: auto; display: block; margin: 0 auto;'/>";

        //Logo BCI
        String imgTag3 = "<img src='data:image/png;base64," + cartaHtmlIndividual.getListImagesBase64().get(2) + "' style='width: 60px; height: auto; display: block; margin-bottom: 4px;'/>";

        String htmlReemplazado = cartaHtmlIndividual.getHtml().replace("{{CORRELATIVO}}", proc_correlativo);

        htmlReemplazado = htmlReemplazado.replace("{{FOLIO_IMPRESION}}", contFolioProceso);

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_1}}", imgTag1);

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_2}}", imgTag2);

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_3}}", imgTag3);

        htmlReemplazado = htmlReemplazado.replace("{{RUT}}", excelCobranza.getRut());

        htmlReemplazado = htmlReemplazado.replace("{{DV}}", excelCobranza.getDv());

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRES}}", excelCobranza.getNombres());

        htmlReemplazado = htmlReemplazado.replace("{{APELLIDO_PATERNO}}", excelCobranza.getApellidoPaterno());

        htmlReemplazado = htmlReemplazado.replace("{{APELLIDO_MATERNO}}", excelCobranza.getApellidoMaterno());

        htmlReemplazado = htmlReemplazado.replace("{{DIRECCION}}", excelCobranza.getDireccion());

        htmlReemplazado = htmlReemplazado.replace("{{COMUNA}}", excelCobranza.getComuna());

        htmlReemplazado = htmlReemplazado.replace("{{TIPO_VEHICULO}}", excelCobranza.getTipoVehiculo());

        htmlReemplazado = htmlReemplazado.replace("{{PISO}}",  "1&deg; Piso");

        if(excelCobranza.getTipoVehiculo().equalsIgnoreCase("Moto")){
            htmlReemplazado = htmlReemplazado.replace("{{PATENTE}}", excelCobranza.getPlacaPatente());

        }else{
            htmlReemplazado = htmlReemplazado.replace("{{PATENTE}}", excelCobranza.getPlacaPatente().concat("-").concat(excelCobranza.getDg()));
        }

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_INFRACCION}}", excelCobranza.getFechaInfraccion());

        htmlReemplazado = htmlReemplazado.replace("{{HORA_INFRACCION}}", excelCobranza.getHoraInfraccion());

        htmlReemplazado = htmlReemplazado.replace("{{FOLIO}}", excelCobranza.getFolio());

        String valorMulta = String.format("%,d", Integer.parseInt(excelCobranza.getValorMulta())).replace(",",".");

        htmlReemplazado = htmlReemplazado.replace("{{VALOR_MULTA}}", valorMulta);

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_VENCIMIENTO}}", excelCobranza.getVence());

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CITACION}}", excelCobranza.getFechaCitacion());

        if(excelCobranza.getJuzgado().equalsIgnoreCase("PRIMER")){
            htmlReemplazado = htmlReemplazado.replace("{{N_JUZGADO}}", "1<sup>er</sup> JUZGADO");
        }
        else if(excelCobranza.getJuzgado().equalsIgnoreCase("SEGUNDO")){
            htmlReemplazado = htmlReemplazado.replace("{{N_JUZGADO}}", "2<sup>do</sup> JUZGADO");
        }

        htmlReemplazado = htmlReemplazado.replace("{{PISO}}", excelCobranza.getPiso().concat(" Piso"));

        return htmlReemplazado;
    }

    public static String generarPlantillaCobranzaMasiva(String proc_correlativo,
                                                        String contFolioProceso,
                                                        CartaHtml cartaHtmlMasiva,
                                                        ExcelCobranza excelCobranza,
                                                        List<ExcelCobranzaNormalizado> listaExcelCobranza) throws Exception {


        //Escudo
        String imgTag1 = "<img src='data:image/png;base64," + cartaHtmlMasiva.getListImagesBase64().get(0) + "' style='width: 60px; height: auto;'/>";

        //Firma
        String imgTag2 = "<img src='data:image/png;base64," + cartaHtmlMasiva.getListImagesBase64().get(1) + "' style='width: 180px; height: auto;'/>";

        String htmlReemplazado = cartaHtmlMasiva.getHtml().replace("{{CORRELATIVO}}", proc_correlativo);

        htmlReemplazado = htmlReemplazado.replace("{{FOLIO_IMPRESION}}", contFolioProceso);

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_1}}", imgTag1);

        htmlReemplazado = htmlReemplazado.replace("{{LOGO_2}}", imgTag2);

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRES}}", excelCobranza.getNombres());

        htmlReemplazado = htmlReemplazado.replace("{{APELLIDO_PATERNO}}", excelCobranza.getApellidoPaterno());

        htmlReemplazado = htmlReemplazado.replace("{{APELLIDO_MATERNO}}", excelCobranza.getApellidoMaterno());

        htmlReemplazado = htmlReemplazado.replace("{{DIRECCION}}", excelCobranza.getDireccion());

        htmlReemplazado = htmlReemplazado.replace("{{COMUNA}}", excelCobranza.getComuna());

        htmlReemplazado = htmlReemplazado.replace("{{RUT}}", excelCobranza.getRut());

        htmlReemplazado = htmlReemplazado.replace("{{DV}}", excelCobranza.getDv());

        htmlReemplazado = htmlReemplazado.replace("{{TIPO_VEHICULO}}", excelCobranza.getTipoVehiculo());

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CITACION}}", excelCobranza.getFechaCitacion());

        if(excelCobranza.getJuzgado().equalsIgnoreCase("PRIMER")){
            htmlReemplazado = htmlReemplazado.replace("{{N_JUZGADO}}", "1<sup>er</sup> JUZGADO");
        }
        else if(excelCobranza.getJuzgado().equalsIgnoreCase("SEGUNDO")){
            htmlReemplazado = htmlReemplazado.replace("{{N_JUZGADO}}", "2<sup>do</sup> JUZGADO");
        }

        htmlReemplazado = htmlReemplazado.replace("{{PISO}}", excelCobranza.getPiso().concat(" Piso"));
        //htmlReemplazado = htmlReemplazado.replace("{{PISO}}",  "1&deg; Piso");

        if(excelCobranza.getTipoVehiculo().equalsIgnoreCase("Moto")){
            htmlReemplazado = htmlReemplazado.replace("{{PATENTE}}", excelCobranza.getPlacaPatente());

        }else{
            htmlReemplazado = htmlReemplazado.replace("{{PATENTE}}", excelCobranza.getPlacaPatente().concat("-").concat(excelCobranza.getDg()));
        }

        String numFormateado = "";
        String trHml = "";
        for(int i = 0; i< listaExcelCobranza.size(); i++){
            numFormateado = String.format("%,d", Integer.parseInt(listaExcelCobranza.get(i).getValorMulta())).replace(",",".");
            trHml = trHml+"<tr>"+
                    "<td>"+ listaExcelCobranza.get(i).getFolio()+ "</td>"+
                    "<td>"+ listaExcelCobranza.get(i).getFechaInfraccion()+  "</td>"+
                    "<td>"+ listaExcelCobranza.get(i).getHoraInfraccion()+"</td>"+
                    "<td>" +listaExcelCobranza.get(i).getRolMop()+ "</td>"+
                    "<td class=\"lugar-infr\">"+listaExcelCobranza.get(i).getLugarMulta()+"</td>"+
                    "<td>"+ listaExcelCobranza.get(i).getVence()+  "</td>"+
                    "<td>"+ numFormateado + "</td>"+
                    "</tr>";
        }

        htmlReemplazado = htmlReemplazado.replace("{{TABLA_INFRACCIONES}}", trHml);

        return htmlReemplazado;

    }

    public static String formatearRut(String rut) {
        rut = rut.replaceAll("^0+", ""); // quitar ceros izquierda

        String cuerpo = rut.substring(0, rut.length() - 1);
        String dv = rut.substring(rut.length() - 1);

        cuerpo = cuerpo.replaceAll("(\\d)(?=(\\d{3})+(?!\\d))", "$1.");

        return cuerpo + "-" + dv;
    }

    public static String generarPlantillaReporteCobranzas(String lecturaHtml, String archivoHtmlLogo, EjecutarMerge ejecutarMerge) throws Exception {

        // 1. Definir el formateador con localización en español
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

        // 2. Obtener la fecha (Si es un String, primero hay que parsearlo, si ya es Date, convertirlo)
        // Supongamos que capturamos la fecha actual o la del objeto
        String fechaFormateada = LocalDateTime.now().format(formatter);

        // Para que la primera letra del mes sea Mayúscula (Abril)
        fechaFormateada = fechaFormateada.substring(0, 6) +
                fechaFormateada.substring(6, 7).toUpperCase() +
                fechaFormateada.substring(7);

        // 1. Cargar el contenido del HTML desde la ruta/archivo
        String html = PlantillaCargar.cargarPlantilla(lecturaHtml);

        // 2. Procesar el LOGO en Base64
        ClassPathResource imgFile = new ClassPathResource(archivoHtmlLogo);
        byte[] imageBytes;
        try (InputStream is = imgFile.getInputStream()) {
            imageBytes = is.readAllBytes();
        }
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        // Agregamos el marcador {{LOGO}} al HTML si quieres mostrarlo,
        // sino, el banner azul ya cumple la función visual.
        String imgTag = "<img width='120' src='data:image/png;base64," + base64 + "' style='display:block; margin-bottom:10px;'/>";

        // 3. Reemplazos de Datos de la Tabla
        // Usamos String.valueOf o formateo para asegurar que no falle si son null
        html = html.replace("{{totalFilasGeneradasUpload}}", String.valueOf(ejecutarMerge.getSizeArchivoUpload()));

        // Si tienes estos campos en tu objeto ejecutarMerge, los mapeamos así:
        // Nota: Asegúrate de que los nombres coincidan con los getters de tu clase

        html = html.replace("{{observacion}}", String.valueOf(ejecutarMerge.getObservacion()));
        html = html.replace("{{show_unidad_UPPER}}", String.valueOf(ejecutarMerge.getUnidad().toUpperCase()));
        html = html.replace("{{SIZE_CSV_CORREOS}}", String.valueOf(ejecutarMerge.getRegistrosUnicos()));

        html = html.replace("{{totalMasivas}}", String.valueOf(ejecutarMerge.getTotalMasivas()));
        html = html.replace("{{totalErroneas}}", String.valueOf(ejecutarMerge.getTotalErroneas()));

        // Estos dos no tenían {{}} en tu HTML de ejemplo, pero los agregamos aquí:
        html = html.replace("{{totalCartas}}", String.valueOf(ejecutarMerge.getTotalCartas()));
        html = html.replace("{{totalIndividuales}}", String.valueOf(ejecutarMerge.getTotalIndividuales()));

        // 4. Reemplazo de fecha y footer
        // Asumiendo que tienes un método que devuelve la fecha formateada
        html = html.replace("{{FECHA_AHORA_Text}}", fechaFormateada);

        // Opcional: Reemplazar el LOGO si lo pusiste en alguna parte del body
        html = html.replace("{{LOGO}}", imgTag);

        return html;
    }
}
