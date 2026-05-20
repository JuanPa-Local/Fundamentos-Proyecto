package com.openlib.backend.UI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ViewFactory {

    private final ConfigurableApplicationContext springContext;

    public ViewFactory(ConfigurableApplicationContext springContext) {
        this.springContext = springContext;
    }

    public void showRoleView(String role, Stage stage) {
        String view = "/views/main-view.fxml";
        if ("ADMIN".equalsIgnoreCase(role)) {
            view = "/views/admin-view.fxml";
        } else if ("SELLER".equalsIgnoreCase(role)) {
            view = "/views/seller-view.fxml";
        } else if ("BUYER".equalsIgnoreCase(role)) {
            view = "/views/buyer-view.fxml";
        }
        cambiarPantalla(view, stage);
    }

    public void showView(String fxmlPath, Stage stage) {
        cambiarPantalla(fxmlPath, stage);
    }

    private void cambiarPantalla(String fxmlPath, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            if (getClass().getResource("/styles/global.css") != null) {
                scene.getStylesheets().add(
                    getClass().getResource("/styles/global.css").toExternalForm()
                );
            }
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
