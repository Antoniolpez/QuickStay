package ProyectoFinal.Cliente;

import ProyectoFinal.Cliente.Chat.HiloReceptorDatos;
import ProyectoFinal.Cliente.Controladores.Principal;
import ProyectoFinal.Comun.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;


import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class BufferesUser {
    /**
     * Gestiona los clientes en línea
     * Abre y cierra buffers según se necesiten
     */
    private static Socket socket = null;
    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;
    private static String ip;
    private static int port;
    private static final Alert alert = new Alert(Alert.AlertType.ERROR);
    private static boolean conectado = false;
    public static void conexion(){
        Local.crearRutaFileSocketProperties();
        String[] socketProperties = Local.getSocketProperties();
        ip = socketProperties[0];
        port = Integer.parseInt(socketProperties[1]);
        BufferesUser.conectarServer();
    }

    public static void setConectado(boolean conectado) {
        BufferesUser.conectado = conectado;
    }

    public static void conectarServer() {
        try {
            if (!conectado) {
                socket = new Socket(ip, port);
                System.out.println("Conexión establecida con el servidor");
                conectado = true;
                openObjectInputStream();
                openObjectOutputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
            alert.setTitle("Error al establecer conexión con el servidor");
            alert.setHeaderText("No se ha podido establecer conexión con el servidor");
            alert.setContentText("Por favor, inténtelo de nuevo más tarde");

            ButtonType buttonTypeRetry = new ButtonType("Reintentar");
            ButtonType buttonTypeExit = new ButtonType("Salir");

            alert.getButtonTypes().setAll(buttonTypeRetry, buttonTypeExit);

            Optional<ButtonType> result;

            do {
                result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == buttonTypeExit) {
                        // Salir de la aplicación
                        GestorPantallas.cerrarSesion(true);
                    }else{
                        conectarServer();
                    }
                }
            } while (result.isPresent() && result.get() == buttonTypeRetry);
            Platform.exit();
            System.exit(0);
        }
    }

    public static void openObjectInputStream() {
        try {
            if (socket == null){
                System.err.println("ERROR::El socket es null. No se puede abrir el ObjectInputStream.");
            }
            if (socket.getInputStream() == null){
                System.err.println("ERROR::El InputStream del socket es null. No se puede abrir el ObjectInputStream.");
            }
            if (objectInputStream != null){
                objectInputStream = null;
            }
            if (socket != null && socket.getInputStream() != null) {
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            }
        } catch (EOFException e) {
            System.err.println("ERROR::Se alcanzó el final del archivo de manera inesperada " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR::No se ha podido iniciar el ObjectInputStream " + e.getMessage());
        }
    }

    public static void openObjectOutputStream() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            if (e instanceof SocketException) {
                System.err.println("ERROR::El socket ya ha sido cerrado. No se puede abrir el ObjectOutputStream.");
            } else {
                System.err.println("ERROR::No se ha podido iniciar el ObjectOutputStream " + e.getMessage());
            }
        }
    }
    public static ObjectOutputStream getObjectOutputStream(){
        return objectOutputStream;
    }
    public static ObjectInputStream getObjectInputStream() {return objectInputStream;}

    public static ArrayList<Propiedad> getPropiedades(Usuario usuario){
        usuario.setAccionServer("getPropiedades");

        ArrayList<Propiedad> propiedades = new ArrayList<>();
        try {
            objectOutputStream.writeObject(usuario);
            objectOutputStream.flush();
            propiedades = (ArrayList<Propiedad>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return propiedades;
    }

    public static void actualizarContactos(Usuario contacto){
        Usuario usuario = new Usuario();
        usuario.setUsuario(Principal.getUsuario().getUsuario());
        usuario.setAccionServer("actListContacto");
        usuario.getContactos().add(contacto);
        System.out.println("Usuario enviado: " + usuario);
        while (!HiloReceptorDatos.getOperativo()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error al dormir el hilo: " + e.getMessage());
            }
        }
        try {
            objectOutputStream.writeObject(usuario);
            objectOutputStream.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error al dormir el hilo: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Socket getSocket() {
        return socket;
    }

    public static ArrayList<Usuario> getUsuariosOnline() {

        Usuario usuario = new Usuario();
        usuario.setUsuario(Principal.getUsuario().getUsuario());
        usuario.setContactos(Principal.getUsuario().getContactos());
        usuario.setAccionServer("obtenerUsuariosOnline");
        try {
            objectOutputStream.writeObject(usuario);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<Usuario> usuariosOnline = new ArrayList<>();
        try {
            usuariosOnline = (ArrayList<Usuario>) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return usuariosOnline;
    }


    public static void actualizarPropiedad(Propiedad propiedad) {
        propiedad.setAccionServer("actualizarPropiedad");
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                objectOutputStream.writeObject(propiedad);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void eliminarPropiedad(Propiedad propiedad) {
        propiedad.setAccionServer("eliminarPropiedad");
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                Propiedad finalPropiedad  = new Propiedad();
                finalPropiedad.setAccionServer("eliminarPropiedad");
                finalPropiedad.setId(propiedad.getId());
                finalPropiedad.setPropietario(propiedad.getPropietario());
                objectOutputStream.writeObject(finalPropiedad);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void addResena(ResenaPropiedad resena) {
        resena.setAccionServer("addResena");
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    System.out.println("Esperando a que el hilo receptor de datos termine");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                System.out.println("Hilo receptor de datos terminado");
                objectOutputStream.writeObject(resena);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public static void eliminarResena(ResenaPropiedad resena) {
        resena.setAccionServer("eliminarResena");
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                objectOutputStream.writeObject(resena);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void insertarTarjeta(Tarjeta tarjeta) {
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                objectOutputStream.writeObject(tarjeta);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void insertarAlquiler(Alquiler alquila) {
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                objectOutputStream.writeObject(alquila);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void eliminarTarjeta(Tarjeta tarjeta) {
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                Tarjeta tarjetaEliminar = new Tarjeta();
                tarjetaEliminar.setAccionServer("eliminarTarjeta");
                tarjetaEliminar.setNumero(tarjeta.getNumero());
                objectOutputStream.writeObject(tarjetaEliminar);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void eliminarContacto(Usuario contacto) {
        Usuario usuario = new Usuario();
        usuario.setUsuario(Principal.getUsuario().getUsuario());
        usuario.setAccionServer("eliminarContacto");
        usuario.getContactos().add(contacto);
        try {
            objectOutputStream.writeObject(usuario);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertarFacturacion(Facturacion facturacion) {
        System.out.println("Insertando facturación");
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                objectOutputStream.writeObject(facturacion);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void actualizarUsuario(Usuario usuario) {
        usuario.setAccionServer("actualizarUsuario");
        usuario.setUsuario(Principal.getUsuario().getUsuario());
        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                objectOutputStream.writeObject(usuario);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }



    public static boolean eliminarCuenta(Usuario usuario) {
        usuario.setAccionServer("eliminarUsuario");

        for (Propiedad propiedad : usuario.getPropiedades()) {
            for (Alquiler alquiler : propiedad.getAlquileres()){
                if (alquiler.getCheckout().isAfter(LocalDateTime.now())){
                    System.out.println("No se puede eliminar la cuenta porque hay alquileres pendientes!!");
                    return false;
                }
            }
        }

        new Thread(() -> {
            try {
                while (!HiloReceptorDatos.getOperativo()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                Usuario usuarioEliminar = new Usuario();
                usuarioEliminar.setUsuario(usuario.getUsuario());
                usuarioEliminar.setAccionServer("eliminarUsuario");
                objectOutputStream.writeObject(usuarioEliminar);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        return true;
    }
}

