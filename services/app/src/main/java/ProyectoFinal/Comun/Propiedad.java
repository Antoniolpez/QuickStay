package ProyectoFinal.Comun;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Clase Propiedad que implementa las interfaces Serializable y Cloneable.
 * Esta clase representa una propiedad que puede ser alquilada.
 */
public class Propiedad implements Serializable, Cloneable {
    /**
     * Acción que se realizará en el servidor.
     */
    private String accionServer;
    /**
     * Identificador único de la propiedad.
     */
    private int id;
    /**
     * Nombre de la propiedad.
     */
    private String nombre;
    /**
     * Tipo de la propiedad.
     */
    private String tipo;
    /**
     * Comunidad donde se encuentra la propiedad.
     */
    private String  comunidad;
    /**
     * Provincia donde se encuentra la propiedad.
     */
    private String provincia;
    /**
     * Localidad donde se encuentra la propiedad.
     */
    private String localidad;
    /**
     * Dirección de la propiedad.
     */
    private String direccion;
    /**
     * Latitud de la ubicación de la propiedad.
     */
    private float latitud;
    /**
     * Longitud de la ubicación de la propiedad.
     */
    private float longitud;
    /**
     * Altitud de la ubicación de la propiedad.
     */
    private float altitud;
    /**
     * Código postal de la ubicación de la propiedad.
     */
    private  int codigoPostal;
    /**
     * Precio por hora de alquiler de la propiedad.
     */
    private float precioHora;
    /**
     * Usuario propietario de la propiedad.
     */
    private Usuario propietario;
    /**
     * Lista de fotos de la propiedad.
     */
    private ArrayList<FotosPropiedad> fotos = new ArrayList<>();
    /**
     * Descripción de la propiedad.
     */
    private String descripcion;
    /**
     * Pedanía donde se encuentra la propiedad.
     */
    private String pedania;
    /**
     * Lista de reseñas de la propiedad.
     */
    private ArrayList<ResenaPropiedad> resenas = new ArrayList<>();
    /**
     * Soporte para cambios de propiedad.
     */
    private PropertyChangeSupport support;
    /**
     * Lista de alquileres de la propiedad.
     */
    private ArrayList<Alquiler> alquileres = new ArrayList<>();

    /**
     * Constructor de la clase Propiedad.
     *
     * @param id Identificador único de la propiedad.
     * @param nombre Nombre de la propiedad.
     * @param tipo Tipo de la propiedad.
     * @param comunidad Comunidad donde se encuentra la propiedad.
     * @param provincia Provincia donde se encuentra la propiedad.
     * @param localidad Localidad donde se encuentra la propiedad.
     * @param pedania Pedanía donde se encuentra la propiedad.
     * @param direccion Dirección de la propiedad.
     * @param latitud Latitud de la ubicación de la propiedad.
     * @param longitud Longitud de la ubicación de la propiedad.
     * @param altitud Altitud de la ubicación de la propiedad.
     * @param codigoPostal Código postal de la ubicación de la propiedad.
     * @param precioHora Precio por hora de alquiler de la propiedad.
     * @param propietario Usuario propietario de la propiedad.
     * @param fotos Lista de fotos de la propiedad.
     * @param descripcion Descripción de la propiedad.
     */
    public Propiedad(int id, String nombre, String tipo, String comunidad, String provincia, String localidad, String pedania, String direccion, float latitud, float longitud, float altitud, int codigoPostal,float precioHora, Usuario propietario, ArrayList<FotosPropiedad> fotos, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.comunidad = comunidad;
        this.provincia = provincia;
        this.localidad = localidad;
        this.pedania = pedania;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.codigoPostal = codigoPostal;
        this.propietario = propietario;
        if (fotos != null){
            this.fotos.addAll(fotos);
        }
        this.descripcion = descripcion;
        this.support = new PropertyChangeSupport(this);
        this.precioHora = precioHora;
    }

    /**
     * Constructor vacío de la clase Propiedad.
     */
    public Propiedad(){

    }

    /**
     * Constructor de la clase Propiedad sin el identificador único.
     *
     * @param nombre Nombre de la propiedad.
     * @param tipo Tipo de la propiedad.
     * @param comunidad Comunidad donde se encuentra la propiedad.
     * @param provincia Provincia donde se encuentra la propiedad.
     * @param localidad Localidad donde se encuentra la propiedad.
     * @param pedania Pedanía donde se encuentra la propiedad.
     * @param direccion Dirección de la propiedad.
     * @param latitud Latitud de la ubicación de la propiedad.
     * @param longitud Longitud de la ubicación de la propiedad.
     * @param altitud Altitud de la ubicación de la propiedad.
     * @param codigoPostal Código postal de la ubicación de la propiedad.
     * @param precioHora Precio por hora de alquiler de la propiedad.
     * @param propietario Usuario propietario de la propiedad.
     * @param fotos Lista de fotos de la propiedad.
     * @param descripcion Descripción de la propiedad.
     */
    public Propiedad(String nombre, String tipo, String comunidad, String provincia, String localidad, String pedania, String direccion, float latitud, float longitud, float altitud, int codigoPostal,float precioHora ,Usuario propietario, ArrayList<FotosPropiedad> fotos, String descripcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.comunidad = comunidad;
        this.provincia = provincia;
        this.localidad = localidad;
        this.pedania = pedania;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.codigoPostal = codigoPostal;
        this.propietario = propietario;
        this.fotos = fotos;
        this.descripcion = descripcion;
        this.support = new PropertyChangeSupport(this);
        this.precioHora = precioHora;
    }

