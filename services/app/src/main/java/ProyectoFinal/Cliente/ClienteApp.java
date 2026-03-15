package ProyectoFinal.Cliente;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ClienteApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            GestorPantallas.lanzar(primaryStage);
        } catch (Exception e) {
            System.err.println("Error al lanzar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}