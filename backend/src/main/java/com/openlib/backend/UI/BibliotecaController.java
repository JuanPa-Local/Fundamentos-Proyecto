package com.openlib.backend.UI;

import com.openlib.backend.application.usecase.VerBibliotecaUseCase;
import com.openlib.backend.domain.order.LibroBiblioteca;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BibliotecaController {

    @FXML private FlowPane booksContainer;
    @FXML private Label statusLabel;

    private final VerBibliotecaUseCase verBibliotecaUseCase;

    public BibliotecaController(VerBibliotecaUseCase verBibliotecaUseCase) {
        this.verBibliotecaUseCase = verBibliotecaUseCase;
    }

    @FXML
    public void initialize() {
        cargarBiblioteca();
    }

    private void cargarBiblioteca() {
        try {
            UUID currentBuyerId = SessionManager.getInstance().getCurrentUser().getId();
            List<LibroBiblioteca> libros = verBibliotecaUseCase.ejecutar(currentBuyerId);

            booksContainer.getChildren().clear();

            if (libros.isEmpty()) {
                statusLabel.setText("Tu biblioteca está vacía. ¡Explora el catálogo!");
                return;
            }

            statusLabel.setText("Tus libros adquiridos (" + libros.size() + ")");

            for (LibroBiblioteca libro : libros) {
                VBox card = new VBox(10);
                card.setAlignment(Pos.CENTER);
                card.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #3a3a3a; -fx-border-radius: 8;");
                card.setPrefWidth(180);

                // Portada
                ImageView coverView = new ImageView();
                coverView.setFitWidth(120);
                coverView.setFitHeight(160);
                coverView.setPreserveRatio(true);

                // Título y Autor
                Label titleLbl = new Label(libro.getTitulo());
                titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");
                titleLbl.setWrapText(true);
                
                Label authorLbl = new Label(libro.getAutor());
                authorLbl.setStyle("-fx-text-fill: #bbbbbb; -fx-font-size: 12px;");

                // Botón Leer (placeholder o acción futura)
                Button leerBtn = new Button("Leer / Descargar");
                leerBtn.setStyle("-fx-background-color: #8FC234; -fx-text-fill: #000000; -fx-font-weight: bold; -fx-background-radius: 5;");
                leerBtn.setOnAction(e -> {
                    // Aquí se integrará con US-020 (Descargas)
                    System.out.println("Descargar libro " + libro.getLibroId());
                });

                card.getChildren().addAll(coverView, titleLbl, authorLbl, leerBtn);
                booksContainer.getChildren().add(card);
            }

        } catch (Exception e) {
            statusLabel.setText("Error al cargar la biblioteca: " + e.getMessage());
        }
    }

    @FXML
    private void goToCatalogo() {
        ViewFactory.loadView("buyer-view.fxml");
    }
}