    /**
     * Obtiene el identificador único de la propiedad.
     *
     * @return El identificador único de la propiedad.
     */
    public int getId() { return id; }

    /**
     * Obtiene el nombre de la propiedad.
     *
     * @return El nombre de la propiedad.
     */
    public String getNombre() { return nombre; }

    /**
     * Obtiene el tipo de la propiedad.
     *
     * @return El tipo de la propiedad.
     */
    public String getTipo() { return tipo; }

    /**
     * Obtiene el propietario de la propiedad.
     *
     * @return El propietario de la propiedad.
     */
    public Usuario getPropietario() { return propietario; }

    /**
     * Obtiene la lista de fotos de la propiedad.
     *
     * @return La lista de fotos de la propiedad.
     */
    public ArrayList<FotosPropiedad> getFotos() {
        return fotos;
    }

    /**
     * Obtiene el precio por hora de alquiler de la propiedad.
     *
     * @return El precio por hora de alquiler de la propiedad.
     */
    public ArrayList<Alquiler> getAlquileres() {
        return alquileres;
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
     * Obtiene la descripción de la propiedad.
     *
     * @return La descripción de la propiedad.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene la comunidad donde se encuentra la propiedad.
     *
     * @return La comunidad donde se encuentra la propiedad.
     */
    public String getComunidad() {
        return comunidad;
    }

    /**
     * Obtiene la provincia donde se encuentra la propiedad.
     *
     * @return La provincia donde se encuentra la propiedad.
     */
    public String getProvincia() {
        return provincia;
    }

    /**
     * Obtiene la localidad donde se encuentra la propiedad.
     *
     * @return La localidad donde se encuentra la propiedad.
     */
    public String getLocalidad() {
        return localidad;
    }

    /**
     * Obtiene la dirección de la propiedad.
     *
     * @return La dirección de la propiedad.
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * Obtiene la latitud de la ubicación de la propiedad.
     *
     * @return La latitud de la ubicación de la propiedad.
     */
    public float getLatitud() {
        return latitud;
    }

    /**
     * Obtiene la longitud de la ubicación de la propiedad.
     *
     * @return La longitud de la ubicación de la propiedad.
     */
    public float getLongitud() {
        return longitud;
    }

    /**
     * Obtiene la altitud de la ubicación de la propiedad.
     *
     * @return La altitud de la ubicación de la propiedad.
     */
    public float getAltitud() {
        return altitud;
    }

    /**
     * Obtiene el código postal de la ubicación de la propiedad.
     *
     * @return El código postal de la ubicación de la propiedad.
     */
    public int getCodigoPostal() {
        return codigoPostal;
    }

    /**
     * Obtiene el precio por hora de alquiler de la propiedad.
     *
     * @return El precio por hora de alquiler de la propiedad.
     */
    public float getPrecioHora() {
        return precioHora;
    }

    /**
     * Establece el precio por hora de alquiler de la propiedad.
     *
     * @param precioHora El precio por hora de alquiler de la propiedad.
     */
    public void setPrecioHora(float precioHora) {
        this.precioHora = precioHora;
    }

    /**
     * Establece la altitud de la ubicación de la propiedad.
     *
     * @param altitud La altitud de la ubicación de la propiedad.
     */
    public void setAltitud(float altitud) {
        this.altitud = altitud;
    }

    /**
     * Establece la latitud de la ubicación de la propiedad.
     *
     * @param latitud La latitud de la ubicación de la propiedad.
     */
    public void setLatitud(float latitud) {
        this.latitud = latitud;
    }

    /**
     * Establece la longitud de la ubicación de la propiedad.
     *
     * @param longitud La longitud de la ubicación de la propiedad.
     */
    public void setLongitud(float longitud) {
        this.longitud = longitud;
    }

    /**
     * Establece la lista de alquileres de la propiedad.
     *
     * @param alquileres La lista de alquileres de la propiedad.
     */
    public void setAlquileres(ArrayList<Alquiler> alquileres) {
        this.alquileres = alquileres;
    }

    /**
     * Obtiene la pedanía donde se encuentra la propiedad.
     *
     * @return La pedanía donde se encuentra la propiedad.
     */
    public String getPedania() {
        if (pedania == null){
            return "";
        }
        return pedania;
    }

    /**
     * Añade un alquiler a la lista de alquileres de la propiedad.
     *
     * @param alquiler El alquiler a añadir.
     */
    public void addAlquiler(Alquiler alquiler) {
        alquileres.add(alquiler);
    }

    /**
     * Establece la lista de reseñas de la propiedad.
     *
     * @param resenas La lista de reseñas de la propiedad.
     */
    public void setResenas(ArrayList<ResenaPropiedad> resenas) {
        this.resenas = resenas;
    }

    /**
     * Establece la descripción de la propiedad.
     *
     * @param descripcion La descripción de la propiedad.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
     * Establece el identificador único de la propiedad.
     *
     * @param id El identificador único de la propiedad.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Establece el nombre de la propiedad.
     *
     * @param nombre El nombre de la propiedad.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece el tipo de la propiedad.
     *
     * @param tipo El tipo de la propiedad.
     */
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    /**
     * Establece el propietario de la propiedad.
     *
     * @param propietario El propietario de la propiedad.
     */
    public void setPropietario(Usuario propietario) {
        this.propietario = propietario;
    }

    /**
     * Establece la lista de fotos de la propiedad.
     *
     * @param fotos La lista de fotos de la propiedad.
     */
    public void setFotos(ArrayList<FotosPropiedad> fotos) {
        this.fotos = fotos;
    }

    /**
     * Establece la dirección de la propiedad.
     *
     * @param text La dirección de la propiedad.
     */
    public void setDireccion(String text) {
        this.direccion = text;
    }

    /**
     * Establece la pedanía donde se encuentra la propiedad.
     *
     * @param text La pedanía donde se encuentra la propiedad.
     */
    public void setPedania(String text) {
        this.pedania = text;
    }

    /**
     * Establece la localidad donde se encuentra la propiedad.
     *
     * @param text La localidad donde se encuentra la propiedad.
     */
    public void setLocalidad(String text) {
        this.localidad = text;
    }

    /**
     * Establece la provincia donde se encuentra la propiedad.
     *
     * @param text La provincia donde se encuentra la propiedad.
     */
    public void setProvincia(String text) {
        this.provincia = text;
    }

    /**
     * Establece la comunidad donde se encuentra la propiedad.
     *
     * @param text La comunidad donde se encuentra la propiedad.
     */
    public void setComunidad(String text) {
        this.comunidad = text;
    }

    /**
     * Establece el código postal de la ubicación de la propiedad.
     *
     * @param codigoPostal El código postal de la ubicación de la propiedad.
     */
    public void setCodigoPostal(int codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    /**
     * Obtiene la ubicación de la propiedad.
     *
     * @return La ubicación de la propiedad.
     */
    public String getUbicacion(){
        return direccion + ", " + codigoPostal + ", " + localidad + ", " + provincia + ", " + comunidad;
    }

    /**
     * Obtiene la lista de reseñas de la propiedad.
     *
     * @return La lista de reseñas de la propiedad.
     */
    public ArrayList<ResenaPropiedad> getResenas() {
        return resenas;
    }

    /**
     * Añade una reseña a la lista de reseñas de la propiedad.
     *
     * @param resena La reseña a añadir.
     */
    public void addResena(ResenaPropiedad resena) {
        ArrayList<ResenaPropiedad> oldResenas = new ArrayList<>(this.resenas);
        this.resenas.add(resena);
        support.firePropertyChange("resenas", oldResenas, this.resenas);
    }

    /**
     * Añade un PropertyChangeListener al soporte.
     *
     * @param pcl El PropertyChangeListener a añadir.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Elimina un PropertyChangeListener del soporte.
     *
     * @param pcl El PropertyChangeListener a eliminar.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Crea y devuelve una copia de esta propiedad.
     *
     * @return Una copia de esta propiedad.
     */
    @Override
    public Propiedad clone() {
        try {
            return (Propiedad) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // This should never happen
        }
    }

    /**
     * Genera una representación en cadena de caracteres de la propiedad.
     *
     * @return Una cadena de caracteres que representa la propiedad.
     */
    @Override
    public String toString() {
        return "Propiedad{" +
                "accionServer='" + accionServer + '\'' +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", comunidad='" + comunidad + '\'' +
                ", provincia='" + provincia + '\'' +
                ", localidad='" + localidad + '\'' +
                ", direccion='" + direccion + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", altitud=" + altitud +
                ", codigoPostal=" + codigoPostal +
                ", propietario=" + propietario +
                ", descripcion='" + descripcion + '\'' +
                ", pedania='" + pedania + '\'' +
                ", resenas=" + resenas +
                '}';
    }


}
