package com.avitech.sia.db;

import com.avitech.sia.DBUtil;
import com.avitech.sia.iu.UsuariosController.UserRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public List<UserRow> getAllUsers() {
        List<UserRow> users = new ArrayList<>();
        String sql = "SELECT nombre, usuario, rol, estado, ultimo_acceso FROM users";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String nombre = rs.getString("nombre");
                String usuario = rs.getString("usuario");
                String rol = rs.getString("rol");
                String estado = rs.getString("estado");
                LocalDateTime ultimoAcceso = rs.getTimestamp("ultimo_acceso").toLocalDateTime();

                users.add(new UserRow(nombre, usuario, rol, estado, ultimoAcceso));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users from database: " + e.getMessage());
        }
        return users;
    }
}
