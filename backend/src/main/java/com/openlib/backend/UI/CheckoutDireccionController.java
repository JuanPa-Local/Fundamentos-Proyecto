package com.openlib.backend.UI;

import com.openlib.backend.application.usecase.GuardarDireccionCheckoutUseCase;
import com.openlib.backend.application.usecase.ObtenerDireccionPrerrellenaUseCase;
import com.openlib.backend.domain.order.DireccionFacturacion;
import com.openlib.backend.domain.order.exception.CampoDireccionObligatorioException;
import com.openlib.backend.domain.order.exception.CarritoVacioException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CheckoutDireccionController {

    @FXML
    private TextField calleField;
    @FXML
    private TextField ciudadField;
    @FXML
    private TextField departamentoField;
    @FXML
    private TextField paisField;

    private final GuardarDireccionCheckoutUseCase guardarDireccionCheckoutUseCase;
    private final ObtenerDireccionPrerrellenaUseCase obtenerDireccionPrerrellenaUseCase;

    public CheckoutDireccionController(GuardarDireccionCheckoutUseCase guardarDireccionCheckoutUseCase,
                                       ObtenerDireccionPrerrellenaUseCase obtenerDireccionPrerrellenaUseCase) {
        this.guardarDireccionCheckoutUseCase = guardarDireccionCheckoutUseCase;
        this.obtenerDireccionPrerrellenaUseCase = obtenerDireccionPrerrellenaUseCase;
    }

    @FXML
    public void initialize() {
        UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
        DireccionFacturacion direccionPrerrellena = obtenerDireccionPrerrellenaUseCase.ejecutar(currentBuyerId);
        
        if (direccionPrerrellena != null) {
            calleField.setText(direccionPrerrellena.getCalle());
            ciudadField.setText(direccionPrerrellena.getCiudad());
            departamentoField.setText(direccionPrerrellena.getDepartamento());
            paisField.setText(direccionPrerrellena.getPais());
        }
    }

    @FXML
    private void handleContinuar() {
        UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
        try {
            guardarDireccionCheckoutUseCase.ejecutar(
                currentBuyerId,
                calleField.getText(),
                ciudadField.getText(),
                departamentoField.getText(),
                paisField.getText()
            );
            
            // Avanza al siguiente paso (US-017)
            ViewFactory.loadView("checkout-pago-view.fxml");
        } catch (CampoDireccionObligatorioException | CarritoVacioException e) {
            mostrarError("Error de validación", e.getMessage());
        } catch (Exception e) {
            mostrarError("Error inesperado", "Ocurrió un error al guardar la dirección: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        ViewFactory.loadView("carrito-view.fxml");
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
