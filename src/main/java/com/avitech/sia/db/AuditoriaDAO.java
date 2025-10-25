package com.avitech.sia.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    public record AuditoriaRecord(
            int id,
            int idUsuario,
            LocalDateTime fecha,
            String accion,
            String modulo,
            String detalle,
            String referencia) {}

    public void insert(int idUsuario, String accion, String modulo, String detalle, String referencia) throws Exception {
        String sql = "INSERT INTO Auditoria (id_usuario, accion, modulo, detalle, referencia) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, accion);
            ps.setString(3, modulo);
            ps.setString(4, detalle);
            ps.setString(5, referencia);
            ps.executeUpdate();
        }
    }

    public List<AuditoriaRecord> getAll() throws Exception {
        List<AuditoriaRecord> records = new ArrayList<>();
        String sql = "SELECT id_auditoria, id_usuario, fecha, accion, modulo, detalle, referencia FROM Auditoria ORDER BY fecha DESC";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                records.add(new AuditoriaRecord(
                        rs.getInt("id_auditoria"),
                        rs.getInt("id_usuario"),
                        rs.getTimestamp("fecha").toLocalDateTime(),
                        rs.getString("accion"),
                        rs.getString("modulo"),
                        rs.getString("detalle"),
                        rs.getString("referencia")
                ));
            }
        }
        return records;
    }

    // You can add more methods here for filtering, searching, etc.
}
