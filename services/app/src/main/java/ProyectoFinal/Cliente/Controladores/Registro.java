package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Cliente.GestorPantallas;
import ProyectoFinal.Cliente.GestorSecurity;
import ProyectoFinal.Cliente.Librerias.NumerosTelefono;
import ProyectoFinal.Cliente.Local;
import ProyectoFinal.Comun.Usuario;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

public class Registro {

    @FXML
    private TextField userTextField;
    @FXML
    private TextField aliasTextField;
    @FXML
    private TextField apellidoTextField;
    @FXML
    private PasswordField pwBox;
    @FXML
    private PasswordField pwBoxRep;
    @FXML
    private DatePicker fechaNacimientoField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField numTelefonoTextField;
    @FXML
    private ImageView profilePicView;
    @FXML
    private Label errorLabel;
    private static boolean conectado = false;

    private static final ArrayList<NumerosTelefono> numerosTelefonoArrayList = Local.leerPrefijosNumerosTelefono();

    public void initialize() {
        errorLabel.setText("");
        errorLabel.setStyle("-fx-text-fill: red");
        numTelefonoTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[+\\d]*")) {
                numTelefonoTextField.setText(newValue.replaceAll("[^+\\d]", ""));
            }
        });
    }

    @FXML
    protected void handleUploadButtonAction() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            profilePicView.setImage(image);
        }
    }

    public void limpiarStyle(){
        userTextField.setStyle("-fx-border-color: none");
        errorLabel.setText("");
    }


    @FXML
    public void handleRegisterButtonAction() {
        if (estanVacios()) {
            String id = userTextField.getText();
            String nombre = aliasTextField.getText();
            String apellidos = apellidoTextField.getText();
            String email = emailTextField.getText();
            String password = pwBox.getText();
            String passwordRep = pwBoxRep.getText();
            if (!password.equals(passwordRep)){
                errorLabel.setText("Las contraseñas no coinciden");
                pwBox.setStyle("-fx-border-color: red");
                pwBoxRep.setStyle("-fx-border-color: red");
                return;
            }
            String fechaNacimiento = fechaNacimientoField.getValue().toString();
            String numTelefono = numTelefonoTextField.getText();
            boolean tienePrefijo = false;
            for (NumerosTelefono numero : numerosTelefonoArrayList){
                if (numTelefono.startsWith(numero.getPrefijo())){
                    tienePrefijo = true;
                    break;
                }
            }
            if (!tienePrefijo){
                errorLabel.setText("El número de teléfono no tiene prefijo");
                numTelefonoTextField.setStyle("-fx-border-color: red");
                return;
            }
            String encryptedBytes = null;
            String url = profilePicView.getImage().getUrl();
            int indicePunto = url.lastIndexOf(".");
            String formato = indicePunto == -1 ? "" : url.substring(indicePunto + 1);
            byte[] imagenUsuario = imageToBytes(profilePicView, formato);
            encryptedBytes = GestorSecurity.sha512(password);
            try {
                BufferesUser.conexion();
                if (!conectado) {
                    try {
                        ObjectOutputStream oos = BufferesUser.getObjectOutputStream();
                        Usuario usuario = new Usuario(id, nombre, apellidos, fechaNacimiento, encryptedBytes , email,numTelefono,
                                null, imagenUsuario,null,null,null ,"registro");

                        oos.writeObject(usuario);
                        oos.flush();
                    } catch (RuntimeException e) {
                        System.err.println("Error al crear el ObjectOutputStream: " + e.getMessage());
                    }
                }
                Usuario usuarioRecibido;
                String respuesta = "Este usuario ya está registrado";
                try {
                    usuarioRecibido = (Usuario) BufferesUser.getObjectInputStream().readObject();
                    respuesta = usuarioRecibido.getAccionServer();
                } catch (ClassNotFoundException e) {
                    System.err.println("Error al recibir el usuario: " + e.getMessage());
                }
                System.out.println("Respuesta del servidor: " + respuesta);
                if (respuesta.equals("Este usuario ya está registrado")){
                    userTextField.setStyle("-fx-border-color: red");
                    errorLabel.setText("Este usuario ya está registrado");
                }else{
                    // Cargar la escena de inicio
                    iniciarSesion();
                }

            } catch (IOException e) {
                System.err.println("Error al enviar el usuario: " + e.getMessage());
            }
        }else{
            errorLabel.setText("Rellena todos los campos");
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

    private boolean estanVacios() {
        return !userTextField.getText().isEmpty() &&
                !aliasTextField.getText().isEmpty() &&
                !apellidoTextField.getText().isEmpty() &&
                !fechaNacimientoField.getValue().toString().isEmpty() &&
                !pwBox.getText().isEmpty() && !pwBoxRep.getText().isEmpty() &&
                !emailTextField.getText().isEmpty() && !numTelefonoTextField.getText().isEmpty();
    }

    public void iniciarSesion(){
        GestorPantallas.mostrarPantalla(1, false);
    }
}