package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Cliente.GestorPantallas;
import ProyectoFinal.Cliente.GestorSecurity;
import ProyectoFinal.Cliente.Local;
import ProyectoFinal.Comun.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.*;

/**
 * Controlador de la pantalla de inicio.
 */
public class Inicio {

    @FXML
    private TextField nombreUsuarioField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox guardarLogin;
    @FXML
    private Hyperlink modoInvitado;

    /**
     * Método para inicializar la pantalla de inicio.
     * Si hay datos de inicio de sesión guardados, se inicia la sesión automáticamente.
     * Si no hay datos guardados, se muestra la pantalla de inicio de sesión.
     */
    public void initialize() {
        System.out.println("Inicio");
        String linea = Local.leerLineaEspecifica(0);
        if (linea != null){
            String[] datos = linea.split(" ");
            if (datos[0].equals("true")){
                GestorPantallas.setRecordar(true);
                BufferesUser.conexion();
                iniciarSesion(new Usuario(datos[1],datos[1], "null", "null", datos[2],"null","null", null,null,null, null,null,"login"));
                guardarLogin.setSelected(true);
            }else{
                System.out.println("No hay datos guardados");
                Platform.runLater(() -> GestorPantallas.mostrarPantalla(1, false));
            }
        }else{
            System.out.println("No hay datos guardados");
            Platform.runLater(() -> GestorPantallas.mostrarPantalla(1, false));
        }
    }

    /**
     * Método para guardar los datos de inicio de sesión.
     * Si el checkbox 'guardarLogin' está seleccionado, se guardan los datos de inicio de sesión.
     * Si no está seleccionado, se borran los datos de inicio de sesión guardados.
     * @param passwordCifrada La contraseña cifrada del usuario.
     */
    public void guardar(String passwordCifrada){
        if (guardarLogin.isSelected()){
            if (nombreUsuarioField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                guardarLogin.setSelected(false);
                nombreUsuarioField.setStyle("-fx-border-color: red");
                passwordField.setStyle("-fx-border-color: red");
            }else{
                nombreUsuarioField.setStyle("");
                passwordField.setStyle("");
                Local.setGuardar(true);
                Local.setNombreUsuario(nombreUsuarioField.getText());
                Local.setPassword(passwordCifrada);
                Local.loginSave();
            }
        }else{
            Local.setGuardar(false);
            Local.setNombreUsuario("");
            Local.setPassword("");
            Local.loginSave();
        }
    }

    /**
     * Método para gestionar los inicios de sesión.
     * Si se selecciona el modo invitado, se inicia la sesión como invitado.
     * Si no, se inicia la sesión con los datos introducidos por el usuario.
     * @param event El evento de ratón que desencadena este método.
     */
    public void gestionIniciosSesion(MouseEvent event) {
        System.out.println("Gestionando inicio de sesión...");
        BufferesUser.conexion();
        GestorPantallas.ocultarLogin();
        Usuario usuario;
        if (event.getSource().equals(modoInvitado)) {
            usuario = new Usuario("invitado", "invitado", "invitado", "0/0/0000",
                    "Dc6moAXT+FoLPYDu/H2q9g==", "invitado@gmail.com","null",null,
                    null,null, null,null,"login");
            iniciarSesion(usuario);
        } else{
            String nombreUsuario = nombreUsuarioField.getText();
            String password = passwordField.getText();
            usuario = new Usuario(nombreUsuario,nombreUsuario, "null", "null", password,"null",
                    "null",null,null,null,null,null, "login");

            usuario.setPassword(GestorSecurity.sha512(usuario.getPassword()));
            iniciarSesion(usuario);
            guardar(usuario.getPassword());
            Local.setUsuario(usuario);
        }
    }

    /**
     * Método para iniciar la sesión.
     * Se envían los datos del usuario al servidor y se espera una respuesta.
     * Si la respuesta es 'login error', se muestra un error y se cierra la sesión.
     * Si la respuesta es 'login ok', se carga la pantalla principal.
     * @param usuario El usuario que intenta iniciar sesión.
     */
    public void iniciarSesion(Usuario usuario) {
        try {
                try {
                    BufferesUser.getObjectOutputStream().writeObject(usuario);
                    BufferesUser.getObjectOutputStream().flush();
                } catch (RuntimeException e) {
                    System.err.println("Error al crear el ObjectOutputStream: " + e.getMessage());

                }

            System.out.println("Esperando respuesta del servidor...");
            String respuesta = "login error";
            try {
                ObjectInputStream ois = BufferesUser.getObjectInputStream();
                Usuario usuarioRecibido = null;
                try {
                    usuarioRecibido = (Usuario) ois.readObject();
                }catch (NullPointerException e){
                    GestorPantallas.getPrimaryStage().close();
                    loginError();
                    GestorPantallas.cerrarSesion(true);
                }
                Principal.setUsuario(usuarioRecibido);
                respuesta = usuarioRecibido.getAccionServer();
            } catch (ClassNotFoundException e) {
                System.err.println("Error al recibir el usuario: " + e.getMessage());
            }
            System.out.println("Respuesta del servidor: " + respuesta);
            if (respuesta.equals("login error")){
                GestorPantallas.getPrimaryStage().close();
                loginError();
                GestorPantallas.cerrarSesion(true);
            }
            if (respuesta.equals("login ok")){
                GestorPantallas.cargarPrincipal();
                System.out.println("Cambiando a la escena de inicio...");
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para mostrar un error de inicio de sesión.
     * Se muestra una alerta con el mensaje de error.
     */
    public void loginError(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error al iniciar sesión");
        alert.setHeaderText("Error al iniciar sesión");
        alert.setContentText("El nombre de usuario o la contraseña son incorrectos.");
        alert.showAndWait();
    }

    /**
     * Método para cambiar el cursor a mano cuando el ratón entra en un nodo.
     * @param event El evento de ratón que desencadena este método.
     */
    @FXML
    public void mouseEntered(MouseEvent event){
        ((Node) event.getSource()).getScene().setCursor(Cursor.HAND);
    }

    /**
     * Método para cambiar el cursor a predeterminado cuando el ratón sale de un nodo.
     * @param event El evento de ratón que desencadena este método.
     */
    @FXML
    public void mouseExited(MouseEvent event){
        ((Node) event.getSource()).getScene().setCursor(Cursor.DEFAULT);
    }

    /**
     * Método para mostrar la pantalla de registro.
     */
    @FXML
    public void registrarse() {
        GestorPantallas.mostrarPantalla(4, false);
    }

    /**
     * Método para cerrar la aplicación.
     */
    public void systemExit(){
        Platform.exit();
        System.exit(0);
    }
}