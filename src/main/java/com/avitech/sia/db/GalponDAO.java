package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GalponDAO {

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
}
