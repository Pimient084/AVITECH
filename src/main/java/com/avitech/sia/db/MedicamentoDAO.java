package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
}
