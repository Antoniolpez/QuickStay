package ProyectoFinal.Comun;

import java.io.Serializable;

/**
 * Clase FotosPropiedad que implementa la interfaz Serializable.
 * Esta clase representa una foto asociada a una propiedad.
 */
public class FotosPropiedad implements Serializable {
    /**
     * Identificador único de la imagen.
     */
    private int id;
    /**
     * Contenido de la imagen en formato de bytes.
     */
    private byte[] imagen;
    /**
     * Formato de la imagen (por ejemplo, "jpg", "png").
     */
    private String formato;
    /**
     * Identificador de la propiedad a la que pertenece la imagen.
     */
    private int idPropiedad;

    /**
     * Constructor de la clase FotosPropiedad.
     *
     * @param id Identificador único de la imagen.
     * @param imagen Contenido de la imagen en formato de bytes.
     * @param formato Formato de la imagen.
     * @param idPropiedad Identificador de la propiedad a la que pertenece la imagen.
     */
    public FotosPropiedad(int id, byte[] imagen, String formato, int idPropiedad) {
        this.id = id;
        this.imagen = imagen;
        this.formato = formato;
        this.idPropiedad = idPropiedad;
    }

    /**
     * Obtiene el identificador único de la imagen.
     *
     * @return El identificador único de la imagen.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único de la imagen.
     *
     * @param id El identificador único de la imagen.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el formato de la imagen.
     *
     * @return El formato de la imagen.
     */
    public String getFormato() {
        return formato;
    }

    /**
     * Establece el formato de la imagen.
     *
     * @param formato El formato de la imagen.
     */
    public void setFormato(String formato) {
        this.formato = formato;
    }

    /**
     * Obtiene el identificador de la propiedad a la que pertenece la imagen.
     *
     * @return El identificador de la propiedad a la que pertenece la imagen.
     */
    public int getIdPropiedad() {
        return idPropiedad;
    }

    /**
     * Establece el identificador de la propiedad a la que pertenece la imagen.
     *
     * @param idPropiedad El identificador de la propiedad a la que pertenece la imagen.
     */
    public void setIdPropiedad(int idPropiedad) {
        this.idPropiedad = idPropiedad;
    }

    /**
     * Obtiene el contenido de la imagen en formato de bytes.
     *
     * @return El contenido de la imagen en formato de bytes.
     */
    public byte[] getImagenBytes() {
        return imagen;
    }

    /**
     * Establece el contenido de la imagen en formato de bytes.
     *
     * @param imagen El contenido de la imagen en formato de bytes.
     */
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }
}