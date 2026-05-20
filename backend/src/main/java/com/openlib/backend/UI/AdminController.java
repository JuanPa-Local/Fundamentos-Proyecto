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
import javafx.scene.layout.VBox;
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
public class AdminController implements Initializable {

    // ── Métricas
    @FXML private Label lblUsuarios;
    @FXML private Label lblLibros;
    @FXML private Label lblOrdenes;

    // ── Paneles de navegación
    @FXML private VBox panelDashboard;
    @FXML private VBox panelUsuarios;
    @FXML private VBox panelLibros;
    @FXML private VBox panelOrdenes;

    // ── Tabla órdenes recientes (dashboard)
    @FXML private TableView<String[]> recentOrdersTable;
    @FXML private TableColumn<String[], String> rColUser;
    @FXML private TableColumn<String[], String> rColBook;
    @FXML private TableColumn<String[], String> rColDate;

    // ── Tabla usuarios
    @FXML private TableView<String[]> usersTable;
    @FXML private TableColumn<String[], String> uColId;
    @FXML private TableColumn<String[], String> uColName;
    @FXML private TableColumn<String[], String> uColEmail;
    @FXML private TableColumn<String[], String> uColRole;
    @FXML private TableColumn<String[], String> uColDate;
    @FXML private TableColumn<String[], String> uColActions;
    @FXML private TextField searchUserField;

    // ── Tabla libros
    @FXML private TableView<String[]> booksAdminTable;
    @FXML private TableColumn<String[], String> bColTitle;
    @FXML private TableColumn<String[], String> bColAuthor;
    @FXML private TableColumn<String[], String> bColCategory;
    @FXML private TableColumn<String[], String> bColIsbn;
    @FXML private TableColumn<String[], String> bColDate;
    @FXML private TableColumn<String[], String> bColActions;
    @FXML private TextField searchBookField;

    // ── Tabla órdenes completa
    @FXML private TableView<String[]> ordersTable;
    @FXML private TableColumn<String[], String> oColId;
    @FXML private TableColumn<String[], String> oColUser;
    @FXML private TableColumn<String[], String> oColBook;
    @FXML private TableColumn<String[], String> oColStatus;
    @FXML private TableColumn<String[], String> oColDate;
    @FXML private Label totalOrdersLabel;

    // Listas completas para filtrado local
    private ObservableList<String[]> todosLosUsuarios  = FXCollections.observableArrayList();
    private ObservableList<String[]> todosLosLibros    = FXCollections.observableArrayList();

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConfigurableApplicationContext springContext;
    private final ViewFactory viewFactory;

