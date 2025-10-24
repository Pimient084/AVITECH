package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalponDAO {

    public record GalponSummary(String nombre, int capacidad, int currentProduction) {}

    public List<String> getActiveGalpones() throws Exception {
        List<String> galpones = new ArrayList<>();
        // Assuming 'Vista_Estado_Galpones' provides the necessary information
        String sql = "SELECT nombre_galpon FROM Vista_Estado_Galpones WHERE estado_lote = 'Activo' ORDER BY nombre_galpon";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                galpones.add(rs.getString("nombre_galpon"));
            }
        }
        return galpones;
    }

    public Map<Integer, String> getGalponMap() throws Exception {
        Map<Integer, String> galponMap = new HashMap<>();
        String sql = "SELECT id_galpon, nombre FROM Galpones";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                galponMap.put(rs.getInt("id_galpon"), rs.getString("nombre"));
            }
        }
        return galponMap;
    }

    public Map<Integer, GalponSummary> getGalponProductionSummary(LocalDate date) throws Exception {
        Map<Integer, GalponSummary> summaryMap = new HashMap<>();
        String sql = "SELECT g.id_galpon, g.nombre, g.capacidad, COALESCE(SUM(ph.total_huevos), 0) AS current_production " +
                     "FROM Galpones g " +
                     "LEFT JOIN ProduccionHuevos ph ON g.id_galpon = ph.galpon AND ph.fecha = ? " +
                     "GROUP BY g.id_galpon, g.nombre, g.capacidad " +
                     "ORDER BY g.id_galpon";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id_galpon");
                    String nombre = rs.getString("nombre");
                    int capacidad = rs.getInt("capacidad");
                    int currentProduction = rs.getInt("current_production");
                    summaryMap.put(id, new GalponSummary(nombre, capacidad, currentProduction));
                }
            }
        }
        return summaryMap;
    }
}
