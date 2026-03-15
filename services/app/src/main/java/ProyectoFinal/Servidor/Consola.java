package ProyectoFinal.Servidor;

import ProyectoFinal.Comun.Usuario;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Scanner;

/**
 * Clase Consola.
 * Esta clase se encarga de gestionar la consola del servidor.
 * La consola permite al administrador del servidor ejecutar comandos para obtener información sobre el servidor.
 * La consola se ejecuta en un hilo independiente del servidor.
 * @extends Thread
 */
public class Consola extends Thread {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Método para iniciar la consola.
     * Este método se ejecuta cuando se inicia el hilo.
     */
    @Override
    public void run() {
        iniciarConsola();
    }

    /**
     * Método para iniciar la consola.
     * Este método se encarga de leer los comandos introducidos por el usuario y ejecutar la acción correspondiente.
     */
    private void iniciarConsola() {
        while (true) {
            String comando = scanner.nextLine();
            switch (comando) {
                case "/help":
                    System.out.println("Comandos disponibles:");
                    System.out.println("/help - Muestra la lista de comandos disponibles");
                    System.out.println("/list - Muestra la lista de usuarios conectados");
                    System.out.println("/ip - Muestra la IP pública del servidor");
                    System.out.println("/online - Muestra la lista de usuarios conectados");
                    break;
                case "/list":
                    listarUsuariosConectados();
                    break;
                case "/ip":
                    System.out.println("IP pública: " + getIpPublica());
                    break;
                case "/online":
                    System.out.println("Usuarios conectados:");
                    for (Usuario usuario : HiloServidor.getUsuariosOnline()) {
                        System.out.println(usuario.getNombre());
                    }
                    break;
                default:
                    System.out.println("Comando desconocido. Escribe /help para ver la lista de comandos disponibles.");
                    break;
            }
        }
    }

    /**
     * Método para listar los usuarios conectados.
     * Este método se encarga de imprimir en la consola la lista de usuarios conectados.
     */
    private void listarUsuariosConectados() {
        System.out.println("Usuarios conectados:");
        boolean hayUsuarios = false;
        for (HiloServidor hilo : Servidor.getHilosServidor()) {
            System.out.println("El hilo " + hilo.getName() + " está conectado sirviendo a " + hilo.getUsuario().getNombre());
            hayUsuarios = true;
        }
        if (!hayUsuarios) {
            System.out.println("No hay usuarios conectados");
        }
    }

    /**
     * Método para obtener la IP pública del servidor.
     * Este método se encarga de obtener la IP pública del servidor a través de un servicio web.
     * @return La IP pública del servidor.
     */
    public String getIpPublica() {
        String ip = null;
        try {
            URL url = new URL("http://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            ip = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }
}