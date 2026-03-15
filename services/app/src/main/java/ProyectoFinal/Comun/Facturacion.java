package ProyectoFinal.Comun;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Clase Facturacion que implementa la interfaz Serializable.
 * Esta clase representa una factura generada por el alquiler de una propiedad.
 */
public class Facturacion implements Serializable {
    /**
     * Identificador único de la factura.
     */
    private int id;
    /**
     * Tarjeta utilizada para el pago.
     */
    private Tarjeta tarjeta;
    /**
     * Usuario que realiza el pago.
     */
    private Usuario usuario;
    /**
     * Propiedad que se alquila.
     */
    private Propiedad propiedad;
    /**
     * Fecha de la factura.
     */
    private LocalDate fecha;
    /**
     * NIF del usuario.
     */
    private String nif;

    /**
     * Constructor de la clase Facturacion.
     *
     * @param id Identificador único de la factura.
     * @param tarjeta Tarjeta utilizada para el pago.
     * @param usuario Usuario que realiza el pago.
     * @param propiedad Propiedad que se alquila.
     * @param fecha Fecha de la factura.
     * @param nif NIF del usuario.
     */
    public Facturacion(int id, Tarjeta tarjeta, Usuario usuario, Propiedad propiedad, LocalDate fecha, String nif) {
        this.id = id;
        this.tarjeta = tarjeta;
        this.usuario = usuario;
        this.propiedad = propiedad;
        this.fecha = fecha;
        this.nif = nif;
    }

    /**
     * Obtiene el identificador único de la factura.
     *
     * @return El identificador único de la factura.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único de la factura.
     *
     * @param id El identificador único de la factura.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene la tarjeta utilizada para el pago.
     *
     * @return La tarjeta utilizada para el pago.
     */
    public Tarjeta getTarjeta() {
        return tarjeta;
    }

    /**
     * Establece la tarjeta utilizada para el pago.
     *
     * @param tarjeta La tarjeta utilizada para el pago.
     */
    public void setTarjeta(Tarjeta tarjeta) {
        this.tarjeta = tarjeta;
    }

    /**
     * Obtiene el usuario que realiza el pago.
     *
     * @return El usuario que realiza el pago.
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Establece el usuario que realiza el pago.
     *
     * @param usuario El usuario que realiza el pago.
     */
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    /**
     * Obtiene la propiedad que se alquila.
     *
     * @return La propiedad que se alquila.
     */
    public Propiedad getPropiedad() {
        return propiedad;
    }

    /**
     * Establece la propiedad que se alquila.
     *
     * @param propiedad La propiedad que se alquila.
     */
    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
    }

    /**
     * Obtiene la fecha de la factura.
     *
     * @return La fecha de la factura.
     */
    public LocalDate getFecha() {
        return fecha;
    }

    /**
     * Establece la fecha de la factura.
     *
     * @param fecha La fecha de la factura.
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    /**
     * Obtiene el NIF del usuario.
     *
     * @return El NIF del usuario.
     */
    public String getNif() {
        return nif;
    }

    /**
     * Establece el NIF del usuario.
     *
     * @param nif El NIF del usuario.
     */
    public void setNif(String nif) {
        this.nif = nif;
    }
}