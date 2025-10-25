package com.avitech.sia.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioDAO {
    // id_usuario, usuario (login), password, rol, email, telefono, direccion
    public record Usuario(int id, String usuario, String password, String rol, String email, String telefono, String direccion) {}

    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    public Optional<Usuario> findByUsuario(String u) throws Exception {
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT id_usuario, usuario, password, rol, email, telefono, direccion FROM Usuarios WHERE usuario=?")) {
            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("usuario"),
                            rs.getString("password"),
                            rs.getString("rol"),
                            rs.getString("email"),
                            rs.getString("telefono"),
                            rs.getString("direccion")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    public Optional<Usuario> findById(int id) throws Exception {
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT id_usuario, usuario, password, rol, email, telefono, direccion FROM Usuarios WHERE id_usuario=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("usuario"),
                            rs.getString("password"),
                            rs.getString("rol"),
                            rs.getString("email"),
                            rs.getString("telefono"),
                            rs.getString("direccion")
                    ));
                }
                return Optional.empty();
            }
        }
    }

    public List<String> getAllNombres() throws Exception {
        List<String> nombres = new ArrayList<>();
        try (Connection cn = DB.get();
             PreparedStatement ps = cn.prepareStatement("SELECT usuario FROM Usuarios ORDER BY usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                nombres.add(rs.getString("usuario"));
            }
        }
        return nombres;
    }

    public List<Usuario> getAll() throws Exception {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT id_usuario, usuario, password, rol, email, telefono, direccion FROM Usuarios ORDER BY usuario";

        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("usuario"),
                        rs.getString("password"),
                        rs.getString("rol"),
                        rs.getString("email"),
                        rs.getString("telefono"),
                        rs.getString("direccion")
                ));
            }
        }
        return usuarios;
    }

    public void insert(Usuario usuario, int actorId) throws Exception {
        String sql = "INSERT INTO Usuarios (usuario, password, rol, email, telefono, direccion) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.usuario());
            ps.setString(2, usuario.password());
            ps.setString(3, usuario.rol());
            ps.setString(4, usuario.email());
            ps.setString(5, usuario.telefono());
            ps.setString(6, usuario.direccion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    auditoriaDAO.insert(actorId, "CREATE", "Usuarios", "Usuario creado: " + usuario.usuario(), "id_usuario=" + newId);
                }
            }
        }
    }

    public void update(Usuario usuario, int actorId) throws Exception {
        String sql = "UPDATE Usuarios SET usuario=?, password=?, rol=?, email=?, telefono=?, direccion=? WHERE id_usuario=?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.usuario());
            ps.setString(2, usuario.password());
            ps.setString(3, usuario.rol());
            ps.setString(4, usuario.email());
            ps.setString(5, usuario.telefono());
            ps.setString(6, usuario.direccion());
            ps.setInt(7, usuario.id());
            ps.executeUpdate();
            auditoriaDAO.insert(actorId, "UPDATE", "Usuarios", "Usuario actualizado: " + usuario.usuario(), "id_usuario=" + usuario.id());
        }
    }

    public void delete(int id, int actorId) throws Exception {
        // First, get the user's name for the audit log before deleting
        Optional<Usuario> userToDelete = findById(id);
        String userName = userToDelete.map(Usuario::usuario).orElse("Desconocido");

        String sql = "DELETE FROM Usuarios WHERE id_usuario=?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            auditoriaDAO.insert(actorId, "DELETE", "Usuarios", "Usuario eliminado: " + userName, "id_usuario=" + id);
        }
    }

    public void updatePassword(int id, String newPasswordHash, int actorId) throws Exception {
        // Get the user's name for the audit log
        Optional<Usuario> userToUpdate = findById(id);
        String userName = userToUpdate.map(Usuario::usuario).orElse("Desconocido");

        String sql = "UPDATE Usuarios SET password=? WHERE id_usuario=?";
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
            auditoriaDAO.insert(actorId, "UPDATE_PASSWORD", "Usuarios", "Contraseña de usuario actualizada: " + userName, "id_usuario=" + id);
        }
    }
}
