package ProyectoFinal.Cliente.Celdas;


import ProyectoFinal.Comun.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;

/**
 * Clase ContactosCeldas
 * Esta clase se encarga de manejar la información de los contactos en la interfaz de usuario.
 * Cada instancia de esta clase representa una celda en la lista de contactos.
 */
public class ContactosCeldas {

    @FXML
    private Label nombreContacto;

    @FXML
    private ImageView imagenContacto;

    /**
     * Método setInfo
     * Este método se encarga de establecer la información del usuario en la celda de contacto.
     * @param usuario El usuario cuya información se va a establecer en la celda de contacto.
     */
    public void setInfo(Usuario usuario) {
        nombreContacto.setText(usuario.getNombre()); // Establecer el nombre del usuario en la etiqueta de nombre de contacto

        // Si el usuario tiene una imagen, establecer la imagen en la vista de imagen de contacto
        if (usuario.getImagenUsuario().length > 0) {
            imagenContacto.setImage(new Image(new ByteArrayInputStream(usuario.getImagenUsuario())));
            imagenContacto.setFitHeight(50);
            imagenContacto.setFitWidth(50);
            imagenContacto.setPreserveRatio(true);
            imagenContacto.setSmooth(true);
            imagenContacto.setCache(true);
            imagenContacto.setVisible(true);
        }
    }
}