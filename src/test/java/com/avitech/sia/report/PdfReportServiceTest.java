package com.avitech.sia.report;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PdfReportServiceTest {

    @Test
    void generate_stock_actual_pdf() throws Exception {
        PdfReportService svc = new PdfReportService();
        ReportRequest req = new ReportRequest(
                ReportType.STOCK_ACTUAL,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
        Path out = svc.generate(req);
        assertTrue(Files.exists(out), "El PDF de stock debe existir");
        assertTrue(Files.size(out) > 0, "El PDF de stock no debe estar vacío");
    }

    @Test
    void generate_registro_articulo_pdf() throws Exception {
        PdfReportService svc = new PdfReportService();
        ReportRequest req = new ReportRequest(
                ReportType.REGISTRO_ARTICULO,
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                null,
                null,
                null,
                null,
                false
        );
        Path out = svc.generate(req);
        assertTrue(Files.exists(out), "El PDF de registro debe existir");
        assertTrue(Files.size(out) > 0, "El PDF de registro no debe estar vacío");
    }

    @Test
    void generate_recibos_insumos_pdf() throws Exception {
        PdfReportService svc = new PdfReportService();
        ReportRequest req = new ReportRequest(
                ReportType.RECIBOS_INSUMOS,
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                null,
                null,
                null,
                null,
                false
        );
        Path out = svc.generate(req);
        assertTrue(Files.exists(out), "El PDF de recibos debe existir");
        assertTrue(Files.size(out) > 0, "El PDF de recibos no debe estar vacío");
    }
}

