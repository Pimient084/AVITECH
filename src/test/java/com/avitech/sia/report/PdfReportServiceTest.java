package com.avitech.sia.report;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfReportServiceTest {

    @Test
    void generate_produccion_pdf() throws Exception {
        PdfReportService svc = new PdfReportService();
        ReportRequest req = new ReportRequest(
                ReportType.PRODUCCION,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
        Path out = svc.generate(req);
        assertTrue(Files.exists(out), "El PDF de producción debe existir");
        assertTrue(Files.size(out) > 0, "El PDF de producción no debe estar vacío");
    }

    @Test
    void generate_usuarios_pdf() throws Exception {
        PdfReportService svc = new PdfReportService();
        ReportRequest req = new ReportRequest(
                ReportType.USUARIOS,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
        Path out = svc.generate(req);
        assertTrue(Files.exists(out), "El PDF de usuarios debe existir");
        assertTrue(Files.size(out) > 0, "El PDF de usuarios no debe estar vacío");
    }

    @Test
    void generate_alertas_pdf() throws Exception {
        PdfReportService svc = new PdfReportService();
        ReportRequest req = new ReportRequest(
                ReportType.ALERTAS,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
        Path out = svc.generate(req);
        assertTrue(Files.exists(out), "El PDF de alertas debe existir");
        assertTrue(Files.size(out) > 0, "El PDF de alertas no debe estar vacío");
    }
}
