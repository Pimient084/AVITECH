package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDAO {

    public List<Medicamento> getAll() throws Exception {
        List<Medicamento> medicamentos = new ArrayList<>();
        String sql = "SELECT * FROM Medicamentos ORDER BY nombre";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                medicamentos.add(new Medicamento(
                        rs.getInt("Id_Medicamento"),
                        rs.getString("nombre"),
                        rs.getString("presentacion"),
                        rs.getInt("stock"),
                        rs.getInt("stock_minimo"),
                        rs.getBigDecimal("valor_unitario")
                ));
            }
        }
        return medicamentos;
    }

    public void insert(Medicamento medicamento) throws Exception {
        String sql = "INSERT INTO Medicamentos (nombre, presentacion, stock, stock_minimo, valor_unitario) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicamento.getNombre());
            ps.setString(2, medicamento.getPresentacion());
            ps.setInt(3, medicamento.getStock());
            ps.setInt(4, medicamento.getStockMinimo());
            ps.setBigDecimal(5, medicamento.getValorUnitario());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    medicamento.setId(rs.getInt(1));
                }
            }
        }
    }
}
