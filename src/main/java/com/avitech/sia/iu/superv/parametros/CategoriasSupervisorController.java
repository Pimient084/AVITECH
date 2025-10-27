package com.avitech.sia.iu.superv.parametros;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CategoriasSupervisorController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row, String> cNombre, cTipo, cColor, cDesc, cEstado, cAccion;

    private final ObservableList<Row> master = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados","Activo","Inactivo"));
        cbEstado.getSelectionModel().selectFirst();

        cNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombre));
        cTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tipo));
        cColor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().color));
        cDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().desc));
        cEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado));
        cAccion.setCellFactory(col -> {
            final Hyperlink link = new Hyperlink("✎ Editar"); // Moved declaration to be final and inside setCellFactory
            return new TableCell<>() {
                @Override protected void updateItem(String s, boolean empty) {
                    super.updateItem(s, empty);
                    if (empty) { setGraphic(null); return; }
                    link.setOnAction(e -> System.out.println("Editar " + getTableView().getItems().get(getIndex()).nombre));
                    setGraphic(link);
                }
            };
        });

        master.setAll(
                new Row("Alimento Balanceado","Insumo","#10B981","Alimentos preparados","Activo"),
                new Row("Medicamentos","Insumo","#3B82F6","Farmacéuticos","Activo"),
                new Row("Vitaminas","Insumo","#F59E0B","Suplementos vitamínicos","Activo"),
                new Row("Huevos Comerciales","Producto","#EF4444","Venta","Activo"),
                new Row("Huevos Fértiles","Producto","#8B5CF6","Incubación","Activo")
        );
        tbl.setItems(master);
    }

    @FXML private void onAgregar() { System.out.println("Agregar categoría"); }

    public static class Row {
        public final String nombre, tipo, color, desc, estado;
        public Row(String n,String t,String c,String d,String e){nombre=n;tipo=t;color=c;desc=d;estado=e;}
    }
}
