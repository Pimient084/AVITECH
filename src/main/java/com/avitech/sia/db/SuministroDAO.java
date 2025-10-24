package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SuministroDAO {

    public List<Suministro> getAll() throws Exception {
        List<Suministro> suministros = new ArrayList<>();
        String sql = "SELECT * FROM Suministros ORDER BY fecha DESC";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                suministros.add(new Suministro(
                        rs.getInt("id_movimiento"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getString("tipo"),
                        rs.getString("item"),
                        rs.getInt("cantidad"),
                        rs.getString("unidad"),
                        rs.getString("responsable"),
                        rs.getString("proveedor"),
                        rs.getString("motivo"),
                        rs.getBigDecimal("valor_total")
                ));
            }
        }
        return suministros;
    }

    public void insert(Suministro suministro) throws Exception {
        String sql = "INSERT INTO Suministros (fecha, tipo, item, cantidad, unidad, responsable, proveedor, motivo, valor_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(suministro.getFecha()));
            ps.setString(2, suministro.getTipo());
            ps.setString(3, suministro.getItem());
            ps.setInt(4, suministro.getCantidad());
            ps.setString(5, suministro.getUnidad());
            ps.setString(6, suministro.getResponsable());
            ps.setString(7, suministro.getProveedor());
            ps.setString(8, suministro.getMotivo());
            ps.setBigDecimal(9, suministro.getValorTotal());

            ps.executeUpdate();
        }
    }
}
