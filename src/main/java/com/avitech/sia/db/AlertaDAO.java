package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {

    public record Alerta(int id, String tipo, String descripcion, String categoria, LocalDateTime fecha, int idItem, String estado) {}

    public int getActiveLowStockAlertsCount() throws Exception {
        String sql = "SELECT COUNT(*) FROM Alertas WHERE estado = 'Activa' AND categoria = 'Stock' AND tipo = 'Bajo'";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<String> getRecentAlertsDescriptions(int limit) throws Exception {
        List<String> alerts = new ArrayList<>();
        String sql = "SELECT descripcion, fecha FROM Alertas ORDER BY fecha DESC LIMIT ?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alerts.add(rs.getString("descripcion") + " · " + rs.getTimestamp("fecha").toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        }
        return alerts;
    }
}
