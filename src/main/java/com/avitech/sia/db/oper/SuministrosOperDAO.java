package com.avitech.sia.db.oper;

import com.avitech.sia.db.Suministro;
import com.avitech.sia.db.SuministroDAO;

import java.util.List;

/**
 * Wrapper DAO para operaciones de suministros desde la vista Operador.
 */
public class SuministrosOperDAO {
    private final SuministroDAO delegate = new SuministroDAO();

    public List<Suministro> getAll() throws Exception {
        return delegate.getAll();
    }

    public void insert(Suministro s) throws Exception {
        delegate.insert(s);
    }

    public int getDistinctItemCount() throws Exception {
        return delegate.getDistinctItemCount();
    }
}

