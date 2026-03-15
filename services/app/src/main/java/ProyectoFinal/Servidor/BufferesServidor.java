package ProyectoFinal.Servidor;

import java.io.*;
import java.net.Socket;

/**
 * Clase BufferesServidor.
 * Esta clase se encarga de gestionar los flujos de entrada y salida de objetos para un socket específico (que va vinculado a un inicio de sesión específico).
 */
public class BufferesServidor {

    /** Nombre del usuario  */
    private String nombreUsuario;
    /** Socket asociado al usuario */
    private final Socket socket;
    /** Flujo de entrada de objetos */
    private ObjectInputStream objectInputStream;
    /** Flujo de salida de objetos */
    private ObjectOutputStream objectOutputStream;

    /**
     * Constructor de la clase BufferesServidor.
     * @param nombreUsuario El nombre del usuario.
     * @param socket El socket asociado al usuario.
     */
    public BufferesServidor(String nombreUsuario, Socket socket) {
        this.nombreUsuario = nombreUsuario;
        this.socket = socket;
    }

    /**
     * Método para obtener el nombre del usuario.
     * @return El nombre del usuario.
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * Método para establecer el nombre del usuario.
     * @param nombreUsuario El nuevo nombre del usuario.
     */
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    /**
     * Método para obtener el flujo de entrada de objetos.
     * @return El flujo de entrada de objetos.
     */
    public ObjectInputStream getObjectInputStream(){
        return objectInputStream;
    }

    /**
     * Método para obtener el flujo de salida de objetos.
     * @return El flujo de salida de objetos.
     */
    public ObjectOutputStream getObjectOutputStream(){return objectOutputStream;}

    /**
     * Método para abrir el flujo de entrada de objetos.
     * Si el socket es nulo o está cerrado, se muestra un error.
     * Si se produce una excepción de E/S, se muestra un error y se establece el flujo de entrada de objetos a nulo.
     */
    public void openObjectInputStream() {
        try {
            if (socket != null && !socket.isClosed()) {
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } else {
                System.err.println("ERROR::El socket es null o ha sido cerrado. No se puede abrir el ObjectInputStream.");
            }
            System.out.println("Abriendo ObjectInputStream");
        } catch (IOException e) {
            System.err.println("ERROR::No se ha podido iniciar el ObjectInputStream " + e.getMessage());
            objectInputStream = null;
        }
    }

    /**
     * Método para abrir el flujo de salida de objetos.
     * Si se produce una excepción de E/S, se muestra un error.
     */
    public void openObjectOutputStream() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Abriendo ObjectOutputStream");
        } catch (IOException e) {
            System.err.println("ERROR::No se ha podido iniciar el ObjectOutputStream " + e.getMessage());
        }
    }
}