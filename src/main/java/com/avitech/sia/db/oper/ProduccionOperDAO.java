package com.avitech.sia.db.oper;

import com.avitech.sia.db.Produccion;
import com.avitech.sia.db.ProduccionDAO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Wrapper DAO para el rol Operador — delega en ProduccionDAO pero ofrece una API
 * enfocada a la vista/operaciones del operador.
 */
public class ProduccionOperDAO {
    private final ProduccionDAO delegate = new ProduccionDAO();

    public List<Produccion> getAll() throws Exception {
        return delegate.getAll();
    }

    public void insert(Produccion p) throws Exception {
        delegate.insert(p);
    }

    public int getDailyTotalHuevos(LocalDate date) throws Exception {
        return delegate.getDailyTotalHuevos(date);
    }

    public Map<LocalDate, Integer> getWeeklyTotalHuevos(LocalDate startDate, LocalDate endDate) throws Exception {
        return delegate.getWeeklyTotalHuevos(startDate, endDate);
    }

    public Map<Integer, Integer> getGalponProduction(LocalDate date) throws Exception {
        return delegate.getGalponProduction(date);
    }
}

