package com.avitech.sia.db;

import com.avitech.sia.iu.SanidadController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<SanidadController.MedRow> getMedicamentos() {
        List<SanidadController.MedRow> medicamentos = new ArrayList<>();
        String sql = "SELECT * FROM Medicamentos";

        try (Connection conn = DB.get();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int stock = rs.getInt("stock");
                int stockMinimo = rs.getInt("stock_minimo");
                double nivel = stockMinimo > 0 ? (double) stock / stockMinimo : 0.0;
                medicamentos.add(new SanidadController.MedRow(nombre, stock, nivel, "Ver en Inventario"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return medicamentos;
    }

    public List<String> getNombresMedicamentos() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM Medicamentos ORDER BY nombre";

        try (Connection conn = DB.get();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nombres;
    }
}
