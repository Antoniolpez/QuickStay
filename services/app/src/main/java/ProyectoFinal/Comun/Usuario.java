package ProyectoFinal.Comun;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Clase Usuario que implementa la interfaz Serializable.
 * Esta clase representa un usuario en el sistema.
 */
public class Usuario implements Serializable {

    /**
     * Nombre de usuario.
     */
    private String usuario ;
    /**
     * Nombre del usuario.
     */
    private String nombre;
    /**
     * Apellidos del usuario.
     */
    private String apellidos;
    /**
     * Fecha de nacimiento del usuario.
     */
    private String fechaNacimiento;
    /**
     * Contraseña del usuario.
     */
    private String password;
    /**
     * Correo electrónico del usuario.
     */
    private String email;
    /**
     * Acción que se realizará en el servidor.
     */
    private String accionServer;
    /**
     * Imagen del usuario.
     */
    private byte[] imagenUsuario;
    /**
     * Número de teléfono del usuario.
     */
    private String numTelefono;
    /**
     * Lista de contactos del usuario.
     */
    private ArrayList<Usuario> contactos;
    /**
     * Lista de tarjetas del usuario.
     */
    private ArrayList<Tarjeta> tarjetas;
    /**
     * Lista de facturas del usuario.
     */
    private ArrayList<Facturacion> facturas;
    /**
     * Lista de propiedades del usuario.
     */
    private ArrayList<Propiedad> propiedades;

    /**
     * Constructor de la clase Usuario.
     *
     * @param usuario Nombre de usuario.
     * @param nombre Nombre del usuario.
     * @param apellidos Apellidos del usuario.
     * @param fechaNacimiento Fecha de nacimiento del usuario.
     * @param password Contraseña del usuario.
     * @param email Correo electrónico del usuario.
     * @param numTelefono Número de teléfono del usuario.
     * @param contactos Lista de contactos del usuario.
     * @param imagenUsuario Imagen del usuario.
     * @param tarjeta Lista de tarjetas del usuario.
     * @param facturas Lista de facturas del usuario.
     * @param propiedades Lista de propiedades del usuario.
     * @param accionServer Acción que se realizará en el servidor.
     */
    public Usuario(String usuario, String nombre, String apellidos, String fechaNacimiento, String password, String email,String numTelefono, ArrayList<Usuario> contactos, byte[] imagenUsuario, ArrayList<Tarjeta> tarjeta, ArrayList<Facturacion> facturas,ArrayList<Propiedad> propiedades, String accionServer) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.password = password;
        this.email = email;
        this.numTelefono = numTelefono;
        this.contactos = contactos;
        this.imagenUsuario = imagenUsuario;
        this.tarjetas = tarjeta;
        this.facturas = facturas;
        this.propiedades = propiedades;
        this.accionServer = accionServer;
    }

    /**
     * Constructor vacío de la clase Usuario.
     */
    public Usuario(){

    }

    /**
     * Obtiene el nombre de usuario.
     *
     * @return El nombre de usuario.
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Establece el nombre de usuario.
     *
     * @param usuario El nombre de usuario.
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return El nombre del usuario.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param nombre El nombre del usuario.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene los apellidos del usuario.
     *
     * @return Los apellidos del usuario.
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Establece los apellidos del usuario.
     *
     * @param apellidos Los apellidos del usuario.
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Obtiene la fecha de nacimiento del usuario.
     *
     * @return La fecha de nacimiento del usuario.
     */
    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento del usuario.
     *
     * @param fechaNacimiento La fecha de nacimiento del usuario.
     */
    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Obtiene la contraseña del usuario.
     *
     * @return La contraseña del usuario.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece la contraseña del usuario.
     *
     * @param password La contraseña del usuario.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el correo electrónico del usuario.
     *
     * @return El correo electrónico del usuario.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Obtiene la lista de tarjetas del usuario.
     *
     * @return La lista de tarjetas del usuario.
     */
    public ArrayList<Tarjeta> getTarjeta() {
        return tarjetas;
    }

    /**
     * Establece la lista de tarjetas del usuario.
     *
     * @param tarjeta La lista de tarjetas del usuario.
     */
    public void setTarjeta(ArrayList<Tarjeta> tarjeta) {
        this.tarjetas = tarjeta;
    }

    /**
     * Establece el correo electrónico del usuario.
     *
     * @param email El correo electrónico del usuario.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la lista de facturas del usuario.
     *
     * @return La lista de facturas del usuario.
     */
    public ArrayList<Facturacion> getFacturacion() {
        return facturas;
    }

    /**
     * Establece la lista de facturas del usuario.
     *
     * @param facturas La lista de facturas del usuario.
     */
    public void setFacturacion(ArrayList<Facturacion> facturas) {
        this.facturas = facturas;
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
     * Establece el número de teléfono del usuario.
     *
     * @param numTelefono El número de teléfono del usuario.
     */
    public void setNumTelefono(String numTelefono) {
        this.numTelefono = numTelefono;
    }

    /**
     * Obtiene la imagen del usuario.
     *
     * @return La imagen del usuario.
     */
    public byte[] getImagenUsuario() {
        return imagenUsuario;
    }

    /**
     * Establece la imagen del usuario.
     *
     * @param imagenUsuario La imagen del usuario.
     */
    public void setImagenUsuario(byte[] imagenUsuario) {
        this.imagenUsuario = imagenUsuario;
    }

    /**
     * Obtiene la lista de propiedades del usuario.
     *
     * @return La lista de propiedades del usuario.
     */
    public ArrayList<Propiedad> getPropiedades() {
        return propiedades;
    }

    /**
     * Establece la lista de propiedades del usuario.
     *
     * @param propiedades La lista de propiedades del usuario.
     */
    public void setPropiedades(ArrayList<Propiedad> propiedades) {
        this.propiedades = propiedades;
    }

    /**
     * Obtiene el número de teléfono del usuario.
     *
     * @return El número de teléfono del usuario.
     */
    public String getNumTelefono() {
        return numTelefono;
    }

    /**
     * Obtiene la lista de contactos del usuario.
     *
     * @return La lista de contactos del usuario.
     */
    public ArrayList<Usuario> getContactos() {
        if (contactos == null){
            contactos = new ArrayList<>();
        }
        return contactos;
    }

    /**
     * Establece la lista de contactos del usuario.
     *
     * @param contactos La lista de contactos del usuario.
     */
    public void setContactos(ArrayList<Usuario> contactos) {
        this.contactos = contactos;
    }

    /**
     * Genera una representación en cadena de caracteres del usuario.
     *
     * @return Una cadena de caracteres que representa al usuario.
     */
    @Override
    public String toString() {
        return "Usuario{" +
                "usuario='" + usuario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", fechaNacimiento='" + fechaNacimiento + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", accionServer='" + accionServer + '\'' +
                ", numTelefono='" + numTelefono + '\'' +
                ", contactos=" + contactos +
                '}';
    }
}