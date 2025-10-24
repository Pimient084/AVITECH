package com.avitech.sia.db;

import com.avitech.sia.iu.SuministrosController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SuministroDAO {

    public List<SuministrosController.Mov> getSuministros() {
        List<SuministrosController.Mov> suministros = new ArrayList<>();
        String sql = "SELECT * FROM Suministros";

        try (Connection conn = DB.get();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String fecha = rs.getString("fecha");
                String item = rs.getString("item");
                String cantidad = rs.getString("cantidad");
                String unidad = rs.getString("unidad");
                String tipo = rs.getString("tipo");
                String responsable = rs.getString("responsable");
                String detalles = rs.getString("motivo");
                String stock = ""; // Esta columna no está en la tabla Suministros

                suministros.add(new SuministrosController.Mov(fecha, item, cantidad, unidad, tipo, responsable, detalles, stock));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suministros;
    }
}
