package ProyectoFinal.Comun;

import java.io.Serializable;

/**
 * Clase Tarjeta que implementa la interfaz Serializable.
 * Esta clase representa una tarjeta de crédito/débito asociada a un usuario.
 */
public class Tarjeta implements Serializable {
    /**
     * Acción que se realizará en el servidor.
     */
    private String accionServer;
    /**
     * Número de la tarjeta.
     */
    private String numero;
    /**
     * Usuario titular de la tarjeta.
     */
    private Usuario titular;
    /**
     * Fecha de caducidad de la tarjeta.
     */
    private String fechaCaducidad;
    /**
     * Código de verificación de la tarjeta.
     */
    private int cvv;
    /**
     * Dirección de facturación asociada a la tarjeta.
     */
    private String direccionFacturacion;

    /**
     * Constructor de la clase Tarjeta.
     *
     * @param numero Número de la tarjeta.
     * @param titular Usuario titular de la tarjeta.
     * @param fechaCaducidad Fecha de caducidad de la tarjeta.
     * @param cvv Código de verificación de la tarjeta.
     * @param direccionFacturacion Dirección de facturación asociada a la tarjeta.
     * @param accionServer Acción que se realizará en el servidor.
     */
    public Tarjeta(String numero, Usuario titular, String fechaCaducidad, int cvv, String direccionFacturacion, String accionServer) {
        this.numero = numero;
        this.titular = titular;
        this.fechaCaducidad = fechaCaducidad;
        this.cvv = cvv;
        this.direccionFacturacion = direccionFacturacion;
        this.accionServer = accionServer;
    }

    /**
     * Constructor vacío de la clase Tarjeta.
     */
    public Tarjeta() {

    }

    /**
     * Establece la acción que se realizará en el servidor.
     *
     * @param accion La acción que se realizará en el servidor.
     */
    public void setAccionServer(String accion){
        this.accionServer = accion;
    }

    /**
     * Obtiene el número de la tarjeta.
     *
     * @return El número de la tarjeta.
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Establece el número de la tarjeta.
     *
     * @param numero El número de la tarjeta.
     */
    public void setNumero(String numero) {
        this.numero = numero;
    }

    /**
     * Obtiene el usuario titular de la tarjeta.
     *
     * @return El usuario titular de la tarjeta.
     */
    public Usuario getTitular() {
        return titular;
    }

    /**
     * Establece el usuario titular de la tarjeta.
     *
     * @param titular El usuario titular de la tarjeta.
     */
    public void setTitular(Usuario titular) {
        this.titular = titular;
    }

    /**
     * Obtiene la fecha de caducidad de la tarjeta.
     *
     * @return La fecha de caducidad de la tarjeta.
     */
    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    /**
     * Establece la fecha de caducidad de la tarjeta.
     *
     * @param fechaCaducidad La fecha de caducidad de la tarjeta.
     */
    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    /**
     * Obtiene el código de verificación de la tarjeta.
     *
     * @return El código de verificación de la tarjeta.
     */
    public int getCvv() {
        return cvv;
    }

    /**
     * Establece el código de verificación de la tarjeta.
     *
     * @param cvv El código de verificación de la tarjeta.
     */
    public void setCvv(int cvv) {
        this.cvv = cvv;
    }

    /**
     * Obtiene la dirección de facturación asociada a la tarjeta.
     *
     * @return La dirección de facturación asociada a la tarjeta.
     */
    public String getDireccionFacturacion() {
        return direccionFacturacion;
    }

    /**
     * Establece la dirección de facturación asociada a la tarjeta.
     *
     * @param direccionFacturacion La dirección de facturación asociada a la tarjeta.
     */
    public void setDireccionFacturacion(String direccionFacturacion) {
        this.direccionFacturacion = direccionFacturacion;
    }

    /**
     * Obtiene la acción que se realizará en el servidor.
     *
     * @return La acción que se realizará en el servidor.
     */
    public String getAccionServer() {
        return accionServer;
    }
}