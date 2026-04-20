package com.openlib.backend.UI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ControllerUI {

    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private final ConfigurableApplicationContext springContext;

    public ControllerUI(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @FXML
    public void goToRegister() {
        cambiarPantalla("/views/register-view.fxml");
    }

    @FXML
    public void handleLogin() {
        String email = userField.getText();

        // LÓGICA DE NAVEGACIÓN (Punto 3)
        if (email.equals("admin@openlib.com")) {
            cambiarPantalla("/views/admin-view.fxml");
        } else if (email.equals("seller@openlib.com")) {
            cambiarPantalla("/views/seller-view.fxml");
        } else {
            cambiarPantalla("/views/buyer-view.fxml");
        }
    }

    private void cambiarPantalla(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);

            // Cargar el CSS morado que hicimos
            scene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());

            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Error cargando la vista: " + fxmlPath);
            e.printStackTrace();
        }
    }
}