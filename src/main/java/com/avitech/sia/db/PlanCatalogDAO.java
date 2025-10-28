package com.avitech.sia.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanCatalogDAO {

    private void ensureTable() throws Exception {
        String ddl = "CREATE TABLE IF NOT EXISTS Plan_Catalogo (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nombre VARCHAR(100) NOT NULL UNIQUE," +
                "descripcion VARCHAR(255)," +
                "edad VARCHAR(50)," +
                "estado VARCHAR(30) NOT NULL" +
                ")";
        try (Connection conn = DB.get(); Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        }
    }

    public List<PlanCatalog> listAll() throws Exception {
        ensureTable();
        String sql = "SELECT id, nombre, descripcion, edad, estado FROM Plan_Catalogo ORDER BY id DESC";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<PlanCatalog> list = new ArrayList<>();
            while (rs.next()) {
                PlanCatalog p = new PlanCatalog(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("descripcion"),
                        rs.getString("edad"),
                        rs.getString("estado")
                );
                list.add(p);
            }
            return list;
        }
    }

    public PlanCatalog insert(PlanCatalog plan) throws Exception {
        ensureTable();
        String sql = "INSERT INTO Plan_Catalogo (nombre, descripcion, edad, estado) VALUES (?,?,?,?)";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, plan.getNombre());
            ps.setString(2, plan.getDescripcion());
            ps.setString(3, plan.getEdad());
            ps.setString(4, plan.getEstado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    plan.setId(keys.getInt(1));
                }
            }
            return plan;
        }
    }

    public void update(PlanCatalog plan) throws Exception {
        ensureTable();
        String sql = "UPDATE Plan_Catalogo SET nombre=?, descripcion=?, edad=?, estado=? WHERE id=?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plan.getNombre());
            ps.setString(2, plan.getDescripcion());
            ps.setString(3, plan.getEdad());
            ps.setString(4, plan.getEstado());
            ps.setInt(5, plan.getId());
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) throws Exception {
        ensureTable();
        String sql = "DELETE FROM Plan_Catalogo WHERE id=?";
        try (Connection conn = DB.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
