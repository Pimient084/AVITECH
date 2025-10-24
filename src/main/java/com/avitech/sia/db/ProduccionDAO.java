package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProduccionDAO {

    public List<Produccion> getAll() throws Exception {
        List<Produccion> produccion = new ArrayList<>();
        String sql = "SELECT * FROM ProduccionHuevos ORDER BY fecha DESC";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                produccion.add(new Produccion(
                        rs.getInt("Id_produccion"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getInt("galpon"),
                        rs.getInt("total_huevos"),
                        rs.getInt("huevos_L"),
                        rs.getInt("huevos_M"),
                        rs.getInt("huevos_S"),
                        rs.getFloat("temperatura"),
                        rs.getFloat("humedad"),
                        rs.getInt("mortalidad"),
                        rs.getString("responsable")
                ));
            }
        }
        return produccion;
    }

    public void insert(Produccion produccion) throws Exception {
        String sql = "INSERT INTO ProduccionHuevos (fecha, galpon, total_huevos, huevos_L, huevos_M, huevos_S, temperatura, humedad, mortalidad, responsable) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(produccion.getFecha()));
            ps.setInt(2, produccion.getGalpon());
            ps.setInt(3, produccion.getTotalHuevos());
            ps.setInt(4, produccion.getHuevosL());
            ps.setInt(5, produccion.getHuevosM());
            ps.setInt(6, produccion.getHuevosS());
            ps.setFloat(7, produccion.getTemperatura());
            ps.setFloat(8, produccion.getHumedad());
            ps.setInt(9, produccion.getMortalidad());
            ps.setString(10, produccion.getResponsable());

            ps.executeUpdate();
        }
    }

    public int getDailyTotalHuevos(LocalDate date) throws Exception {
        String sql = "SELECT SUM(total_huevos) FROM ProduccionHuevos WHERE fecha = ?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Map<LocalDate, Integer> getWeeklyTotalHuevos(LocalDate startDate, LocalDate endDate) throws Exception {
        Map<LocalDate, Integer> weeklyProduction = new LinkedHashMap<>();
        String sql = "SELECT fecha, SUM(total_huevos) AS total FROM ProduccionHuevos WHERE fecha BETWEEN ? AND ? GROUP BY fecha ORDER BY fecha";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    weeklyProduction.put(rs.getDate("fecha").toLocalDate(), rs.getInt("total"));
                }
            }
        }
        return weeklyProduction;
    }

    public Map<Integer, Integer> getGalponProduction(LocalDate date) throws Exception {
        Map<Integer, Integer> galponProduction = new LinkedHashMap<>();
        String sql = "SELECT galpon, SUM(total_huevos) AS total FROM ProduccionHuevos WHERE fecha = ? GROUP BY galpon ORDER BY galpon";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    galponProduction.put(rs.getInt("galpon"), rs.getInt("total"));
                }
            }
        }
        return galponProduction;
    }
}
