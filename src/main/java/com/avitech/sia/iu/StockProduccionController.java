package com.avitech.sia.iu;

import com.avitech.sia.db.StockProduccion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class StockProduccionController {

    @FXML private TableView<StockProduccion> tblStock;
    @FXML private TableColumn<StockProduccion, Long> colId;
    @FXML private TableColumn<StockProduccion, String> colProducto;
    @FXML private TableColumn<StockProduccion, String> colLote;
    @FXML private TableColumn<StockProduccion, Integer> colCantidad;
    @FXML private TableColumn<StockProduccion, String> colUbicacion;
    @FXML private TableColumn<StockProduccion, String> colEstado;
    @FXML private Button btnClose;
    @FXML private Label lblTitle;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colLote.setCellValueFactory(new PropertyValueFactory<>("lote"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    public void setStock(List<StockProduccion> stock) {
        tblStock.setItems(FXCollections.observableArrayList(stock));
    }

    // Permite cambiar el título mostrado en el modal
    public void setTitle(String title) {
        if (lblTitle != null) lblTitle.setText(title);
    }

    @FXML
    private void onClose() {
        Stage s = (Stage) btnClose.getScene().getWindow();
        s.close();
    }
}
