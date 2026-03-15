package ProyectoFinal.Cliente.Chat;

import ProyectoFinal.Comun.Mensaje;

import java.util.Comparator;

/**
 * La clase Comparador implementa la interfaz Comparator para comparar objetos de la clase Mensaje.
 * Se utiliza para ordenar los mensajes según su fecha de llegada.
 */
public class Comparador implements Comparator<Mensaje> {

    /**
     * Este método compara dos mensajes según su fecha de llegada.
     * @param mensaje1 El primer mensaje a comparar.
     * @param mensaje2 El segundo mensaje a comparar.
     * @return Un número negativo si la fecha de llegada del primer mensaje es anterior a la del segundo,
     * cero si las fechas de llegada son iguales, o un número positivo si la fecha de llegada del primer mensaje es posterior a la del segundo.
     */
    @Override
    public int compare(Mensaje mensaje1, Mensaje mensaje2) {
        return mensaje1.getFechaLlegada().compareTo(mensaje2.getFechaLlegada());
    }
}