package ProyectoFinal.Cliente.Chat;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Cliente.Controladores.Principal;
import ProyectoFinal.Comun.Mensaje;
import ProyectoFinal.Comun.Propiedad;
import ProyectoFinal.Comun.ResenaPropiedad;
import ProyectoFinal.Comun.Usuario;

import java.io.IOException;
import java.util.ArrayList;

/**
 * La clase HiloReceptorDatos extiende la clase Thread y se encarga de recibir los datos del servidor.
 * Esta clase se ejecuta en un hilo separado para no bloquear la interfaz de usuario mientras se reciben los datos.
 */
public class HiloReceptorDatos extends Thread {
    /** La instancia de la clase Principal */
    private static Principal principal;
    /** Un booleano que indica si el hilo está operativo */
    private static boolean OPERATIVO = true;

    /**
     * Constructor de la clase HiloReceptorDatos
     * Este constructor inicia el hilo.
     */
    public HiloReceptorDatos() {
        this.start();
    }

    /**
     * Este método se ejecuta cuando se inicia el hilo.
     * Se encarga de recibir los datos del servidor y procesarlos.
     */
    @Override
    public void run() {
        while (!BufferesUser.getSocket().isClosed()) {
            System.out.println("Esperando mensaje");
            try {
                Object update = BufferesUser.getObjectInputStream().readObject();
                System.out.println("El usuario " + Principal.getUsuario().getNombre() + " ha recibido " + update.toString());
                if (update instanceof Mensaje mensajeRecibido) {
                    OPERATIVO = false;
                    principal.mostrarMensaje(mensajeRecibido.getMensaje(), mensajeRecibido.getUsuarioEmisor());
                    System.out.println(mensajeRecibido.getMensaje());
                } else if (update instanceof ArrayList<?> lista) {
                    OPERATIVO = false;
                    if (lista.stream().allMatch(o -> o instanceof Usuario)) {
                        System.out.println("Actualizando lista de usuarios online");
                        principal.actualizarListaUsuariosOnline((ArrayList<Usuario>) lista);
                    } else if (lista.stream().allMatch(o -> o instanceof Propiedad)){
                        System.out.println("Actualizando propiedades");
                        principal.actualizarPropiedades((ArrayList<Propiedad>) lista);
                    }else if (lista.stream().allMatch(o -> o instanceof ResenaPropiedad)){
                        System.out.println("Actualizando resenas");
                        principal.actualizarResenas((ArrayList<ResenaPropiedad>) lista);
                    }
                }else if (update instanceof Usuario usuario){
                    switch (usuario.getAccionServer()) {
                        case "contacto insertado" -> {
                            Principal.getUsuario().getContactos().add(usuario);
                            Principal.actualizacionContactos();
                        }
                        case "usuario no existe" -> Principal.actualizacionContactos();
                        case "actualizarContacto" -> {
                            Principal.getUsuario().getContactos().remove(usuario);
                            Principal.getUsuario().getContactos().add(usuario);
                        }
                    }
                }else{
                    System.err.println("Mensaje no reconocido");
                }
                OPERATIVO = true;

            } catch (IOException | ClassNotFoundException e) {
                break;
            }
        }
        System.out.println("Se ha cerrado la conexión");
        // Poner ventana emergente
    }


    /**
     * Este método se encarga de devolver el valor de la variable OPERATIVO.
     * @return El valor de la variable OPERATIVO.
     */
    public static boolean getOperativo(){
        System.out.println("Operativo: " + OPERATIVO);
        return OPERATIVO;
    }

    /**
     * Este método establece el controlador principal.
     * @param principal El controlador principal.
     */
    public static void setPrincipal(Principal principal) {
        HiloReceptorDatos.principal = principal;
    }

}
