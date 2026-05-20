package com.openlib.backend.UI;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ControllerUI {

    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConfigurableApplicationContext springContext;
    private final ViewFactory viewFactory;

    public ControllerUI(ConfigurableApplicationContext springContext, ViewFactory viewFactory) {
        this.springContext = springContext;
        this.viewFactory = viewFactory;
    }

    @FXML
    public void goToRegister() {
        viewFactory.showView("/views/register-view.fxml", (Stage) loginButton.getScene().getWindow());
    }

    @FXML
    public void handleLogin() {
        String email    = userField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Por favor ingresa tu correo y contraseña.");
            return;
        }

        errorLabel.setText("");
        loginButton.setDisable(true);
        loginButton.setText("Iniciando sesión...");

        new Thread(() -> {
            try {
                String body = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
                );

                HttpRequest request = HttpRequest.newBuilder()
                    /*.uri(URI.create("http://localhost:8080/api/auth/login"))*/
                        .uri(URI.create("http://localhost:8080/api/users/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Iniciar Sesión");

                    if (response.statusCode() == 200) {
                        // Extraer el rol del JSON de respuesta
                        // Formato esperado: {"token":"...","role":"ADMIN","fullName":"..."}
                        String role = extraerCampoJson(response.body(), "role");
                        SessionManager.getInstance().setEmail(email);
                        SessionManager.getInstance().setRole(role);

                        if ("ADMIN".equalsIgnoreCase(role)) {
                            viewFactory.showRoleView(role, (Stage) loginButton.getScene().getWindow());
                        } else if ("SELLER".equalsIgnoreCase(role)) {
                            viewFactory.showRoleView(role, (Stage) loginButton.getScene().getWindow());
                        } else {
                            viewFactory.showRoleView(role, (Stage) loginButton.getScene().getWindow());
                        }
                    } else if (response.statusCode() == 401) {
                        errorLabel.setText("Correo o contraseña incorrectos.");
                    } else {
                        // Fallback visual para pruebas sin backend completo
                        errorLabel.setText("");
                        enrutarPorEmailFallback(email);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Iniciar Sesión");
                    // Backend no disponible: enrutar por email para pruebas
                    SessionManager.getInstance().setEmail(email);
                    enrutarPorEmailFallback(email);
                });
            }
        }).start();
    }

    /**
     * Enrutamiento por email cuando el backend no está disponible.
     * Útil durante desarrollo.
     */
    private void enrutarPorEmailFallback(String email) {
        if (email.contains("admin")) {
            SessionManager.getInstance().setRole("ADMIN");
            viewFactory.showRoleView("ADMIN", (Stage) loginButton.getScene().getWindow());
        } else if (email.contains("seller")) {
            SessionManager.getInstance().setRole("SELLER");
            viewFactory.showRoleView("SELLER", (Stage) loginButton.getScene().getWindow());
        } else {
            SessionManager.getInstance().setRole("BUYER");
            viewFactory.showRoleView("BUYER", (Stage) loginButton.getScene().getWindow());
        }
    }

    /**
     * Extrae un campo de texto de un JSON simple sin dependencias externas.
     * Ejemplo: extraerCampoJson("{\"role\":\"ADMIN\"}", "role") → "ADMIN"
     */
    private String extraerCampoJson(String json, String campo) {
        String buscar = "\"" + campo + "\"";
        int idx = json.indexOf(buscar);
        if (idx < 0) return "";
        int inicio = json.indexOf("\"", idx + buscar.length() + 1);
        if (inicio < 0) return "";
        int fin = json.indexOf("\"", inicio + 1);
        if (fin < 0) return "";
        return json.substring(inicio + 1, fin);
    }


}
