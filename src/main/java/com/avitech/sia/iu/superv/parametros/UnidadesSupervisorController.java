package com.avitech.sia.iu.superv.parametros;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UnidadesSupervisorController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row, String> cNombre, cSimbolo, cTipo, cDesc, cEstado, cAccion;

    private final ObservableList<Row> master = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados","Activo","Inactivo"));
        cbEstado.getSelectionModel().selectFirst();

        cNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombre));
        cSimbolo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().simbolo));
        cTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tipo));
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

        // datos dummy
        master.setAll(
                new Row("Kilogramo","kg","Peso","Unidad de masa","Activo"),
                new Row("Gramo","g","Peso","Unidad de masa menor","Activo"),
                new Row("Litro","L","Volumen","Unidad de volumen","Activo"),
                new Row("Mililitro","mL","Volumen","Unidad de volumen menor","Activo"),
                new Row("Dosis","dosis","Cantidad","Dosis médica","Activo"),
                new Row("Unidad","und","Cantidad","Unidad individual","Activo")
        );
        tbl.setItems(master);
    }

    @FXML private void onAgregar() { System.out.println("Agregar unidad"); }

    public static class Row {
        public final String nombre, simbolo, tipo, desc, estado;
        public Row(String n, String s, String t, String d, String e) { nombre=n; simbolo=s; tipo=t; desc=d; estado=e; }
    }
}
