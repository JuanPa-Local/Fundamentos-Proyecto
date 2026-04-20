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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class BuyerController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane booksContainer;
    @FXML private Label statusLabel;
    @FXML private Label userLabel;
    @FXML private ComboBox<String> sortCombo;

    // Todos los libros cargados, para filtrar localmente
    private List<String[]> todosLosLibros = new ArrayList<>();
    // [0]=title, [1]=author, [2]=category, [3]=isbn, [4]=id

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
        sortCombo.setOnAction(e -> ordenarYMostrar());

        // Mostrar email del usuario en sesión
        if (!SessionManager.getEmail().isEmpty()) {
            userLabel.setText(SessionManager.getEmail());
        }

        cargarLibros(null, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carga y visualización de libros
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Carga libros desde la API.
     * @param search   texto de búsqueda, o null para todos
     * @param categoria categoría a filtrar, o null para todas
     */
    private void cargarLibros(String search, String categoria) {
        statusLabel.setText("Cargando catálogo...");
        booksContainer.getChildren().clear();

        new Thread(() -> {
            try {
                // Construir URL con parámetros opcionales
                StringBuilder url = new StringBuilder("http://localhost:8080/api/books");
                String sep = "?";
                if (search != null && !search.isEmpty()) {
                    url.append(sep).append("search=").append(search);
                    sep = "&";
                }
                if (categoria != null && !categoria.isBlank() && !categoria.equals("Todas")) {
                    url.append(sep).append("category=").append(java.net.URLEncoder.encode(categoria, "UTF-8"));
                }

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .header("Authorization", SessionManager.bearerHeader())
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    List<String[]> libros = parsearLibros(response.body());
                    Platform.runLater(() -> {
                        todosLosLibros = libros;
                        mostrarLibros(libros);
                        statusLabel.setText(libros.isEmpty()
                            ? "No se encontraron libros."
                            : libros.size() + " libros en el catálogo");
                    });
                } else {
                    Platform.runLater(() -> {
                        mostrarLibrosDemo();
                        statusLabel.setText("Vista previa del catálogo");
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    mostrarLibrosDemo();
                    statusLabel.setText("Modo sin conexión — vista previa");
                });
            }
        }).start();
    }

    private void mostrarLibros(List<String[]> libros) {
        booksContainer.getChildren().clear();
        for (String[] libro : libros) {
            booksContainer.getChildren().add(
                crearTarjetaLibro(libro[0], libro[1], libro[2], libro[4])
            );
        }
    }

    private void ordenarYMostrar() {
        if (todosLosLibros.isEmpty()) return;
        String criterio = sortCombo.getValue();
        List<String[]> copia = new ArrayList<>(todosLosLibros);

        if ("Título A-Z".equals(criterio)) {
            copia.sort((a, b) -> a[0].compareToIgnoreCase(b[0]));
        } else if ("Autor A-Z".equals(criterio)) {
            copia.sort((a, b) -> a[1].compareToIgnoreCase(b[1]));
        }
        // "Más recientes" = orden original de la API

        mostrarLibros(copia);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tarjeta de libro
    // ─────────────────────────────────────────────────────────────────────────

    private VBox crearTarjetaLibro(String titulo, String autor, String categoria, String id) {
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

        Label iconLabel = new Label("📖");
        iconLabel.setStyle("-fx-font-size: 40px;");
        StackPane coverPane = new StackPane(cover, iconLabel);
        coverPane.setPrefSize(180, 220);

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
        downloadBtn.setOnAction(e -> descargarLibro(id, titulo));

        info.getChildren().addAll(titleLabel, authorLabel, categoryBadge, priceLabel, downloadBtn);
        card.getChildren().addAll(coverPane, info);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Acciones de UI
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        cargarLibros(query.isEmpty() ? null : query, null);
    }

    @FXML
    private void filtrarCategoria(javafx.event.ActionEvent e) {
        Button btn = (Button) e.getSource();
        String categoria = btn.getText().trim();
        // El texto tiene espacios al inicio: "  Programación" → limpiar
        categoria = categoria.replaceAll("^\\s+", "");
        cargarLibros(null, "Todas".equals(categoria) ? null : categoria);
    }

    @FXML
    private void goToBiblioteca() {
        statusLabel.setText("Cargando tu biblioteca...");
        // Obtener órdenes/compras del usuario actual
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/orders/my"))
                    .header("Authorization", SessionManager.bearerHeader())
                    .GET()
                    .build();
                HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    List<String[]> ordenes = parsearOrdenes(response.body());
                    Platform.runLater(() -> {
                        booksContainer.getChildren().clear();
                        if (ordenes.isEmpty()) {
                            statusLabel.setText("Tu biblioteca está vacía. ¡Descarga tu primer libro!");
                        } else {
                            for (String[] o : ordenes) {
                                booksContainer.getChildren().add(
                                    crearTarjetaLibro(o[1], "", "", o[0])
                                );
                            }
                            statusLabel.setText("Mi Biblioteca — " + ordenes.size() + " libros");
                        }
                    });
                } else {
                    Platform.runLater(() -> statusLabel.setText("No se pudo cargar tu biblioteca."));
                }
            } catch (Exception ex) {
                Platform.runLater(() -> statusLabel.setText("Biblioteca no disponible sin conexión."));
            }
        }).start();
    }

    private void descargarLibro(String bookId, String titulo) {
        statusLabel.setText("Iniciando descarga: " + titulo + "...");

        new Thread(() -> {
            try {
                String body = String.format("{\"bookId\":\"%s\"}", bookId);
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/orders"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", SessionManager.bearerHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

                HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    if (response.statusCode() == 200 || response.statusCode() == 201) {
                        // Obtener la URL de descarga del JSON de respuesta
                        String downloadUrl = campo(response.body(), "downloadUrl");
                        if (!downloadUrl.isEmpty()) {
                            statusLabel.setText("Descarga lista: " + titulo);
                            // Abrir URL en el navegador del sistema
                            try {
                                java.awt.Desktop.getDesktop().browse(URI.create(downloadUrl));
                            } catch (Exception ex) {
                                statusLabel.setText("URL: " + downloadUrl);
                            }
                        } else {
                            statusLabel.setText("¡Orden registrada! Descarga de: " + titulo);
                        }
                    } else if (response.statusCode() == 409) {
                        statusLabel.setText("Ya tienes este libro en tu biblioteca.");
                    } else {
                        statusLabel.setText("Error al procesar la descarga.");
                    }
                });

            } catch (Exception ex) {
                Platform.runLater(() ->
                    statusLabel.setText("No se pudo conectar al servidor para descargar.")
                );
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
    // Demo / fallback visual
    // ─────────────────────────────────────────────────────────────────────────

    private void mostrarLibrosDemo() {
        String[][] demos = {
            {"Clean Code", "Robert C. Martin", "Programación", "demo-1"},
            {"The Pragmatic Programmer", "Andrew Hunt", "Programación", "demo-2"},
            {"Diseño de Interfaces", "Steve Krug", "Diseño", "demo-3"},
            {"Algoritmos y Estructuras", "Cormen et al.", "Matemáticas", "demo-4"},
            {"Spring Boot en Acción", "Craig Walls", "Programación", "demo-5"},
            {"El Método Lean Startup", "Eric Ries", "Negocios", "demo-6"}
        };
        booksContainer.getChildren().clear();
        for (String[] d : demos) {
            booksContainer.getChildren().add(crearTarjetaLibro(d[0], d[1], d[2], d[3]));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Parsers JSON
    // ─────────────────────────────────────────────────────────────────────────

    /** Devuelve: [title, author, category, isbn, id] */
    private List<String[]> parsearLibros(String json) {
        List<String[]> lista = new ArrayList<>();
        if (json == null || json.isBlank()) return lista;
        for (String obj : dividirObjetos(json)) {
            lista.add(new String[]{
                campo(obj, "title"),
                campo(obj, "author"),
                campo(obj, "category"),
                campo(obj, "isbn"),
                campo(obj, "id")
            });
        }
        return lista;
    }

    /** Devuelve: [id, bookTitle] para las órdenes del usuario */
    private List<String[]> parsearOrdenes(String json) {
        List<String[]> lista = new ArrayList<>();
        if (json == null || json.isBlank()) return lista;
        for (String obj : dividirObjetos(json)) {
            lista.add(new String[]{
                campo(obj, "id"),
                campo(obj, "bookTitle")
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
