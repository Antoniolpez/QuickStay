package ProyectoFinal.Servidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Clase Servidor.
 * Esta clase se encarga de gestionar las conexiones de los clientes al servidor.
 */
public class Servidor {
    private static final int PORT = 1234;
    private static final ArrayList<HiloServidor> HILOS_SERVIDOR = new ArrayList<>();

    /**
     * Método principal del servidor.
     * Este método se encarga de iniciar el servidor y de aceptar las conexiones de los clientes.
     * @param args Los argumentos de la línea de comandos.
     */
    public static void main(String[] args){
        Consola consola = new Consola();
        consola.start();
        try{
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Servidor escuchando en el puerto " + PORT);
            boolean conectar = MySQL.conectar();
            while(conectar){
                System.out.println("Esperando conexión");
                Socket socketUsuario = server.accept();
                String nombreUsuario = socketUsuario.getInetAddress().getHostName();
                System.out.println("Cliente conectado desde la dirección " + socketUsuario.getInetAddress().getHostAddress() + " con nombre " + socketUsuario.getInetAddress().getHostName());

                System.out.println("Cliente conectado desde la dirección " + socketUsuario.getInetAddress().getHostAddress());
                BufferesServidor cliente = new BufferesServidor(nombreUsuario, socketUsuario);

                // EL HILO NO ENTRA AL ARRAYLIST (NO ES PÚBLICO) HASTA QUE NO SE INICIE SESIÓN
                new HiloServidor(socketUsuario, cliente);
            }
        }catch (Exception e){
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    /**
     * Método para obtener la lista de hilos del servidor.
     * @return La lista de hilos del servidor.
     */
    public static ArrayList<HiloServidor> getHilosServidor() {
        return HILOS_SERVIDOR;
    }
}