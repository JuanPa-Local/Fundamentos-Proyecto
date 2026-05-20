package com.openlib.backend.UI;

import com.openlib.backend.application.usecase.EliminarItemDelCarritoUseCase;
import com.openlib.backend.application.usecase.VerCarritoUseCase;
import com.openlib.backend.domain.order.Carrito;
import com.openlib.backend.domain.order.ItemCarrito;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CarritoController {

    @FXML
    private ListView<ItemCarrito> itemsListView;
    
    @FXML
    private Label totalItemsLabel;
    
    @FXML
    private Label emptyCartLabel;
    
    @FXML
    private Button proceedToCheckoutBtn;

    private final VerCarritoUseCase verCarritoUseCase;
    private final EliminarItemDelCarritoUseCase eliminarItemDelCarritoUseCase;

    public CarritoController(VerCarritoUseCase verCarritoUseCase, EliminarItemDelCarritoUseCase eliminarItemDelCarritoUseCase) {
        this.verCarritoUseCase = verCarritoUseCase;
        this.eliminarItemDelCarritoUseCase = eliminarItemDelCarritoUseCase;
    }

    @FXML
    public void initialize() {
        itemsListView.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(ItemCarrito item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    Label title = new Label(item.getTitulo());
                    Button deleteBtn = new Button("Eliminar");
                    deleteBtn.setOnAction(e -> eliminarItem(item.getLibroId()));
                    hbox.getChildren().addAll(title, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });
        cargarCarrito();
    }

    private void cargarCarrito() {
        UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
        verCarritoUseCase.ejecutar(currentBuyerId).ifPresentOrElse(carrito -> {
            ObservableList<ItemCarrito> items = FXCollections.observableArrayList(carrito.getItems());
            itemsListView.setItems(items);
            totalItemsLabel.setText("Total items: " + carrito.getItems().size());
            
            boolean isEmpty = carrito.estaVacio();
            emptyCartLabel.setVisible(isEmpty);
            proceedToCheckoutBtn.setDisable(isEmpty);
            itemsListView.setVisible(!isEmpty);
        }, () -> {
            emptyCartLabel.setVisible(true);
            proceedToCheckoutBtn.setDisable(true);
            itemsListView.setVisible(false);
            totalItemsLabel.setText("Total items: 0");
        });
    }

    private void eliminarItem(UUID libroId) {
        UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
        try {
            eliminarItemDelCarritoUseCase.ejecutar(currentBuyerId, libroId);
            cargarCarrito();
        } catch (Exception ex) {
            mostrarError("Error al eliminar el item", ex.getMessage());
        }
    }

    @FXML
    private void procedToCheckout() {
        ViewFactory.loadView("checkout-direccion-view.fxml");
    }

    @FXML
    private void goBack() {
        ViewFactory.loadView("buyer-view.fxml");
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
