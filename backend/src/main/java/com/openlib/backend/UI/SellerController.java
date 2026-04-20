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
import javafx.scene.layout.HBox;
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
import java.util.ArrayList;
import java.util.List;
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
    @FXML private TableColumn<String[], String> colActions;
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
        categoryCombo.setItems(FXCollections.observableArrayList(
            "Programación", "Ciencias", "Matemáticas",
            "Diseño", "Negocios", "Historia", "Literatura", "Otros"
        ));

        // Mostrar nombre/email del seller en sesión
        if (!SessionManager.getEmail().isEmpty()) {
            sellerNameLabel.setText(SessionManager.getEmail());
        }

        configurarColumnas();
        cargarMisLibros();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Columnas de la tabla
    // ─────────────────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        colTitle.setCellValueFactory(data    -> new SimpleStringProperty(data.getValue()[0]));
        colAuthor.setCellValueFactory(data   -> new SimpleStringProperty(data.getValue()[1]));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colIsbn.setCellValueFactory(data     -> new SimpleStringProperty(data.getValue()[3]));
        colDate.setCellValueFactory(data     -> new SimpleStringProperty(data.getValue()[4]));

        // Columna de acciones: botón Eliminar
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle(
                    "-fx-background-color: #cf667933; -fx-text-fill: #cf6679;" +
                    "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"
                );
                btnEliminar.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    confirmarEliminar(row[3], row[0]); // isbn, título
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carga de libros del seller
    // ─────────────────────────────────────────────────────────────────────────

    private void cargarMisLibros() {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/books/my"))
                    .header("Authorization", SessionManager.bearerHeader())
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    List<String[]> libros = parsearLibros(response.body());
                    Platform.runLater(() -> {
                        ObservableList<String[]> data = FXCollections.observableArrayList(libros);
                        booksTable.setItems(data);
                        totalBooksLabel.setText(data.size() + " libros publicados");
                    });
                } else {
                    Platform.runLater(() -> totalBooksLabel.setText("No se pudo cargar tus libros."));
                }

            } catch (Exception e) {
                Platform.runLater(() -> totalBooksLabel.setText("Sin conexión al servidor"));
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Publicar nuevo libro
    // ─────────────────────────────────────────────────────────────────────────

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
                    escJson(title), escJson(author), escJson(isbn),
                    escJson(category), escJson(description),
                    escJson(selectedFile.getAbsolutePath())
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/books"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", SessionManager.bearerHeader())
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
                    } else if (response.statusCode() == 409) {
                        errorLabel.setText("Ya existe un libro con ese ISBN.");
                    } else {
                        errorLabel.setText("Error al publicar. Código: " + response.statusCode());
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

    // ─────────────────────────────────────────────────────────────────────────
    // Eliminar libro
    // ─────────────────────────────────────────────────────────────────────────

    private void confirmarEliminar(String isbn, String titulo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar \"" + titulo + "\"? Esta acción no se puede deshacer.",
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) eliminarLibro(isbn);
        });
    }

    private void eliminarLibro(String isbn) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/books/isbn/" + isbn))
                    .header("Authorization", SessionManager.bearerHeader())
                    .DELETE()
                    .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(() -> {
                    cargarMisLibros();
                    successLabel.setText("Libro eliminado correctamente.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText("Error al eliminar el libro."));
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        cambiarPantalla("/views/main-view.fxml");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void limpiarFormulario() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        descriptionArea.clear();
        filePathField.clear();
        categoryCombo.getSelectionModel().clearSelection();
        selectedFile = null;
    }

    /** Devuelve: [title, author, category, isbn, createdAt] */
    private List<String[]> parsearLibros(String json) {
        List<String[]> lista = new ArrayList<>();
        if (json == null || json.isBlank()) return lista;
        for (String obj : dividirObjetos(json)) {
            lista.add(new String[]{
                campo(obj, "title"),
                campo(obj, "author"),
                campo(obj, "category"),
                campo(obj, "isbn"),
                campo(obj, "createdAt")
            });
        }
        return lista;
    }

    private List<String> dividirObjetos(String json) {
        List<String> objetos = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objetos.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objetos;
    }

    private String campo(String obj, String clave) {
        String buscar = "\"" + clave + "\"";
        int idx = obj.indexOf(buscar);
        if (idx < 0) return "";
        int after = idx + buscar.length();
        while (after < obj.length() && (obj.charAt(after) == ':' || obj.charAt(after) == ' ')) after++;
        if (after >= obj.length()) return "";
        char first = obj.charAt(after);
        if (first == '"') {
            int end = obj.indexOf('"', after + 1);
            return end > after ? obj.substring(after + 1, end) : "";
        } else {
            int end = after;
            while (end < obj.length() && obj.charAt(end) != ',' && obj.charAt(end) != '}') end++;
            return obj.substring(after, end).trim();
        }
    }

    /** Escapa caracteres especiales para JSON */
    private String escJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
