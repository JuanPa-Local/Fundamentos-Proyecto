package com.openlib.backend.UI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

@Component
public class RegisterController implements Initializable {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button registerButton;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConfigurableApplicationContext springContext;
    private final ViewFactory viewFactory;

    public RegisterController(ConfigurableApplicationContext springContext, ViewFactory viewFactory) {
        this.springContext = springContext;
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleComboBox.setItems(FXCollections.observableArrayList("BUYER", "SELLER"));
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister() {
        errorLabel.setText("");
        successLabel.setText("");

        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        String role     = roleComboBox.getValue();

        // Validaciones en cliente
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("Por favor completa todos los campos.");
            return;
        }
        if (!email.matches("^[\\w.+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            errorLabel.setText("Ingresa un correo electrónico válido.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Las contraseñas no coinciden.");
            return;
        }
        if (password.length() < 8) {
            errorLabel.setText("La contraseña debe tener mínimo 8 caracteres.");
            return;
        }

        registerButton.setDisable(true);
        registerButton.setText("Registrando...");

        new Thread(() -> {
            try {
                String body = String.format(
                    "{\"fullName\":\"%s\",\"email\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}",
                    escJson(fullName), escJson(email), escJson(password), role
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                javafx.application.Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    registerButton.setText("Crear cuenta");

                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        successLabel.setText("¡Cuenta creada! Redirigiendo al login...");
                        new Thread(() -> {
                            try { Thread.sleep(1500); } catch (Exception ignored) {}
                            javafx.application.Platform.runLater(this::goToLogin);
                        }).start();
                    } else if (response.statusCode() == 409) {
                        errorLabel.setText("Ya existe una cuenta con ese correo.");
                    } else {
                        errorLabel.setText("Error al registrar (código " + response.statusCode() + "). Intenta de nuevo.");
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    registerButton.setText("Crear cuenta");
                    errorLabel.setText("No se pudo conectar al servidor.");
                });
            }
        }).start();
    }

    @FXML
    private void goToLogin() {
        // Siempre volvemos a main-view.fxml (pantalla de login)
        viewFactory.showView("/views/main-view.fxml", (Stage) emailField.getScene().getWindow());
    }

    private String escJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }


}
