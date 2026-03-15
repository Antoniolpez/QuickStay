package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Cliente.GestorPantallas;
import ProyectoFinal.Cliente.GestorSecurity;
import ProyectoFinal.Cliente.Librerias.ResizeListener;
import ProyectoFinal.Comun.Usuario;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

public class Settings {


    @FXML
    private VBox userSettingsPanel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField surnameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    public TextField phoneField;
    @FXML
    private Pane focusKiller;
    @FXML
    public ImageView fotoPerfil;
    public PasswordField newPasswordField;
    public PasswordField confirmPasswordField;


    @FXML
    public void initialize(){
        usernameField.setText(Principal.getUsuario().getNombre());
        surnameField.setText(Principal.getUsuario().getApellidos());
        emailField.setText(Principal.getUsuario().getEmail());
        fotoPerfil.setOnMouseEntered(event -> fotoPerfil.setOpacity(0.5));
        fotoPerfil.setOnMouseExited(event -> fotoPerfil.setOpacity(1.0));
        fotoPerfil.setImage(new Image(new ByteArrayInputStream(Principal.getUsuario().getImagenUsuario())));
        fotoPerfil.setOnMouseClicked(event -> actualizarFoto());
        phoneField.setText(Principal.getUsuario().getNumTelefono());
    }


    public void settingsHide() {
        GestorPantallas.mostrarPrincipal();
    }
    @FXML
    public void mouseEntered(){
        userSettingsPanel.getScene().setCursor(Cursor.HAND);
    }
    @FXML
    public void mouseExited(){
        userSettingsPanel.getScene().setCursor(Cursor.DEFAULT);
    }

    public void cerrarSesion() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText("¿Estás seguro de que quieres cerrar sesión?");

        alert.getDialogPane().getStylesheets().add(getClass().getResource("/ProyectoFinal/Cliente/pantallaprincipal.css").toExternalForm());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            GestorPantallas.cerrarSesion(false);
        }
    }
    public void systemExit(){
        GestorPantallas.systemExit();
    }
    public void maximizar(){
        Stage primaryStage = (Stage) usernameField.getScene().getWindow();

        if (GestorPantallas.isMaximized()){
            primaryStage.setWidth(Principal.getScreen()[0]);
            primaryStage.setHeight(Principal.getScreen()[1]);
            primaryStage.setX(Principal.getScreen()[2]);
            primaryStage.setY(Principal.getScreen()[3]);
            GestorPantallas.setMaximized(false);
            ResizeListener.setMaximized(false);
        }else{
            GestorPantallas.setMaximized(true);
            ResizeListener.setMaximized(true);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            if (screenBounds.getWidth() != primaryStage.getWidth() && screenBounds.getHeight() != primaryStage.getHeight()) {

                Principal.setScreen(new double[]{primaryStage.getWidth(), primaryStage.getHeight(), primaryStage.getX(), primaryStage.getY()});
            }

            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());



        }
    }
    public void minimizar(){
        Stage primaryStage = (Stage) usernameField.getScene().getWindow();
        primaryStage.setIconified(true);
    }

    public void eliminarCuenta() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar Cuenta");
        alert.setHeaderText("¿Estás seguro de que quieres eliminar tu cuenta?");
        alert.setContentText("Esta acción no se puede deshacer");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            System.out.println("Cuenta eliminada");
            BufferesUser.eliminarCuenta(Principal.getUsuario());
            GestorPantallas.cerrarSesion(false);
        }
    }

    public void actualizarFoto() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            fotoPerfil.setImage(image);
        }
    }

    private byte[] imageToBytes(ImageView imageView, String formato) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, formato, byteOutput);
            System.out.println("Imagen convertida a bytes");
            System.out.println("Tamaño de la imagen: " + byteOutput.size());
            return byteOutput.toByteArray();
        } catch (IOException e) {
            System.err.println("Error al convertir la imagen a bytes: " + e.getMessage());
        }
        return null;
    }

    public void actualizarUsuario() {
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre(usernameField.getText());
        usuarioActualizado.setApellidos(surnameField.getText());
        usuarioActualizado.setEmail(emailField.getText());
        usuarioActualizado.setNumTelefono(phoneField.getText());
        usuarioActualizado.setFechaNacimiento(Principal.getUsuario().getFechaNacimiento());
        if (GestorSecurity.sha512(passwordField.getText()).equals(Principal.getUsuario().getPassword())) {
            passwordField.setStyle("-fx-border-color: white");
            if ((newPasswordField.getText().equals(confirmPasswordField.getText()) && !newPasswordField.getText().isEmpty())) {
                confirmPasswordField.setStyle("-fx-border-color: white");
                newPasswordField.setStyle("-fx-border-color: white");
                String password = GestorSecurity.sha512(newPasswordField.getText());
                usuarioActualizado.setPassword(password);
                passwordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
            } else {
                System.out.println("Las contraseñas no coinciden o los campos están vacios");
                confirmPasswordField.setStyle("-fx-border-color: red");
                newPasswordField.setStyle("-fx-border-color: red");
                return;
            }
        }else{
            if (!passwordField.getText().isEmpty()) {
                passwordField.setStyle("-fx-border-color: red");
                return;
            }
            usuarioActualizado.setPassword(Principal.getUsuario().getPassword());
        }
        try {
            String url = fotoPerfil.getImage().getUrl();
            int indicePunto = url.lastIndexOf(".");
            String formato = indicePunto == -1 ? "" : url.substring(indicePunto + 1);
            byte[] imagenUsuario = imageToBytes(fotoPerfil, formato);
            usuarioActualizado.setImagenUsuario(imagenUsuario);
        }catch (NullPointerException e){
            usuarioActualizado.setImagenUsuario(Principal.getUsuario().getImagenUsuario());
        }
        BufferesUser.actualizarUsuario(usuarioActualizado);
    }
}