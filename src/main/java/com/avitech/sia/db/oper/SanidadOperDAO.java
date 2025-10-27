package com.avitech.sia.db.oper;

import com.avitech.sia.db.GalponDAO;
import com.avitech.sia.db.Medicamento;
import com.avitech.sia.db.MedicamentoDAO;
import com.avitech.sia.db.PlanSanitarioDAO;

import java.util.List;

/**
 * Wrapper DAO para operaciones de sanidad desde la vista Operador.
 */
public class SanidadOperDAO {
    private final PlanSanitarioDAO planDAO = new PlanSanitarioDAO();
    private final MedicamentoDAO medDAO = new MedicamentoDAO();
    private final GalponDAO galponDAO = new GalponDAO();

    public int getAplicacionesMes() throws Exception {
        return planDAO.getAplicacionesMes();
    }

    public int getMuertesMes() throws Exception {
        return planDAO.getMuertesMes();
    }

    public List<Medicamento> getAllMedicamentos() throws Exception {
        return medDAO.getAll();
    }

    public List<String> getAllGalpones() throws Exception {
        // Use existing GalponDAO method to get active galpon names
        return galponDAO.getActiveGalpones();
    }
}
