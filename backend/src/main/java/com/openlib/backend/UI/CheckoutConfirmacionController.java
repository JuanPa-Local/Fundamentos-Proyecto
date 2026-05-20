package com.openlib.backend.UI;

import com.openlib.backend.application.usecase.ConfirmarOrdenUseCase;
import com.openlib.backend.domain.order.exception.CarritoVacioException;
import com.openlib.backend.domain.order.exception.SesionCheckoutIncompletaException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CheckoutConfirmacionController {

    @FXML private Label statusLabel;
    
    private final ConfirmarOrdenUseCase confirmarOrdenUseCase;

    public CheckoutConfirmacionController(ConfirmarOrdenUseCase confirmarOrdenUseCase) {
        this.confirmarOrdenUseCase = confirmarOrdenUseCase;
    }

    @FXML
    public void initialize() {
        // En cuanto carga la vista, intentamos confirmar. 
        // Si hay éxito mostramos el mensaje.
        confirmar();
    }

    private void confirmar() {
        UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
        try {
            confirmarOrdenUseCase.ejecutar(currentBuyerId);
            statusLabel.setText("¡Orden confirmada exitosamente!");
            statusLabel.setStyle("-fx-text-fill: #8FC234; -fx-font-size: 16px; -fx-font-weight: bold;");
        } catch (SesionCheckoutIncompletaException | CarritoVacioException e) {
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #CF6679; -fx-font-size: 16px;");
        } catch (Exception e) {
            statusLabel.setText("Ocurrió un error inesperado al confirmar.");
            statusLabel.setStyle("-fx-text-fill: #CF6679; -fx-font-size: 16px;");
        }
    }

    @FXML
    private void goToBiblioteca() {
        ViewFactory.loadView("buyer-view.fxml"); // Asumimos que allí el usuario puede ver su biblioteca
    }
}
