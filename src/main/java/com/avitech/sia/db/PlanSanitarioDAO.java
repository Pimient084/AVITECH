package com.avitech.sia.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class PlanSanitarioDAO {

    public int getAplicacionesMes() throws Exception {
        String sql = "SELECT COUNT(*) FROM Plan_Sanitario WHERE fecha >= ?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(30)));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int getMuertesMes() throws Exception {
        String sql = "SELECT SUM(muertes) FROM Plan_Sanitario WHERE fecha >= ?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.now().minusDays(30)));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
