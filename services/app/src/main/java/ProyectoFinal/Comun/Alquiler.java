package ProyectoFinal.Comun;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clase Alquiler que implementa la interfaz Serializable.
 * Esta clase representa un alquiler de una propiedad por un usuario.
 */
public class Alquiler implements Serializable {
    /**
     * Acción que se realizará en el servidor.
     */
    private String accionServer;
    /**
     * Identificador único del alquiler.
     */
    private int id;
    /**
     * Usuario que realiza el alquiler.
     */
    private Usuario usuarioAlquila;
    /**
     * Propiedad que se alquila.
     */
    private Propiedad propiedadAlquilada;
    /**
     * Número de personas para las que se realiza el alquiler.
     */
    private int numPersonas;
    /**
     * Fecha y hora de inicio del alquiler.
     */
    private LocalDateTime checkin;
    /**
     * Fecha y hora de finalización del alquiler.
     */
    private LocalDateTime checkout;
    /**
     * Coste total del alquiler.
     */
    private final float totalCoste;

    /**
     * Constructor de la clase Alquiler.
     *
     * @param id Identificador único del alquiler.
     * @param usuarioAlquila Usuario que realiza el alquiler.
     * @param propiedadAlquilada Propiedad que se alquila.
     * @param numPersonas Número de personas para las que se realiza el alquiler.
     * @param checkin Fecha y hora de inicio del alquiler.
     * @param checkout Fecha y hora de finalización del alquiler.
     * @param totalCoste Coste total del alquiler.
     */
    public Alquiler(int id, Usuario usuarioAlquila, Propiedad propiedadAlquilada, int numPersonas, LocalDateTime checkin, LocalDateTime checkout, float totalCoste) {
        this.id = id;
        this.usuarioAlquila = usuarioAlquila;
        this.propiedadAlquilada = propiedadAlquilada;
        this.numPersonas = numPersonas;
        this.checkin = checkin;
        this.checkout = checkout;
        this.totalCoste = totalCoste;
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
     * Establece la acción que se realizará en el servidor.
     *
     * @param accionServer La acción que se realizará en el servidor.
     */
    public void setAccionServer(String accionServer) {
        this.accionServer = accionServer;
    }

   /**
     * Obtiene el identificador único del alquiler.
     *
     * @return El identificador único del alquiler.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtiene el coste total del alquiler.
     *
     * @return El coste total del alquiler.
     */
    public float getTotalCoste() {
        return totalCoste;
    }

    /**
     * Establece el identificador único del alquiler.
     *
     * @param id El identificador único del alquiler.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el usuario que realiza el alquiler.
     *
     * @return El usuario que realiza el alquiler.
     */
    public Usuario getUsuarioAlquila() {
        return usuarioAlquila;
    }

    /**
     * Establece el usuario que realiza el alquiler.
     *
     * @param usuarioAlquila El usuario que realiza el alquiler.
     */
    public void setUsuarioAlquila(Usuario usuarioAlquila) {
        this.usuarioAlquila = usuarioAlquila;
    }

    /**
     * Obtiene la propiedad que se alquila.
     *
     * @return La propiedad que se alquila.
     */
    public Propiedad getPropiedadAlquilada() {
        return propiedadAlquilada;
    }

    /**
     * Establece la propiedad que se alquila.
     *
     * @param propiedadAlquilada La propiedad que se alquila.
     */
    public void setPropiedadAlquilada(Propiedad propiedadAlquilada) {
        this.propiedadAlquilada = propiedadAlquilada;
    }

    /**
     * Obtiene el número de personas para las que se realiza el alquiler.
     *
     * @return El número de personas para las que se realiza el alquiler.
     */
    public int getNumPersonas() {
        return numPersonas;
    }

    /**
     * Establece el número de personas para las que se realiza el alquiler.
     *
     * @param numPersonas El número de personas para las que se realiza el alquiler.
     */
    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    /**
     * Obtiene la fecha y hora de inicio del alquiler.
     *
     * @return La fecha y hora de inicio del alquiler.
     */
    public LocalDateTime getCheckin() {
        return checkin;
    }

    /**
     * Establece la fecha y hora de inicio del alquiler.
     *
     * @param checkin La fecha y hora de inicio del alquiler.
     */
    public void setCheckin(LocalDateTime checkin) {
        this.checkin = checkin;
    }

    /**
     * Obtiene la fecha y hora de finalización del alquiler.
     *
     * @return La fecha y hora de finalización del alquiler.
     */
    public LocalDateTime getCheckout() {
        return checkout;
    }

    /**
     * Establece la fecha y hora de finalización del alquiler.
     *
     * @param checkout La fecha y hora de finalización del alquiler.
     */
    public void setCheckout(LocalDateTime checkout) {
        this.checkout = checkout;
    }

    /**
     * Genera una representación en cadena de caracteres del alquiler.
     *
     * @return Una cadena de caracteres que representa el alquiler.
     */
    @Override
    public String toString() {
        return  "Alquiler de pagado por " + usuarioAlquila.getNombre() + " " + usuarioAlquila.getApellidos() +
                " para la propiedad " + propiedadAlquilada.getNombre() +
                " para " + numPersonas + " personas" +
                " desde " + checkin +
                " hasta " + checkout;
    }
}
