package com.openlib.backend.UI;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
public class BuyerController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane booksContainer;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private ComboBox<String> sortCombo;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConfigurableApplicationContext springContext;

    public BuyerController(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sortCombo.setItems(FXCollections.observableArrayList(
                "Más recientes", "Título A-Z", "Autor A-Z"
        ));
        sortCombo.getSelectionModel().selectFirst();
        cargarLibros();
    }

    private void cargarLibros() {
        statusLabel.setText("Cargando catálogo...");
        booksContainer.getChildren().clear();

        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/books"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        // TODO: parsear JSON y crear tarjetas
                        // Por ahora mostramos tarjetas de ejemplo
                        mostrarLibrosEjemplo();
                        statusLabel.setText("Catálogo cargado");
                    } else {
                        mostrarLibrosEjemplo(); // fallback visual
                        statusLabel.setText("Vista previa del catálogo");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarLibrosEjemplo();
                    statusLabel.setText("Modo sin conexión — vista previa");
                });
            }
        }).start();
    }

    /**
     * Crea una tarjeta de libro para mostrarse en el FlowPane.
     * Cuando el backend esté completo, este método recibirá datos reales.
     */
    private VBox crearTarjetaLibro(String titulo, String autor, String categoria) {
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setStyle(
            "-fx-background-color: #1e1e1e;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 0 0 16 0;" +
            "-fx-border-color: #2c2c2c;" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        );

        // Portada placeholder
        Region cover = new Region();
        cover.setPrefSize(180, 220);
        cover.setStyle("-fx-background-color: #2c2c2c; -fx-background-radius: 12 12 0 0;");

        // Ícono de libro (texto placeholder)
        Label iconLabel = new Label("📖");
        iconLabel.setStyle("-fx-font-size: 40px;");
        StackPane coverPane = new StackPane(cover, iconLabel);
        coverPane.setPrefSize(180, 220);

        // Info
        VBox info = new VBox(6);
        info.setStyle("-fx-padding: 0 12;");

        Label titleLabel = new Label(titulo);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label authorLabel = new Label(autor);
        authorLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

        Label categoryBadge = new Label(categoria);
        categoryBadge.setStyle(
            "-fx-background-color: #bb86fc22;" +
            "-fx-text-fill: #bb86fc;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 2 8;" +
            "-fx-font-size: 11px;"
        );

        Label priceLabel = new Label("$0.00 — Gratis");
        priceLabel.setStyle("-fx-text-fill: #03dac6; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button downloadBtn = new Button("Descargar");
        downloadBtn.setMaxWidth(Double.MAX_VALUE);
        downloadBtn.setStyle(
            "-fx-background-color: #bb86fc;" +
            "-fx-text-fill: #000000;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 8 0;" +
            "-fx-cursor: hand;"
        );
        downloadBtn.setOnAction(e -> descargarLibro(titulo));

        info.getChildren().addAll(titleLabel, authorLabel, categoryBadge, priceLabel, downloadBtn);
        card.getChildren().addAll(coverPane, info);

        return card;
    }

    private void mostrarLibrosEjemplo() {
        booksContainer.getChildren().addAll(
            crearTarjetaLibro("Clean Code", "Robert C. Martin", "Programación"),
            crearTarjetaLibro("The Pragmatic Programmer", "Andrew Hunt", "Programación"),
            crearTarjetaLibro("Diseño de Interfaces", "Steve Krug", "Diseño"),
            crearTarjetaLibro("Algoritmos y Estructuras", "Cormen et al.", "Matemáticas"),
            crearTarjetaLibro("Spring Boot en Acción", "Craig Walls", "Programación"),
            crearTarjetaLibro("El Método Lean Startup", "Eric Ries", "Negocios")
        );
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            cargarLibros();
            return;
        }
        statusLabel.setText("Buscando: " + query);
        // TODO: llamar a /api/books?search=query
    }

    @FXML
    private void filtrarCategoria(javafx.event.ActionEvent e) {
        Button btn = (Button) e.getSource();
        String categoria = btn.getText().trim();
        statusLabel.setText("Filtrando: " + categoria);
        // TODO: llamar a /api/books?category=categoria
    }

    @FXML
    private void goToBiblioteca() {
        // TODO: navegar a vista de biblioteca personal
        statusLabel.setText("Mi Biblioteca — próximamente");
    }

    private void descargarLibro(String titulo) {
        statusLabel.setText("Iniciando descarga: " + titulo + "...");
        // TODO: llamar a /api/orders para registrar y obtener signed URL
    }

    @FXML
    private void handleLogout() {
        cambiarPantalla("/views/login-view.fxml");
    }

    private void cambiarPantalla(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource("/styles/global.css").toExternalForm()
            );
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
