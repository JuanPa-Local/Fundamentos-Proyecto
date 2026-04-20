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
    @FXML private TextField searchUserField;

    // ── Tabla libros
    @FXML private TableView<String[]> booksAdminTable;
    @FXML private TableColumn<String[], String> bColTitle;
    @FXML private TableColumn<String[], String> bColAuthor;
    @FXML private TableColumn<String[], String> bColCategory;
    @FXML private TableColumn<String[], String> bColIsbn;
    @FXML private TableColumn<String[], String> bColDate;
    @FXML private TextField searchBookField;

    // ── Tabla órdenes completa
    @FXML private TableView<String[]> ordersTable;
    @FXML private TableColumn<String[], String> oColId;
    @FXML private TableColumn<String[], String> oColUser;
    @FXML private TableColumn<String[], String> oColBook;
    @FXML private TableColumn<String[], String> oColStatus;
    @FXML private TableColumn<String[], String> oColDate;
    @FXML private Label totalOrdersLabel;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ConfigurableApplicationContext springContext;

    public AdminController(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarDashboard();
    }

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

        // Libros
        bColTitle.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue()[0]));
        bColAuthor.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[1]));
        bColCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[2]));
        bColIsbn.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[3]));
        bColDate.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[4]));

        // Órdenes
        oColId.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[0]));
        oColUser.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[1]));
        oColBook.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[2]));
        oColStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));
        oColDate.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[4]));
    }

    private void cargarDashboard() {
        new Thread(() -> {
            try {
                // Llamadas paralelas al backend
                HttpResponse<String> usersResp = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/users")).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
                );
                HttpResponse<String> booksResp = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/books")).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
                );
                HttpResponse<String> ordersResp = httpClient.send(
                    HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/orders")).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
                );

                Platform.runLater(() -> {
                    // TODO: parsear los JSON para sacar conteos reales
                    // Por ahora mostramos datos de ejemplo
                    lblUsuarios.setText("3");
                    lblLibros.setText("6");
                    lblOrdenes.setText("12");

                    ObservableList<String[]> recentData = FXCollections.observableArrayList(
                        new String[]{"juan@openlib.com", "Clean Code", "20/04/2026 09:30"},
                        new String[]{"maria@openlib.com", "Refactoring", "20/04/2026 08:15"},
                        new String[]{"carlos@openlib.com", "Algoritmos", "19/04/2026 17:45"}
                    );
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

    // ── Navegación entre paneles ──

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

        ObservableList<String[]> data = FXCollections.observableArrayList(
            new String[]{"1", "Juan Passos", "juan@openlib.com", "BUYER", "01/04/2026"},
            new String[]{"2", "María López", "maria@openlib.com", "SELLER", "05/04/2026"},
            new String[]{"3", "Admin User", "admin@openlib.com", "ADMIN", "01/01/2026"}
        );
        usersTable.setItems(data);
    }

    @FXML private void showLibros() {
        panelDashboard.setVisible(false);
        panelUsuarios.setVisible(false);
        panelLibros.setVisible(true);
        panelOrdenes.setVisible(false);

        ObservableList<String[]> data = FXCollections.observableArrayList(
            new String[]{"Clean Code", "Robert C. Martin", "Programación", "978-01-36", "15/04/2026"},
            new String[]{"Refactoring", "Martin Fowler", "Programación", "978-02-47", "16/04/2026"},
            new String[]{"Diseño UX", "Steve Krug", "Diseño", "978-03-58", "17/04/2026"}
        );
        booksAdminTable.setItems(data);
    }

    @FXML private void showOrdenes() {
        panelDashboard.setVisible(false);
        panelUsuarios.setVisible(false);
        panelLibros.setVisible(false);
        panelOrdenes.setVisible(true);

        ObservableList<String[]> data = FXCollections.observableArrayList(
            new String[]{"ORD-001", "juan@openlib.com", "Clean Code", "COMPLETADA", "20/04/2026 09:30"},
            new String[]{"ORD-002", "maria@openlib.com", "Refactoring", "COMPLETADA", "20/04/2026 08:15"},
            new String[]{"ORD-003", "carlos@openlib.com", "Algoritmos", "COMPLETADA", "19/04/2026 17:45"}
        );
        ordersTable.setItems(data);
        totalOrdersLabel.setText(data.size() + " órdenes");
    }

    @FXML private void buscarUsuario() {
        // TODO: filtrar usersTable por searchUserField
    }

    @FXML private void buscarLibro() {
        // TODO: filtrar booksAdminTable por searchBookField
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
            Stage stage = (Stage) lblUsuarios.getScene().getWindow();
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
