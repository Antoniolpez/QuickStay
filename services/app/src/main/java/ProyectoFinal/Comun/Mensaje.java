package ProyectoFinal.Comun;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clase Mensaje que implementa la interfaz Serializable.
 * Esta clase representa un mensaje enviado de un usuario a otro.
 */
public class Mensaje implements Serializable {
    /**
     * Usuario que envía el mensaje.
     */
    private final Usuario usuarioEmisor;
    /**
     * Contenido del mensaje.
     */
    private final String  mensaje;
    /**
     * Usuario que recibe el mensaje.
     */
    private final Usuario usuarioReceptor;
    /**
     * Fecha y hora de llegada del mensaje.
     */
    private final LocalDateTime fechaLlegada;

    /**
     * Constructor de la clase Mensaje.
     *
     * @param usuarioEmisor Usuario que envía el mensaje.
     * @param mensaje Contenido del mensaje.
     * @param usuarioReceptor Usuario que recibe el mensaje.
     * @param fecha Fecha y hora de llegada del mensaje.
     */
    public Mensaje(Usuario usuarioEmisor, String mensaje, Usuario usuarioReceptor, LocalDateTime fecha) {
        this.usuarioEmisor = usuarioEmisor;
        this.mensaje = mensaje;
        this.usuarioReceptor = usuarioReceptor;
        this.fechaLlegada = fecha;
    }

    /**
     * Obtiene el usuario que envía el mensaje.
     *
     * @return El usuario que envía el mensaje.
     */
    public Usuario getUsuarioEmisor() {
        return usuarioEmisor;
    }

    /**
     * Obtiene el usuario que recibe el mensaje.
     *
     * @return El usuario que recibe el mensaje.
     */
    public Usuario getUsuarioReceptor() {
        return usuarioReceptor;
    }

    /**
     * Obtiene el contenido del mensaje.
     *
     * @return El contenido del mensaje.
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * Obtiene la fecha y hora de llegada del mensaje.
     *
     * @return La fecha y hora de llegada del mensaje.
     */
    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }
}