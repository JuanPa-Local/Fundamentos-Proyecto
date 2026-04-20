package com.openlib.backend.UI;

import com.openlib.backend.BackendApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApp extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        // Inicia el contexto de Spring Boot
        this.context = new SpringApplicationBuilder()
                .sources(BackendApplication.class)
                .run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Cargar el archivo FXML desde resources/views/
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));

        // Inyecta los beans de Spring (Services/Repositories) en los controladores de JavaFX
        loader.setControllerFactory(context::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root);

        // 2. Cargar el estilo CSS desde resources/styles/
        if (getClass().getResource("/styles/global.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
        }

        primaryStage.setTitle("OpenLib Market - Entrega 1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Apaga Spring al cerrar la ventana para liberar puertos (Postgres/Redis)
        this.context.close();
        Platform.exit();
        System.exit(0);
    }
}