package com.avitech.sia.iu.superv.parametros;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UbicacionesSupervisorController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row,String> cNombre,cTipo,cCapacidad,cResp,cEstado,cAccion;

    private final ObservableList<Row> master = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados","Activo","Inactivo"));
        cbEstado.getSelectionModel().selectFirst();

        cNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombre));
        cTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tipo));
        cCapacidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().capacidad));
        cResp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().responsable));
        cEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado));
        cAccion.setCellFactory(col -> new TableCell<>() {
            final Hyperlink link = new Hyperlink("✎ Editar");
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty) { setGraphic(null); return; }
                link.setOnAction(e -> System.out.println("Editar " + getTableView().getItems().get(getIndex()).nombre));
                setGraphic(link);
            }
        });

        master.setAll(
                new Row("Almacén Principal","Almacén","500 m²","Carlos Pérez","Activo"),
                new Row("Galpón 1","Galpón","5000 aves","Ana Rodríguez","Activo"),
                new Row("Área de Incubación","Área de Proceso","10000 huevos","Luis Morales","Activo"),
                new Row("Almacén de Medicamentos","Almacén","50 m²","Elena Vargas","Activo")
        );
        tbl.setItems(master);
    }

    @FXML private void onAgregar(){ System.out.println("Agregar ubicación"); }

    public static class Row {
        public final String nombre,tipo,capacidad,responsable,estado;
        public Row(String n,String t,String c,String r,String e){nombre=n;tipo=t;capacidad=c;responsable=r;estado=e;}
    }
}