    public AdminController(ConfigurableApplicationContext springContext, ViewFactory viewFactory) {
        this.springContext = springContext;
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarDashboard();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Configuración de columnas
    // ─────────────────────────────────────────────────────────────────────────

    private void configurarColumnas() {
        // Órdenes recientes
        rColUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[0]));
        rColBook.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[1]));
        rColDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));

        // Usuarios
        uColId.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue()[0]));
        uColName.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue()[1]));
        uColEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        uColRole.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue()[3]));
        uColDate.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue()[4]));
        uColActions.setCellFactory(col  -> crearCeldaAccionesUsuario());

        // Libros
        bColTitle.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue()[0]));
        bColAuthor.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[1]));
        bColCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        bColIsbn.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[3]));
        bColDate.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[4]));
        bColActions.setCellFactory(col     -> crearCeldaAccionesLibro());

        // Órdenes
        oColId.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[0]));
        oColUser.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[1]));
        oColBook.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[2]));
        oColStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        oColDate.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[4]));
    }

    /** Celda con botón "Eliminar" para la tabla de usuarios */
    private TableCell<String[], String> crearCeldaAccionesUsuario() {
        return new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle(
                    "-fx-background-color: #cf667933; -fx-text-fill: #cf6679;" +
                    "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"
                );
                btnEliminar.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    confirmarEliminarUsuario(row[0], row[1]);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        };
    }

    /** Celda con botón "Eliminar" para la tabla de libros */
    private TableCell<String[], String> crearCeldaAccionesLibro() {
        return new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle(
                    "-fx-background-color: #cf667933; -fx-text-fill: #cf6679;" +
                    "-fx-background-radius: 6; -fx-padding: 4 10; -fx-cursor: hand;"
                );
                btnEliminar.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    confirmarEliminarLibro(row[3], row[0]); // isbn, titulo
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carga de datos desde el backend
    // ─────────────────────────────────────────────────────────────────────────

    private void cargarDashboard() {
        new Thread(() -> {
            try {
                HttpResponse<String> usersResp = get("/api/users");
                HttpResponse<String> booksResp = get("/api/books");
                HttpResponse<String> ordersResp = get("/api/orders");

                List<String[]> usuarios = parsearUsuarios(usersResp.body());
                List<String[]> libros   = parsearLibros(booksResp.body());
                List<String[]> ordenes  = parsearOrdenes(ordersResp.body());

                // Recientes: últimas 5 órdenes
                List<String[]> recientes = ordenes.size() > 5
                    ? ordenes.subList(ordenes.size() - 5, ordenes.size())
                    : ordenes;

                Platform.runLater(() -> {
                    lblUsuarios.setText(String.valueOf(usuarios.size()));
                    lblLibros.setText(String.valueOf(libros.size()));
                    lblOrdenes.setText(String.valueOf(ordenes.size()));

                    ObservableList<String[]> recentData = FXCollections.observableArrayList();
                    for (String[] o : recientes) {
                        // [id, userEmail, bookTitle, status, date] → mostramos [userEmail, bookTitle, date]
                        recentData.add(new String[]{o[1], o[2], o[4]});
                    }
                    recentOrdersTable.setItems(recentData);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblUsuarios.setText("—");
                    lblLibros.setText("—");
                    lblOrdenes.setText("—");
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navegación entre paneles
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void showDashboard() {
        panelDashboard.setVisible(true);
        panelUsuarios.setVisible(false);
        panelLibros.setVisible(false);
        panelOrdenes.setVisible(false);
        cargarDashboard();
    }

    @FXML private void showUsuarios() {
        panelDashboard.setVisible(false);
        panelUsuarios.setVisible(true);
        panelLibros.setVisible(false);
        panelOrdenes.setVisible(false);

        new Thread(() -> {
            try {
                HttpResponse<String> resp = get("/api/users");
                List<String[]> usuarios = parsearUsuarios(resp.body());
                Platform.runLater(() -> {
                    todosLosUsuarios = FXCollections.observableArrayList(usuarios);
                    usersTable.setItems(todosLosUsuarios);
                });
            } catch (Exception e) {
                Platform.runLater(() -> usersTable.setItems(FXCollections.emptyObservableList()));
            }
        }).start();
    }

    @FXML private void showLibros() {
        panelDashboard.setVisible(false);
        panelUsuarios.setVisible(false);
        panelLibros.setVisible(true);
        panelOrdenes.setVisible(false);

        new Thread(() -> {
            try {
                HttpResponse<String> resp = get("/api/books");
                List<String[]> libros = parsearLibros(resp.body());
                Platform.runLater(() -> {
                    todosLosLibros = FXCollections.observableArrayList(libros);
                    booksAdminTable.setItems(todosLosLibros);
                });
            } catch (Exception e) {
                Platform.runLater(() -> booksAdminTable.setItems(FXCollections.emptyObservableList()));
            }
        }).start();
    }

    @FXML private void showOrdenes() {
        panelDashboard.setVisible(false);
        panelUsuarios.setVisible(false);
        panelLibros.setVisible(false);
        panelOrdenes.setVisible(true);

        new Thread(() -> {
            try {
                HttpResponse<String> resp = get("/api/orders");
                List<String[]> ordenes = parsearOrdenes(resp.body());
                Platform.runLater(() -> {
                    ObservableList<String[]> data = FXCollections.observableArrayList(ordenes);
                    ordersTable.setItems(data);
                    totalOrdersLabel.setText(data.size() + " órdenes");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ordersTable.setItems(FXCollections.emptyObservableList());
                    totalOrdersLabel.setText("Sin datos");
                });
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Búsqueda / filtrado local
    // ─────────────────────────────────────────────────────────────────────────

    @FXML private void buscarUsuario() {
        String q = searchUserField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            usersTable.setItems(todosLosUsuarios);
            return;
        }
        ObservableList<String[]> filtrado = FXCollections.observableArrayList();
        for (String[] row : todosLosUsuarios) {
            // Busca en nombre, email y rol
            if (row[1].toLowerCase().contains(q)
             || row[2].toLowerCase().contains(q)
             || row[3].toLowerCase().contains(q)) {
                filtrado.add(row);
            }
        }
        usersTable.setItems(filtrado);
    }

    @FXML private void buscarLibro() {
        String q = searchBookField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            booksAdminTable.setItems(todosLosLibros);
            return;
        }
        ObservableList<String[]> filtrado = FXCollections.observableArrayList();
        for (String[] row : todosLosLibros) {
            // Busca en título, autor, categoría e ISBN
            if (row[0].toLowerCase().contains(q)
             || row[1].toLowerCase().contains(q)
             || row[2].toLowerCase().contains(q)
             || row[3].toLowerCase().contains(q)) {
                filtrado.add(row);
            }
        }
        booksAdminTable.setItems(filtrado);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Eliminación con confirmación
    // ─────────────────────────────────────────────────────────────────────────

    private void confirmarEliminarUsuario(String id, String nombre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar al usuario \"" + nombre + "\"? Esta acción no se puede deshacer.",
            ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) eliminarUsuario(id);
        });
    }

    private void eliminarUsuario(String id) {
        new Thread(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users/" + id))
                    .header("Authorization", SessionManager.getInstance().bearerHeader())
                    .DELETE()
                    .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(this::showUsuarios);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void confirmarEliminarLibro(String isbn, String titulo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Eliminar el libro \"" + titulo + "\"? Esta acción no se puede deshacer.",
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
                    .header("Authorization", SessionManager.getInstance().bearerHeader())
                    .DELETE()
                    .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(this::showLibros);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logout
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().clear();
        viewFactory.showView("/views/main-view.fxml", (Stage) lblUsuarios.getScene().getWindow());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers de parsing JSON simple (sin dependencias externas)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Parsea un array JSON de usuarios.
     * Formato esperado por objeto:
     * {"id":"1","fullName":"...","email":"...","role":"...","createdAt":"..."}
     * Devuelve: [id, fullName, email, role, createdAt]
     */
    private List<String[]> parsearUsuarios(String json) {
        List<String[]> lista = new ArrayList<>();
        if (json == null || json.isBlank()) return lista;
        for (String obj : dividirObjetos(json)) {
            lista.add(new String[]{
                campo(obj, "id"),
                campo(obj, "fullName"),
                campo(obj, "email"),
                campo(obj, "role"),
                campo(obj, "createdAt")
            });
        }
        return lista;
    }

    /**
     * Parsea un array JSON de libros.
     * Formato esperado: {"id":"1","title":"...","author":"...","category":"...","isbn":"...","createdAt":"..."}
     * Devuelve: [title, author, category, isbn, createdAt]
     */
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

    /**
     * Parsea un array JSON de órdenes.
     * Formato esperado: {"id":"...","userEmail":"...","bookTitle":"...","status":"...","createdAt":"..."}
     * Devuelve: [id, userEmail, bookTitle, status, createdAt]
     */
    private List<String[]> parsearOrdenes(String json) {
        List<String[]> lista = new ArrayList<>();
        if (json == null || json.isBlank()) return lista;
        for (String obj : dividirObjetos(json)) {
            lista.add(new String[]{
                campo(obj, "id"),
                campo(obj, "userEmail"),
                campo(obj, "bookTitle"),
                campo(obj, "status"),
                campo(obj, "createdAt")
            });
        }
        return lista;
    }

    /**
     * Divide un JSON array "[{...},{...}]" en una lista de objetos JSON "{...}".
     */
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

    /** Extrae el valor de un campo string de un objeto JSON simple. */
    private String campo(String obj, String clave) {
        String buscar = "\"" + clave + "\"";
        int idx = obj.indexOf(buscar);
        if (idx < 0) return "";
        int after = idx + buscar.length();
        // Saltar espacios y ':'
        while (after < obj.length() && (obj.charAt(after) == ':' || obj.charAt(after) == ' ')) after++;
        if (after >= obj.length()) return "";
        char first = obj.charAt(after);
        if (first == '"') {
            int end = obj.indexOf('"', after + 1);
            return end > after ? obj.substring(after + 1, end) : "";
        } else {
            // Valor numérico o booleano
            int end = after;
            while (end < obj.length() && obj.charAt(end) != ',' && obj.charAt(end) != '}') end++;
            return obj.substring(after, end).trim();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navegación
    // ─────────────────────────────────────────────────────────────────────────

    private final HttpClient httpClientField = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080" + path))
            .header("Authorization", SessionManager.getInstance().bearerHeader())
            .GET()
            .build();
        return httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    }


}
