package com.avitech.sia.db;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockRepository {

    public List<StockProduccion> getStockEnProduccion() {
        List<StockProduccion> resultado = new ArrayList<>();
        try {
            // 1) Medicamentos: stock directo desde MedicamentoDAO
            MedicamentoDAO medDao = new MedicamentoDAO();
            List<Medicamento> meds = medDao.getAll();
            for (Medicamento m : meds) {
                long id = m.getId();
                String producto = m.getNombre() + " - " + m.getPresentacion();
                String lote = "";
                int cantidad = m.getStock();
                String ubicacion = "";
                String estado = (m.getStockMinimo() > 0 && cantidad <= m.getStockMinimo()) ? "Bajo" : "OK";
                if (cantidad > 0) {
                    resultado.add(new StockProduccion(id, producto, lote, cantidad, ubicacion, estado));
                }
            }

            // 2) Suministros: computar stock por ítem a partir de movimientos (Entradas/Salidas)
            SuministroDAO sumDao = new SuministroDAO();
            List<Suministro> movs = sumDao.getAll();
            Map<String, Integer> stockPorItem = new HashMap<>();
            for (Suministro s : movs) {
                String item = s.getItem();
                int qty = s.getCantidad();
                if ("Entrada".equalsIgnoreCase(s.getTipo())) {
                    stockPorItem.put(item, stockPorItem.getOrDefault(item, 0) + qty);
                } else if ("Salida".equalsIgnoreCase(s.getTipo())) {
                    stockPorItem.put(item, stockPorItem.getOrDefault(item, 0) - qty);
                }
            }
            for (Map.Entry<String, Integer> e : stockPorItem.entrySet()) {
                int qty = e.getValue();
                if (qty <= 0) continue;
                String producto = e.getKey();
                resultado.add(new StockProduccion(0L, producto, "", qty, "", "OK"));
            }

            // Nota: La lógica de suministros también estará disponible por separado en getStockSuministros().

            // 3) Producción de huevos: usar ProduccionDAO para obtener producción por galpón del día
            // (se integra para que el botón 'Ver Stock Producción' muestre también estos registros)
            ProduccionDAO prodDao = new ProduccionDAO();
            Map<Integer, Integer> prodPorGalpon = prodDao.getGalponProduction(LocalDate.now());
            GalponDAO galponDao = new GalponDAO();
            Map<Integer, String> galponMap = galponDao.getGalponMap();
            for (Map.Entry<Integer, Integer> e : prodPorGalpon.entrySet()) {
                int galponId = e.getKey();
                int qty = e.getValue();
                if (qty <= 0) continue;
                String galponNombre = galponMap.getOrDefault(galponId, "Galpón " + galponId);
                String producto = "Huevos";
                String lote = LocalDate.now().toString();
                resultado.add(new StockProduccion(0L, producto, lote, qty, galponNombre, "OK"));
            }

            return resultado;
        } catch (Exception e) {
            throw new RuntimeException("Error consultando stock en producción", e);
        }
    }

    // Devuelve únicamente el stock calculado desde movimientos de suministros (Entradas/Salidas)
    public List<StockProduccion> getStockSuministros() {
        List<StockProduccion> resultado = new ArrayList<>();
        try {
            SuministroDAO sumDao = new SuministroDAO();
            List<Suministro> movs = sumDao.getAll();
            Map<String, Integer> stockPorItem = new HashMap<>();
            for (Suministro s : movs) {
                String item = s.getItem();
                int qty = s.getCantidad();
                if ("Entrada".equalsIgnoreCase(s.getTipo())) {
                    stockPorItem.put(item, stockPorItem.getOrDefault(item, 0) + qty);
                } else if ("Salida".equalsIgnoreCase(s.getTipo())) {
                    stockPorItem.put(item, stockPorItem.getOrDefault(item, 0) - qty);
                }
            }
            for (Map.Entry<String, Integer> e : stockPorItem.entrySet()) {
                int qty = e.getValue();
                if (qty <= 0) continue;
                String producto = e.getKey();
                resultado.add(new StockProduccion(0L, producto, "", qty, "", "OK"));
            }
            return resultado;
        } catch (Exception e) {
            throw new RuntimeException("Error consultando stock de suministros", e);
        }
    }
}
