package ProyectoFinal.Comun;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Clase ResenaPropiedad que implementa la interfaz Serializable.
 * Esta clase representa una reseña realizada a una propiedad.
 */
public class ResenaPropiedad implements Serializable {
    /**
     * Acción que se realizará en el servidor.
     */
    private String accionServer;
    /**
     * Identificador único de la reseña.
     */
    private int id;
    /**
     * Identificador del usuario que realiza la reseña.
     */
    private String idUsuario;
    /**
     * Identificador de la propiedad a la que se hace la reseña.
     */
    private int idPropiedad;
    /**
     * Fecha en la que se realizó la reseña.
     */
    private LocalDate fecha;
    /**
     * Calificación en estrellas de la reseña.
     */
    private Float estrellas;
    /**
     * Comentario de la reseña.
     */
    private String comentario;

    /**
     * Constructor de la clase ResenaPropiedad.
     *
     * @param id Identificador único de la reseña.
     * @param idUsuario Identificador del usuario que realiza la reseña.
     * @param idPropiedad Identificador de la propiedad a la que se hace la reseña.
     * @param fecha Fecha en la que se realizó la reseña.
     * @param estrellas Calificación en estrellas de la reseña.
     * @param comentario Comentario de la reseña.
     */
    public ResenaPropiedad(int id, String idUsuario, int idPropiedad, LocalDate fecha, Float estrellas, String comentario) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idPropiedad = idPropiedad;
        this.fecha = fecha;
        this.estrellas = estrellas;
        this.comentario = comentario;
    }

    /**
     * Establece la acción que se realizará en el servidor.
     *
     * @param accionServer La acción que se realizará en el servidor.
     */
    public void setAccionServer(String accionServer) {
        this.accionServer = accionServer;
    }

    /**
     * Obtiene la acción que se realizará en el servidor.
     *
     * @return La acción que se realizará en el servidor.
     */
    public String getAccionServer() {
        return accionServer;
    }

    /**
     * Obtiene el identificador único de la reseña.
     *
     * @return El identificador único de la reseña.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único de la reseña.
     *
     * @param id El identificador único de la reseña.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el identificador del usuario que realiza la reseña.
     *
     * @return El identificador del usuario que realiza la reseña.
     */
    public String getUsuario() {
        return idUsuario;
    }

    /**
     * Establece el identificador del usuario que realiza la reseña.
     *
     * @param idUsuario El identificador del usuario que realiza la reseña.
     */
    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtiene el identificador de la propiedad a la que se hace la reseña.
     *
     * @return El identificador de la propiedad a la que se hace la reseña.
     */
    public int getIdPropiedad() {
        return idPropiedad;
    }

    /**
     * Establece el identificador de la propiedad a la que se hace la reseña.
     *
     * @param idPropiedad El identificador de la propiedad a la que se hace la reseña.
     */
    public void setIdPropiedad(int idPropiedad) {
        this.idPropiedad = idPropiedad;
    }

    /**
     * Obtiene la fecha en la que se realizó la reseña.
     *
     * @return La fecha en la que se realizó la reseña.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha en la que se realizó la reseña.
     *
     * @param fecha La fecha en la que se realizó la reseña.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene la calificación en estrellas de la reseña.
     *
     * @return La calificación en estrellas de la reseña.
     */
    public Float getEstrellas() {
        return estrellas;
    }

    /**
     * Establece la calificación en estrellas de la reseña.
     *
     * @param estrellas La calificación en estrellas de la reseña.
     */
    public void setEstrellas(Float estrellas) {
        this.estrellas = estrellas;
    }

    /**
     * Obtiene el comentario de la reseña.
     *
     * @return El comentario de la reseña.
     */
    public String getComentario() {
        return comentario;
    }

    /**
     * Establece el comentario de la reseña.
     *
     * @param comentario El comentario de la reseña.
     */
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}