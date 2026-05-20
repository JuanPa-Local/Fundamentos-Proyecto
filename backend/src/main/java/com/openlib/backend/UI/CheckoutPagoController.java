package com.openlib.backend.UI;

import com.openlib.backend.application.usecase.GuardarMetodoPagoUseCase;
import com.openlib.backend.domain.order.exception.MetodoPagoInvalidoException;
import com.openlib.backend.domain.order.exception.SesionCheckoutExpiradaException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CheckoutPagoController {

    @FXML private RadioButton tarjetaRadio;
    @FXML private RadioButton transferenciaRadio;
    @FXML private RadioButton donacionRadio;

    private ToggleGroup pagoGroup;

    private final GuardarMetodoPagoUseCase guardarMetodoPagoUseCase;

    public CheckoutPagoController(GuardarMetodoPagoUseCase guardarMetodoPagoUseCase) {
        this.guardarMetodoPagoUseCase = guardarMetodoPagoUseCase;
    }

    @FXML
    public void initialize() {
        pagoGroup = new ToggleGroup();
        tarjetaRadio.setToggleGroup(pagoGroup);
        transferenciaRadio.setToggleGroup(pagoGroup);
        donacionRadio.setToggleGroup(pagoGroup);
        tarjetaRadio.setSelected(true); // default
    }

    @FXML
    private void handleContinuar() {
        RadioButton selected = (RadioButton) pagoGroup.getSelectedToggle();
        String metodo = selected.getText().toUpperCase();

        UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
        
        try {
            guardarMetodoPagoUseCase.ejecutar(currentBuyerId, metodo);
            ViewFactory.loadView("checkout-confirmacion-view.fxml");
        } catch (MetodoPagoInvalidoException | SesionCheckoutExpiradaException e) {
            mostrarError("Error en método de pago", e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        ViewFactory.loadView("checkout-direccion-view.fxml");
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
