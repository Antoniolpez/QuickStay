package ProyectoFinal.Cliente.Chat;

import ProyectoFinal.Cliente.Local;
import ProyectoFinal.Comun.Mensaje;
import ProyectoFinal.Comun.Usuario;

import java.util.TreeSet;

/**
 * La clase TreeSetMsg extiende la clase TreeSet y se utiliza para almacenar y gestionar los mensajes.
 * Esta clase implementa el patrón Singleton, lo que significa que solo puede haber una instancia de esta clase en el programa.
 */
public class TreeSetMsg extends TreeSet<Mensaje> {

    /** La única instancia de esta clase */
    private static final TreeSetMsg INSTANCE = new TreeSetMsg();

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Se utiliza un Comparador para ordenar los mensajes en el TreeSet.
     */
    private TreeSetMsg() {
        super(new Comparador());
    }

    /**
     * Este método devuelve la única instancia de esta clase.
     * @return La única instancia de TreeSetMsg.
     */
    public static TreeSetMsg getInstance() {
        return INSTANCE;
    }

    /**
     * Este método devuelve un TreeSetMsg que contiene todos los mensajes enviados o recibidos por un usuario específico.
     * @param usuario El usuario cuyos mensajes se quieren obtener.
     * @return Un TreeSetMsg que contiene todos los mensajes enviados o recibidos por el usuario.
     */
    public TreeSetMsg getChatUser(Usuario usuario) {
        System.out.println("Tamaño de: " + INSTANCE.size());
        TreeSetMsg chat = new TreeSetMsg();
        for (Mensaje mensaje : INSTANCE) {
            if (mensaje.getUsuarioEmisor().getUsuario().equals(usuario.getUsuario()) || mensaje.getUsuarioReceptor().getUsuario().equals(usuario.getUsuario())) {
                chat.add(mensaje);
            }
        }
        return chat;
    }

    /**
     * Este método añade un mensaje al TreeSetMsg y lo muestra en el chat.
     * @param mensaje El mensaje a añadir.
     */
    public void addMensaje(Mensaje mensaje) {
        INSTANCE.add(mensaje);
        Local.escribirEnChat(mensaje);
    }

    /**
     * Este método añade un mensaje al TreeSetMsg sin mostrarlo en el chat.
     * @param mensaje El mensaje a añadir.
     */
    public void addMensajeLocal(Mensaje mensaje) {
        INSTANCE.add(mensaje);
        System.out.println("Mensaje añadido localmente " + INSTANCE.size());
    }
}