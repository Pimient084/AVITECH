package com.avitech.sia.report;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class RecentReportsService {

    public record RecentReport(Path path, String name, long size, Instant modified) {}

    private Path outDir() {
        // Mismo directorio que PdfReportService
        return Paths.get("build", "reports", "pdf");
    }

    public List<RecentReport> listRecent(int limit) {
        Path dir = outDir();
        if (!Files.exists(dir)) return List.of();
        List<RecentReport> out = new ArrayList<>();
        try (Stream<Path> st = Files.list(dir)) {
            st.filter(p -> Files.isRegularFile(p))
              .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
              .forEach(p -> {
                  try {
                      BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
                      out.add(new RecentReport(
                              p,
                              p.getFileName().toString(),
                              attr.size(),
                              attr.lastModifiedTime().toInstant()
                      ));
                  } catch (IOException ignore) { }
              });
        } catch (IOException ignore) { }
        out.sort(Comparator.comparing(RecentReport::modified).reversed());
        if (limit > 0 && out.size() > limit) return out.subList(0, limit);
        return out;
    }
}

