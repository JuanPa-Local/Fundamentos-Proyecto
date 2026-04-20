package com.openlib.backend.UI;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ResourceBundle;

@Component
public class SellerController implements Initializable {

    // ── Tab Mis publicaciones
    @FXML private TableView<String[]> booksTable;
    @FXML private TableColumn<String[], String> colTitle;
    @FXML private TableColumn<String[], String> colAuthor;
    @FXML private TableColumn<String[], String> colCategory;
    @FXML private TableColumn<String[], String> colIsbn;
    @FXML private TableColumn<String[], String> colDate;
    @FXML private Label totalBooksLabel;

    // ── Tab Publicar nuevo
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField filePathField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;
    @FXML private Button publishButton;
    @FXML private Label sellerNameLabel;

    private File selectedFile;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConfigurableApplicationContext springContext;

    public SellerController(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Categorías disponibles
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Programación", "Ciencias", "Matemáticas",
                "Diseño", "Negocios", "Historia", "Literatura", "Otros"
        ));

        // Configurar columnas de la tabla
        colTitle.setCellValueFactory(data    -> new SimpleStringProperty(data.getValue()[0]));
        colAuthor.setCellValueFactory(data   -> new SimpleStringProperty(data.getValue()[1]));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colIsbn.setCellValueFactory(data     -> new SimpleStringProperty(data.getValue()[3]));
        colDate.setCellValueFactory(data     -> new SimpleStringProperty(data.getValue()[4]));

        cargarMisLibros();
    }

    private void cargarMisLibros() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/books/my"))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    // TODO: parsear JSON de respuesta
                    // Por ahora datos de ejemplo
                    ObservableList<String[]> data = FXCollections.observableArrayList(
                        new String[]{"Clean Code", "Robert C. Martin", "Programación", "978-01-36", "20/04/2026"},
                        new String[]{"Refactoring", "Martin Fowler", "Programación", "978-02-47", "15/04/2026"}
                    );
                    booksTable.setItems(data);
                    totalBooksLabel.setText(data.size() + " libros publicados");
                });

            } catch (Exception e) {
                Platform.runLater(() -> totalBooksLabel.setText("Sin conexión al servidor"));
            }
        }).start();
    }

    @FXML
    private void selectPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );

        Stage stage = (Stage) filePathField.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handlePublish() {
        errorLabel.setText("");
        successLabel.setText("");

        String title    = titleField.getText().trim();
        String author   = authorField.getText().trim();
        String category = categoryCombo.getValue();

        if (title.isEmpty() || author.isEmpty()) {
            errorLabel.setText("Título y autor son obligatorios.");
            return;
        }
        if (category == null) {
            errorLabel.setText("Selecciona una categoría.");
            return;
        }
        if (selectedFile == null) {
            errorLabel.setText("Debes seleccionar un archivo PDF.");
            return;
        }

        publishButton.setDisable(true);
        publishButton.setText("Publicando...");

        new Thread(() -> {
            try {
                String isbn        = isbnField.getText().trim();
                String description = descriptionArea.getText().trim();

                String body = String.format(
                    "{\"title\":\"%s\",\"author\":\"%s\",\"isbn\":\"%s\"," +
                    "\"category\":\"%s\",\"description\":\"%s\",\"filePath\":\"%s\"}",
                    title, author, isbn, category, description,
                    selectedFile.getAbsolutePath().replace("\\", "\\\\")
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/books"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    publishButton.setDisable(false);
                    publishButton.setText("Publicar libro");

                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        successLabel.setText("¡Libro publicado exitosamente!");
                        limpiarFormulario();
                        cargarMisLibros();
                    } else {
                        errorLabel.setText("Error al publicar. Intenta de nuevo.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    publishButton.setDisable(false);
                    publishButton.setText("Publicar libro");
                    errorLabel.setText("No se pudo conectar al servidor.");
                });
            }
        }).start();
    }

    private void limpiarFormulario() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        descriptionArea.clear();
        filePathField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        selectedFile = null;
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
            Stage stage = (Stage) titleField.getScene().getWindow();
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
