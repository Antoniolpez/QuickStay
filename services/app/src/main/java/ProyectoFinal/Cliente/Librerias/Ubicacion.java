package ProyectoFinal.Cliente.Librerias;

public class Ubicacion {
    private String comunidad;
    private String provincia;
    private String localidad;
    private String codigoPostal;
    private double latitud;
    private double longitud;
    private double altitud;
    private int poblacionTotal;
    private int poblacion1;
    private int poblacion2;

    public Ubicacion(String provincia,String localidad, String  codigoPostal) {
        this.provincia = provincia;
        this.localidad = localidad;
        this.codigoPostal = codigoPostal;
    }

    public Ubicacion(String comunidad, String provincia, String localidad, double latitud, double longitud, double altitud, int poblacionTotal, int poblacion1, int poblacion2, String codigoPostal) {
        this.comunidad = comunidad;
        this.provincia = provincia;
        this.localidad = localidad;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.poblacionTotal = poblacionTotal;
        this.poblacion1 = poblacion1;
        this.poblacion2 = poblacion2;
        this.codigoPostal = codigoPostal;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getComunidad() {
        return comunidad;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public double getAltitud() {
        return altitud;
    }

    @Override
    public String toString() {
        return "Ubicacion{" +
                "comunidad='" + comunidad + '\'' +
                ", provincia='" + provincia + '\'' +
                ", localidad='" + localidad + '\'' +
                ", codigoPostal='" + codigoPostal + '\'' +
                ", latitud=" + latitud +
                ", longitud=" + longitud +
                ", altitud=" + altitud +
                ", poblacionTotal=" + poblacionTotal +
                ", poblacion1=" + poblacion1 +
                ", poblacion2=" + poblacion2 +
                '}';
    }
}