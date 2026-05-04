package com.ragnax.sanbernardo.notificacion.application.service.utilidades;

import com.ragnax.sanbernardo.notificacion.application.service.model.CartaHtml;
import com.ragnax.sanbernardo.notificacion.application.service.model.EjecutarCartas;
import com.ragnax.sanbernardo.notificacion.application.service.model.ExcelCobranza;
import com.ragnax.sanbernardo.notificacion.application.service.model.ExcelCobranzaMerge;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class PlantillaCobranzas {

    public static String generarPlantillaCobranzaIndividual(String proc_correlativo, String contFolioProceso, CartaHtml cartaHtmlIndividual,
                                                            ExcelCobranzaMerge excelCobranzaMerges) {

        DateTimeFormatter entradaFormat = DateTimeFormatter.ofPattern("M/d/yy");

        // Convertimos el String a un objeto LocalDate
        LocalDate fecha = LocalDate.parse(excelCobranzaMerges.getFechaCitacion(), entradaFormat);

        // Definimos el formato de salida deseado
        DateTimeFormatter salidaFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Resultado
        String fechaCitacion = fecha.format(salidaFormat);
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

        htmlReemplazado = htmlReemplazado.replace("{{RUT}}", excelCobranzaMerges.getRut());

        htmlReemplazado = htmlReemplazado.replace("{{DV}}", excelCobranzaMerges.getDv());

        htmlReemplazado = htmlReemplazado.replace("{{NOMBRES}}", excelCobranzaMerges.getNombres());

        htmlReemplazado = htmlReemplazado.replace("{{APELLIDO_PATERNO}}", excelCobranzaMerges.getApellidoPaterno());

        htmlReemplazado = htmlReemplazado.replace("{{APELLIDO_MATERNO}}", excelCobranzaMerges.getApellidoMaterno());

        htmlReemplazado = htmlReemplazado.replace("{{DIRECCION}}", excelCobranzaMerges.getDireccion());

        htmlReemplazado = htmlReemplazado.replace("{{COMUNA}}", excelCobranzaMerges.getComuna());

        htmlReemplazado = htmlReemplazado.replace("{{TIPO_VEHICULO}}", excelCobranzaMerges.getTipoVehiculo());

        if(excelCobranzaMerges.getTipoVehiculo().equalsIgnoreCase("Moto")){
            htmlReemplazado = htmlReemplazado.replace("{{PATENTE}}", excelCobranzaMerges.getPlacaPatente());

        }else{
            htmlReemplazado = htmlReemplazado.replace("{{PATENTE}}", excelCobranzaMerges.getPlacaPatente().concat("-").concat(excelCobranzaMerges.getDg()));
        }

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_INFRACCION}}", excelCobranzaMerges.getFechaInfraccion());

        htmlReemplazado = htmlReemplazado.replace("{{HORA_INFRACCION}}", excelCobranzaMerges.getHoraInfraccion());

        htmlReemplazado = htmlReemplazado.replace("{{FOLIO}}", excelCobranzaMerges.getFolio());

        String valorMulta = String.format("%,d", Integer.parseInt(excelCobranzaMerges.getValorMulta())).replace(",",".");

        htmlReemplazado = htmlReemplazado.replace("{{VALOR_MULTA}}", valorMulta);

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_VENCIMIENTO}}", excelCobranzaMerges.getVence());

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CITACION}}", fechaCitacion);

        if(excelCobranzaMerges.getJuzgado().equalsIgnoreCase("PRIMER")){
            htmlReemplazado = htmlReemplazado.replace("{{N_JUZGADO}}", "1<sup>er</sup> JUZGADO");
        }
        else if(excelCobranzaMerges.getJuzgado().equalsIgnoreCase("SEGUNDO")){
            htmlReemplazado = htmlReemplazado.replace("{{N_JUZGADO}}", "2<sup>do</sup> JUZGADO");
        }

        //htmlReemplazado = htmlReemplazado.replace("{{PISO}}",  "1&deg; Piso");
        htmlReemplazado = htmlReemplazado.replace("{{PISO}}", excelCobranzaMerges.getPiso().concat(" Piso"));

        return htmlReemplazado;
    }

    public static String generarPlantillaCobranzaMasiva(String proc_correlativo,
                                                        String contFolioProceso,
                                                        CartaHtml cartaHtmlMasiva,
                                                        ExcelCobranza excelCobranza,
                                                        List<ExcelCobranzaMerge> listaExcelCobranza) throws Exception {

        DateTimeFormatter entradaFormat = DateTimeFormatter.ofPattern("M/d/yy");

        // Convertimos el String a un objeto LocalDate
        LocalDate fecha = LocalDate.parse(excelCobranza.getFechaCitacion(), entradaFormat);

        // Definimos el formato de salida deseado
        DateTimeFormatter salidaFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Resultado
        String fechaCitacion = fecha.format(salidaFormat);

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

        htmlReemplazado = htmlReemplazado.replace("{{FECHA_CITACION}}", fechaCitacion);

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

    public static String generarPlantillaReporteCobranzas(String lecturaHtml, String archivoHtmlLogo, EjecutarCartas ejecutarCartas) throws Exception {

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
        html = html.replace("{{totalFilasGeneradasUpload}}", String.valueOf(ejecutarCartas.getSizeArchivoUpload()));

        // Si tienes estos campos en tu objeto ejecutarCartas, los mapeamos así:
        // Nota: Asegúrate de que los nombres coincidan con los getters de tu clase

        html = html.replace("{{observacion}}", ejecutarCartas.getObservacion());
        html = html.replace("{{unidad}}", ejecutarCartas.getUnidad().toUpperCase());
        html = html.replace("{{usuarioUpload}}", ejecutarCartas.getUsuarioUpload().toUpperCase());
        html = html.replace("{{usuarioMerge}}", ejecutarCartas.getUsuarioMerge().toUpperCase());

        html = html.replace("{{SIZE_EXCEL_NORMALIZADO}}", String.valueOf(ejecutarCartas.getSizeArchivoNormalizado()));
        html = html.replace("{{SIZE_CSV_CORREOS}}", String.valueOf(ejecutarCartas.getRegistrosUnicos()));
        html = html.replace("{{SIZE_EXCEL_MERGE}}", String.valueOf(ejecutarCartas.getSizeArchivoMerge()));


        try{
            //Cuantas llegaron - Cuantas se agruparon en masivas
            html = html.replace("{{SIZE_AHORRO}}", String.valueOf(Integer.parseInt(ejecutarCartas.getSizeArchivoUpload())-Integer.parseInt(ejecutarCartas.getRegistrosUnicos())));
        }catch(Exception e){
            html = html.replace("{{SIZE_AHORRO}}", "");
        }

        try{
            //Cuantas llegaron - Cuantas hay en el merge antes de las catas
            html = html.replace("{{SIZE_EXCEL_ERRORES}}", String.valueOf(Integer.parseInt(ejecutarCartas.getSizeArchivoUpload())-Integer.parseInt(ejecutarCartas.getSizeArchivoMerge())));
        }catch(Exception e){
            html = html.replace("{{SIZE_EXCEL_ERRORES}}", "");
        }

        html = html.replace("{{totalMasivas}}", String.valueOf(ejecutarCartas.getTotalMasivas()));
        html = html.replace("{{totalErroneas}}", String.valueOf(ejecutarCartas.getTotalErroneas()));

        // Estos dos no tenían {{}} en tu HTML de ejemplo, pero los agregamos aquí:
        html = html.replace("{{totalCartas}}", String.valueOf(ejecutarCartas.getTotalCartas()));
        html = html.replace("{{totalIndividuales}}", String.valueOf(ejecutarCartas.getTotalIndividuales()));

        // 4. Reemplazo de fecha y footer
        // Asumiendo que tienes un método que devuelve la fecha formateada
        html = html.replace("{{FECHA_AHORA_Text}}", fechaFormateada);

        // Opcional: Reemplazar el LOGO si lo pusiste en alguna parte del body
        html = html.replace("{{LOGO}}", imgTag);

        return html;
    }
}
