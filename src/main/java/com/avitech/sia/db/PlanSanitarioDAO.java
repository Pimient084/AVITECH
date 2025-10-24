package com.avitech.sia.db;

import com.avitech.sia.iu.SanidadController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PlanSanitarioDAO {

    public List<SanidadController.PlanRow> getPlanesSanitarios() {
        List<SanidadController.PlanRow> planes = new ArrayList<>();
        String sql = "SELECT * FROM Plan_Sanitario";

        try (Connection conn = DB.get();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String nombreEnfermedad = rs.getString("nombre_enfermedad");
                String descripcion = rs.getString("descripcion");
                String observaciones = rs.getString("observaciones");
                planes.add(new SanidadController.PlanRow(nombreEnfermedad, descripcion, "", observaciones));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return planes;
    }
}
