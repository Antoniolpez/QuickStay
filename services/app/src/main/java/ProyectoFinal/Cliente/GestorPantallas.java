package ProyectoFinal.Cliente;

import ProyectoFinal.Cliente.Controladores.Principal;
import ProyectoFinal.Cliente.Librerias.Resize;
import ProyectoFinal.Cliente.Librerias.ResizeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class GestorPantallas {

private static Parent pantalla1;
private static Parent pantalla2;
private static Parent pantalla3;
private static Parent pantalla4;
private static StackPane CONTENEDOR = new StackPane();
private static double[] screen;
private static final double[] SCREENPANTALLAINICIO = new double[]{570, 326.4,0,0};
private static final double[] SCREENPANTALLAREGISTRO = new double[]{803.2, 615.2, 0,0};

private static boolean settingsON;
private static boolean maximized = false;
private static Stage primaryStage;
private static Scene scene;
private static boolean recordar;
private static int numeroPantalla = 1;
public static void lanzar(Stage primaryStage) throws Exception {
    primaryStage.getIcons().add(new Image(Objects.requireNonNull(GestorPantallas.class.getResourceAsStream("/ProyectoFinal/imgs/logo.png"))));
    primaryStage.setTitle("Humhouse - Iniciar Sesión");
    settingsON = false;
    GestorPantallas.primaryStage = primaryStage;
    screen = Local.getScreenData();
    scene = new Scene(CONTENEDOR ,SCREENPANTALLAINICIO[0], SCREENPANTALLAINICIO[1]);
    primaryStage.setScene(scene);

    pantalla1 = FXMLLoader.load(Objects.requireNonNull(GestorPantallas.class.getResource("iniciarSesion.fxml")));
    pantalla4 = FXMLLoader.load(Objects.requireNonNull(GestorPantallas.class.getResource("registro.fxml")));
    if (!recordar) {
        pantalla4.setVisible(false);
        CONTENEDOR.getChildren().addAll(pantalla1, pantalla4);
        System.out.println("Cargando pantalla de inicio de sesión...");
        System.out.println("Recordar desactivado");
        GestorPantallas.cambiarTituloVentana(false, true);
        primaryStage.setOnCloseRequest(event -> systemExit());
        primaryStage.show();
    }
}

// Método para mostrar una pantalla específica
public static void mostrarPantalla(int numeroPantalla, boolean guardarEstado) {
    if (guardarEstado) {
        screen[0] = primaryStage.getWidth();
        screen[1] = primaryStage.getHeight();
        screen[2] = primaryStage.getX();
        screen[3] = primaryStage.getY();
    }

    if (numeroPantalla == 4){
        primaryStage.setWidth(SCREENPANTALLAREGISTRO[0]);
        primaryStage.setHeight(SCREENPANTALLAREGISTRO[1]);
        centrarPantalla(true);

        pantalla1.setVisible(false);
        pantalla4.setVisible(true);
    }else if (numeroPantalla == 1){

        primaryStage.setWidth(SCREENPANTALLAINICIO[0]);
        primaryStage.setHeight(SCREENPANTALLAINICIO[1]);
        centrarPantalla(false);
        pantalla4.setVisible(false);
        pantalla1.setVisible(true);
    }
    System.out.println("Mostrando pantalla " + numeroPantalla);
}
public static void mostrarSettings(){
    if (settingsON){
        cargarSettings();
        settingsON = false;
    }
    pantalla2.setVisible(false);
    pantalla3.setVisible(true);
}
public static void mostrarPrincipal(){
    pantalla2.setVisible(true);
    pantalla3.setVisible(false);
}

private static void cargarSettings(){
    CONTENEDOR.getChildren().addAll(pantalla3);
    pantalla3.setVisible(false);
}

public static void cargarPrincipal(){
    try {
        primaryStage.close();
        GestorPantallas.primaryStage = new Stage();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(GestorPantallas.class.getResourceAsStream("/ProyectoFinal/imgs/logo.png"))));
        primaryStage.setTitle("Humhouse - Aplicación de escritorio");
        CONTENEDOR = new StackPane();
        pantalla2 = FXMLLoader.load(Objects.requireNonNull(GestorPantallas.class.getResource("pantallaprincipal.fxml")));
        try {
            maximized = Local.leerLineaEspecifica(3).contains("true");
            if(maximized){
                System.out.println("Pantalla maximizada");
                ResizeListener.setMaximized(true);
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                primaryStage.setX(screenBounds.getMinX());
                primaryStage.setY(screenBounds.getMinY());
                scene = new Scene(CONTENEDOR ,screenBounds.getWidth(), screenBounds.getHeight());
            }else{
                scene = new Scene(CONTENEDOR ,screen[0], screen[1]);
                ResizeListener.setMaximized(false);
            }

        }catch (NullPointerException e){
            maximized = false;
        }



        CONTENEDOR.getChildren().addAll(pantalla2);
        primaryStage.setScene(scene);
        try {
            GestorPantallas.cambiarTituloVentana(Local.leerLineaEspecifica(2).contains("true"), false);
        }catch (NullPointerException e){
            GestorPantallas.cambiarTituloVentana(false, false);
        }
        new Thread(() -> {
            try {
                pantalla3 = FXMLLoader.load(Objects.requireNonNull(GestorPantallas.class.getResource("settings.fxml")));
                settingsON = true;
            } catch (IOException e) {
                System.err.println("Error al cargar la pantalla de ajustes: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        primaryStage.show();
    } catch (IOException e) {
        System.err.println("Error al cargar la pantalla : " + e.getMessage());
        e.printStackTrace();
    }
}
public static void ocultarLogin(){
    pantalla1.setVisible(false);
}


private static void centrarPantalla( boolean registro){
    Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    primaryStage.setX((screenBounds.getWidth() - primaryStage.getWidth()) / 2);
    if (!registro) {
        primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2 - 81);
    }else{
        primaryStage.setY((screenBounds.getHeight() - primaryStage.getHeight()) / 2);
    }
}


public static void cerrarSesion(boolean  inicioErroneo){
    try {
        if (!maximized || inicioErroneo){
            System.out.println("Guardando estado de la pantalla");
            Local.saveStateScreen(primaryStage.getWidth(), primaryStage.getHeight(), primaryStage.getX(), primaryStage.getY());
        }else{
            System.out.println("No se ha guardado el estado de la pantalla");
        }
        CONTENEDOR = new StackPane();
        primaryStage.close();
        recordar = false;
        Local.escribirLineaEspecifica(0, "false", Local.getFileData());
        BufferesUser.getSocket().close();
        BufferesUser.setConectado(false);
        lanzar(new Stage());
        centrarPantalla(false);

    } catch (Exception e) {
        System.err.println("Error al cargar la pantalla de inicio de sesión: " + e.getMessage());
        e.printStackTrace();
    }
}
public static void systemExit(){
    try {
        Local.escribirLineaEspecifica(3, "Pantalla maximizada: " + maximized + " en pantalla " + numeroPantalla , Local.getFileData());

        if (!maximized){
            Local.saveStateScreen(primaryStage.getWidth(), primaryStage.getHeight(), primaryStage.getX(), primaryStage.getY());
        }
    } catch (NullPointerException e){
        System.exit(0);
    }
    System.out.println("Cerrando la aplicación...");
    try {
        if(BufferesUser.getSocket() != null) {
            BufferesUser.getSocket().close();
        }
    } catch (IOException e) {
        System.err.println("Error al cerrar la conexión con el servidor: " + e.getMessage());
    }
    try {
        Principal.getHiloReceptorDatos().interrupt();
    }catch (NullPointerException e){
        System.out.println("El hilo no estaba abierto.");
    }
    System.exit(0);
}
public static void cambiarTituloVentana(boolean windowsMode, boolean inicioSesion){
    ResizeListener.setInicioSesion(inicioSesion);
    if (windowsMode){
        System.out.println("Modo Windows");
        primaryStage.initStyle(StageStyle.DECORATED);
    }else {
        System.out.println("Modo Sin Bordes");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        Resize.addResizeListener(primaryStage);
    }
}
public static Parent getPantalla2(){
    return pantalla2;
}

public static Stage getPrimaryStage() {
    return primaryStage;
}

public static void setRecordar(boolean recordar) {
    GestorPantallas.recordar = recordar;
}

public static boolean isMaximized() {
    return maximized;
}
public static void setMaximized(boolean maximized) {
    GestorPantallas.maximized = maximized;
}

public static void setNumeroPantalla(int numeroPantalla) {
    GestorPantallas.numeroPantalla = numeroPantalla;
}
}

