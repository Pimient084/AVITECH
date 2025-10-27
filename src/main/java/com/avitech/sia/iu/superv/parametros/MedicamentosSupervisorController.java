package com.avitech.sia.iu.superv.parametros;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class MedicamentosSupervisorController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TableView<Row> tbl;
    @FXML private TableColumn<Row,String> cNombre,cPActivo,cTipo,cDosis,cRetiro,cEstado,cAccion;

    private final ObservableList<Row> master = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList("Todos los estados","Activo","Inactivo"));
        cbEstado.getSelectionModel().selectFirst();

        cNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombre));
        cPActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().pActivo));
        cTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tipo));
        cDosis.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().dosis));
        cRetiro.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().retiroDias));
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
                new Row("Enrofloxacina 10%","Enrofloxacina","Antibiótico","10mg/kg","5","Activo"),
                new Row("Vitaminas AD3E","Vit. A, D3, E","Vitamina","1ml/L","0","Activo"),
                new Row("Vacuna Newcastle","Virus inactivado","Vacuna","0.5ml/ave","0","Activo")
        );
        tbl.setItems(master);
    }

    @FXML private void onAgregar(){ System.out.println("Agregar medicamento"); }

    public static class Row {
        public final String nombre,pActivo,tipo,dosis,retiroDias,estado;
        public Row(String n,String a,String t,String d,String r,String e){nombre=n;pActivo=a;tipo=t;dosis=d;retiroDias=r;estado=e;}
    }
}
