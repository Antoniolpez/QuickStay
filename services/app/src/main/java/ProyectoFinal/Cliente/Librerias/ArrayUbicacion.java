package ProyectoFinal.Cliente.Librerias;

import java.util.ArrayList;

public class ArrayUbicacion<U> extends ArrayList<Ubicacion> {
    private final ArrayList<Ubicacion> ubicaciones = new ArrayList<>();


    public ArrayUbicacion() {
    }

    public void addUbicacion(Ubicacion ubicacion) {
        ubicaciones.add(ubicacion);
    }

    public String getLocalidad(String provincia) {
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getProvincia().equals(provincia)) {
                return ubicacion.getLocalidad();
            }
        }
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getLocalidad().equals(provincia)) {
                return ubicacion.getLocalidad();
            }
        }


        return null;
    }
    public String getProvincia(String localidad) {
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getLocalidad().equals(localidad)) {
                return ubicacion.getProvincia();
            }
        }
        return null;
    }
    public ArrayList<String> getProvincias(String comunidad) {
        ArrayList<String> provincias = new ArrayList<>();
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getComunidad().equals(comunidad)) {
                provincias.add(ubicacion.getProvincia());
            }
        }
        return provincias;
    }
    public ArrayList<String> getLocalidades(String provincia) {
        ArrayList<String> localidades = new ArrayList<>();
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getProvincia().equals(provincia)) {
                localidades.add(ubicacion.getLocalidad());
            }
        }
        return localidades;
    }
    public String getComunidad(String provincia) {
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getProvincia().equals(provincia)) {
               return ubicacion.getComunidad();
            }
        }
        return null;
    }
    public double getLatitud(String localidad) {
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getLocalidad().equals(localidad)) {
                return ubicacion.getLatitud();
            }
        }
        return 0;
    }
    public double getLongitud(String localidad) {
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getLocalidad().equals(localidad)) {
                return ubicacion.getLongitud();
            }
        }
        return 0;
    }
    public double getAltitud(String localidad) {
        for (Ubicacion ubicacion : ubicaciones) {
            if (ubicacion.getLocalidad().equals(localidad)) {
                return ubicacion.getAltitud();
            }
        }
        return 0;
    }


    public ArrayList<Ubicacion> getUbicaciones() {
        return ubicaciones;
    }
    public int getALL (){
        return ubicaciones.size();
    }
}
