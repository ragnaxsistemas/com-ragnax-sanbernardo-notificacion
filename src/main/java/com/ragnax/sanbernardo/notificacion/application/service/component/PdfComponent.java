package com.ragnax.sanbernardo.notificacion.application.service.component;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.barcodes.BarcodeEAN;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.utils.PdfMerger;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class PdfComponent {

    public byte[] generarPdffromHtmlCobranzaConBarcode(
            String html,
            String textoBarcode,
            String textCode128C) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 🔥 Compresión FULL
        WriterProperties writerProps = new WriterProperties();
        writerProps.setFullCompressionMode(true);

        PdfWriter writer = new PdfWriter(out, writerProps);
        PdfDocument pdfDoc = new PdfDocument(writer);

        // 🔥 Fuente reutilizada (NO crear varias veces)
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // 🔥 HTML optimizado
        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setCharset("utf-8");

        Document document = HtmlConverter.convertToDocument(
                html,
                pdfDoc,
                converterProperties
        );

        // ================================
        // 🔵 CODE 128 (REUTILIZADO)
        // ================================
        Barcode128 barcode = new Barcode128(pdfDoc);
        barcode.setCode(textoBarcode);
        barcode.setX(0.5f);          // 🔥 más liviano
        barcode.setBarHeight(15f);   // 🔥 reduce peso
        barcode.setFont(font);
        barcode.setSize(5);

        // 🔥 CREAR UNA SOLA VEZ
        PdfFormXObject barcodeObject = barcode.createFormXObject(pdfDoc);

        Image barcodeImage1 = new Image(barcodeObject);
        Image barcodeImage2 = new Image(barcodeObject);

        barcodeImage1.setFixedPosition(1, 80, 44);
        barcodeImage2.setFixedPosition(1, 357, 44);

        barcodeImage1.scale(0.8f, 0.8f);
        barcodeImage2.scale(0.8f, 0.8f);

        // ================================
        // 🔴 EAN13
        // ================================
        BarcodeEAN barcodeEanC = new BarcodeEAN(pdfDoc);
        barcodeEanC.setCodeType(BarcodeEAN.EAN13);
        barcodeEanC.setCode(textCode128C);
        barcodeEanC.setX(0.7f);
        barcodeEanC.setBarHeight(25f);
        barcodeEanC.setFont(font);
        barcodeEanC.setSize(7);

        PdfFormXObject objC = barcodeEanC.createFormXObject(pdfDoc);

        float barcodeWidth = objC.getWidth();
        float barcodeHeight = objC.getHeight();

        PdfPage page = pdfDoc.getPage(1);
        PdfCanvas pdfCanvas = new PdfCanvas(page);

        float topX = 488;
        float topY = 700;

        // 🔥 Dibujar EAN13
        pdfCanvas.addXObjectAt(objC, topX, topY);

        // ================================
        // 🟢 TEXTO "CARTA CERTIFICADA"
        // ================================
        float textY = topY + barcodeHeight + 3;

        Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());
        canvas.setFont(font);
        canvas.setFontSize(8);

        canvas.showTextAligned(
                "CARTA CERTIFICADA",
                topX + (barcodeWidth / 2),
                textY,
                TextAlignment.CENTER
        );

        canvas.close();

        // ================================
        // 🔵 Agregar al documento
        // ================================
        document.add(barcodeImage1);
        document.add(barcodeImage2);

        document.close();

        return out.toByteArray();
    }

    /***Este es solo el codigo*/
    public byte[] generarPdffromHtmlCodeEan(String html, String textCode128C) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 🔥 Compresión máxima
        WriterProperties writerProps = new WriterProperties()
                .setFullCompressionMode(true)
                .setCompressionLevel(9);

        PdfWriter writer = new PdfWriter(out, writerProps);
        PdfDocument pdfDoc = new PdfDocument(writer);

        // 🔥 Fuente reutilizada (clave)
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // 🔥 HTML liviano
        ConverterProperties converterProperties = new ConverterProperties();
        converterProperties.setCharset("utf-8");

        String basePath = "file:" + new File("src/main/resources/templates/").getAbsolutePath() + "/";
        converterProperties.setBaseUri(basePath);

        Document document = HtmlConverter.convertToDocument(
                html,
                pdfDoc,
                converterProperties
        );

        // ================================
        // 🔴 EAN13 OPTIMIZADO
        // ================================
        BarcodeEAN barcodeEanC = new BarcodeEAN(pdfDoc);
        barcodeEanC.setCodeType(BarcodeEAN.EAN13);
        barcodeEanC.setCode(textCode128C);

        // 🔥 Ajustes para reducir peso
        barcodeEanC.setX(0.6f);         // barras más finas
        barcodeEanC.setBarHeight(25f);  // menor altura
        barcodeEanC.setFont(font);
        barcodeEanC.setSize(7);

        // 🔥 Crear una sola vez
        PdfFormXObject objC = barcodeEanC.createFormXObject(pdfDoc);

        float barcodeWidth = objC.getWidth();
        float barcodeHeight = objC.getHeight();

        // Página
        PdfPage page = pdfDoc.getPage(1);
        PdfCanvas pdfCanvas = new PdfCanvas(page);

        // Posición
        float topX = 488;
        float topY = 700;

        // 🔥 Dibujar barcode
        pdfCanvas.addXObjectAt(objC, topX, topY);

        // ================================
        // 🟢 TEXTO ARRIBA
        // ================================
        float textY = topY + barcodeHeight + 3;

        Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());
        canvas.setFont(font);
        canvas.setFontSize(8);

        canvas.showTextAligned(
                "CARTA CERTIFICADA",
                topX + (barcodeWidth / 2),
                textY,
                TextAlignment.CENTER
        );

        canvas.close();

        document.close();

        return out.toByteArray();
    }

    public byte[] generarPdffromHtmlCodeEanV2(String html, String textCode128C) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdfDoc = new PdfDocument(writer);

        ConverterProperties properties = new ConverterProperties();

        // ✅ Convertir HTML correctamente (mantiene documento abierto)
        Document document = HtmlConverter.convertToDocument(html, pdfDoc, properties);

        // 🔥 Forzar render del contenido HTML
        document.flush();

        // ✅ Validar que existan páginas
        if (pdfDoc.getNumberOfPages() > 0) {

            // ================================
            // 📌 CREAR CÓDIGO DE BARRAS
            // ================================
            BarcodeEAN barcode = new BarcodeEAN(pdfDoc);
            barcode.setCodeType(BarcodeEAN.EAN13);
            barcode.setCode(textCode128C);

            barcode.setX(0.8f);
            barcode.setBarHeight(30f);
            barcode.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));
            barcode.setSize(8);

            PdfFormXObject barcodeObject = barcode.createFormXObject(pdfDoc);

            float barcodeWidth = barcodeObject.getWidth();
            float barcodeHeight = barcodeObject.getHeight();

            // ================================
            // 📌 POSICIÓN (AJUSTABLE)
            // ================================
            float x = 485;
            float y = 700;

            PdfPage page = pdfDoc.getPage(1);
            PdfCanvas pdfCanvas = new PdfCanvas(page);

            // 🔥 Dibujar código de barras
            pdfCanvas.addXObjectAt(barcodeObject, x, y);

            // ================================
            // 📌 TEXTO "CARTA CERTIFICADA"
            // ================================
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            Canvas canvas = new Canvas(pdfCanvas, page.getPageSize());
            float textY = y + barcodeHeight + 3;

            canvas.setFont(font);
            canvas.setFontSize(8);

            canvas.showTextAligned(
                    "CARTA CERTIFICADA",
                    x + (barcodeWidth / 2),
                    textY,
                    TextAlignment.CENTER
            );

            canvas.close();
        }

        // ✅ Cerrar correctamente
        document.close();

        return out.toByteArray();
    }

    /***Este es para Masiva*/
    public byte[] generarPdffromHtml(String html) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // 2. Convertimos el HTML directamente al stream
        // No es necesario crear un PdfWriter manualmente para casos simples
        HtmlConverter.convertToPdf(html, out);

        // 3. Retornamos el arreglo de bytes
        return out.toByteArray();
    }


    public String guardarPdfIndividual(byte[] pdfBytes,
                                     String rutaCarpeta,
                                     String nombreArchivo) throws Exception {

        Path carpeta = Paths.get(rutaCarpeta);

        if (!Files.exists(carpeta)) {
            Files.createDirectories(carpeta);
        }

        Path archivo = carpeta.resolve(nombreArchivo);

        try (OutputStream os = Files.newOutputStream(archivo)) {
            os.write(pdfBytes);
        }
        return String.valueOf(archivo.getParent()).concat("/").concat(String.valueOf(archivo.getFileName()));
    }

    public void unirPdfs(String dirNombreFinalConsolidadPdf, List<byte[]> listaPdfs) throws Exception {

        log.info("unirPdfs procesados: {} nombre: {}", listaPdfs.size(), dirNombreFinalConsolidadPdf);

        File archivoFinal = new File(dirNombreFinalConsolidadPdf);

        File directorio = archivoFinal.getParentFile();
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        try (OutputStream os = new FileOutputStream(archivoFinal)) {
            mergePdfs(listaPdfs, os);
        }
    }

    public void unirPdfsV2(String nombreFinalConsolidadPdf, List<String> rutasPdfs) throws Exception {

        log.info("unirPdfs procesados: {} nombre: {}", rutasPdfs.size(), nombreFinalConsolidadPdf);

        File archivoFinal = new File(nombreFinalConsolidadPdf);

        File directorio = archivoFinal.getParentFile();
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        try (OutputStream os = new FileOutputStream(archivoFinal)) {
            mergePdfsDesdeArchivos(rutasPdfs, os);
        }
    }

    public void mergePdfs(List<byte[]> pdfs, OutputStream outputStream) throws Exception {
        // 1. Creamos el documento de destino vinculado al OutputStream
        PdfDocument pdfDest = new PdfDocument(new PdfWriter(outputStream));

        // 2. Creamos la herramienta auxiliar PdfMerger
        PdfMerger merger = new PdfMerger(pdfDest);

        for (byte[] pdfBytes : pdfs) {
            // 3. Leemos cada PDF de la lista
            PdfDocument sourcePdf = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)));

            // 4. Agregamos todas las páginas del origen al destino
            merger.merge(sourcePdf, 1, sourcePdf.getNumberOfPages());

            // 5. Cerramos el documento de origen para liberar memoria
            sourcePdf.close();
        }

        // 6. Cerramos el documento de destino (esto escribe el archivo final)
        pdfDest.close();
    }

    public void mergePdfsDesdeArchivos(List<String> rutasPdfs, OutputStream outputStream) throws Exception {

        PdfDocument pdfFinal = new PdfDocument(new PdfWriter(outputStream));
        PdfMerger merger = new PdfMerger(pdfFinal);

        for (String ruta : rutasPdfs) {

            File file = new File(ruta);

            if (!file.exists()) {
                log.warn("Archivo no existe: {}", ruta);
                continue;
            }

            try (PdfDocument pdfOrigen = new PdfDocument(new PdfReader(ruta))) {

                merger.merge(pdfOrigen, 1, pdfOrigen.getNumberOfPages());

            } catch (Exception e) {
                log.error("Error uniendo PDF: {}", ruta, e);
            }
        }

        pdfFinal.close();
    }
}
