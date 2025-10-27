package com.avitech.sia.db.oper;

import com.avitech.sia.db.UsuarioDAO;
import com.avitech.sia.db.UsuarioDAO.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper DAO para operaciones relacionadas con usuarios desde la vista Operador.
 */
public class UsuarioOperDAO {
    private final UsuarioDAO delegate = new UsuarioDAO();

    public Optional<Usuario> findByUsuario(String u) throws Exception {
        return delegate.findByUsuario(u);
    }

    public Optional<Usuario> findById(int id) throws Exception {
        return delegate.findById(id);
    }

    public List<String> getAllNombres() throws Exception {
        return delegate.getAllNombres();
    }

    public List<Usuario> getAll() throws Exception {
        return delegate.getAll();
    }

    public void insert(Usuario usuario, int actorId) throws Exception {
        delegate.insert(usuario, actorId);
    }

    public void update(Usuario usuario, int actorId) throws Exception {
        delegate.update(usuario, actorId);
    }

    public void delete(int id, int actorId) throws Exception {
        delegate.delete(id, actorId);
    }

    public void updatePassword(int id, String newPasswordHash, int actorId) throws Exception {
        delegate.updatePassword(id, newPasswordHash, actorId);
    }
}

