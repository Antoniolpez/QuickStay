package ProyectoFinal.Servidor;

import ProyectoFinal.Comun.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Clase HiloServidor que extiende de Thread.
 * Esta clase se encarga de manejar las conexiones de los clientes en hilos
 * separados.
 */
public class HiloServidor extends Thread {

    private static final Logger logger = LogManager.getLogger(HiloServidor.class);

    /** Nombre del usuario que el hilo está atendiendo */
    private String nombreUsuario;
    /** Socket del usuario */
    private final Socket socketUsuario;
    /** Bufferes del hilo del servidot */
    private final BufferesServidor clientee;
    /** Espacio */
    private static final char espacio = ' ';
    /** Usuario */
    private Usuario usuario;
    /** Propiedad */
    private Propiedad propiedad;
    /** Operativo */
    private boolean operativo = false;
    /** Primeras propiedades */
    private int primerasPropiedades;
    /** Primeras get fotos */
    private int primerasGetFotos;
    /** Hilos servidor */
    private final ArrayList<HiloServidor> hilosServidor = Servidor.getHilosServidor();
    /** Usuarios online */
    private static final ArrayList<Usuario> usuariosOnline = new ArrayList<>();
    /** Mensajes */
    private final ArrayList<Mensaje> mensajes = new ArrayList<>();
    /** Hilo mensajes que no llegan a su destino */
    private boolean hiloMensajesNoLlegan = false;

    /**
     * Método estático para obtener la lista de usuarios en línea.
     * 
     * @return ArrayList de usuarios en línea.
     */
    public static ArrayList<Usuario> getUsuariosOnline() {
        return usuariosOnline;
    }

    /**
     * Constructor de la clase HiloServidor.
     * 
     * @param socketUsuario El socket del usuario.
     * @param cliente       El cliente.
     */
    public HiloServidor(Socket socketUsuario, BufferesServidor cliente) {
        this.socketUsuario = socketUsuario;
        this.clientee = cliente;
        cliente.openObjectOutputStream();
        cliente.openObjectInputStream();
        this.start();

    }

    /**
     * Método run que se encarga de manejar los comandos que se envían al servidor.
     */
    @Override
    public void run() {
        comandos();
    }

    /**
     * Método que se encarga de manejar los comandos que se envían al servidor.
     * El servidor escucha el objeto a recibir y actúa en consecuencia.
     */
    public void comandos() {
        while (!socketUsuario.isClosed()) {
            ResenaPropiedad resenaPropiedad = null;
            Mensaje mensaje = null;
            Alquiler alquiler = null;
            Tarjeta tarjeta = null;
            Facturacion facturacion = null;
            try {
                if (clientee.getObjectInputStream() == null) {
                    logger.error("El ObjectInputStream es null");
                }
                String accion = "";
                Usuario usuario = null;
                try {
                    Object obj = clientee.getObjectInputStream().readObject();
                    logger.debug("Objeto recibido: " + obj);
                    if (obj instanceof Usuario) {
                        usuario = (Usuario) obj;
                        accion = usuario.getAccionServer();
                        logger.debug("Acción: " + accion);
                    } else if (obj instanceof Propiedad) {
                        propiedad = (Propiedad) obj;
                        accion = propiedad.getAccionServer();
                    } else if (obj instanceof Mensaje) {
                        accion = "enviarMensajes";
                        mensaje = (Mensaje) obj;
                    } else if (obj instanceof ResenaPropiedad) {
                        resenaPropiedad = (ResenaPropiedad) obj;
                        accion = resenaPropiedad.getAccionServer();
                    } else if (obj instanceof Alquiler) {
                        alquiler = (Alquiler) obj;
                        accion = alquiler.getAccionServer();
                    } else if (obj instanceof Tarjeta) {
                        tarjeta = (Tarjeta) obj;
                        accion = tarjeta.getAccionServer();
                    } else if (obj instanceof Facturacion) {
                        accion = "insertarFacturacion";
                        facturacion = (Facturacion) obj;
                    }
                } catch (ClassNotFoundException e) {
                    logger.error("Error al leer el objeto: " + e.getMessage());
                } catch (NullPointerException e) {
                    logger.error("Error al leer el objeto Usuario: " + e.getMessage());
                }

                if (!accion.isEmpty()) {
                    logger.info(this.getName() + " que se encuentra atendido a " + nombreUsuario
                            + " ha seleccionado la acción " + accion + " en el servidor");
                    boolean ok = false;
                    switch (accion) {

                        case "login" -> {
                            for (Usuario usuario1 : MySQL.selectAllUsers()) {
                                assert usuario != null;
                                if (usuario1.getUsuario().equals(usuario.getUsuario())
                                        && usuario1.getPassword().equals(usuario.getPassword())) {
                                    usuario1.setAccionServer("login ok");
                                    nombreUsuario = usuario.getUsuario();
                                    clientee.setNombreUsuario(nombreUsuario);
                                    this.usuario = usuario1;
                                    logger.info("Bienvenido! '" + nombreUsuario + "' se ha conectado al servidor");
                                    setName(socketUsuario.getInetAddress().getHostAddress() + "_" + nombreUsuario);
                                    clientee.getObjectOutputStream().writeObject(usuario1);
                                    ok = true;
                                    hilosServidor.add(this);
                                    new Thread(() -> {
                                        if (usuario1.getContactos() != null) {
                                            for (Usuario usuario2 : usuario1.getContactos()) {
                                                for (HiloServidor hiloServidor : hilosServidor) {
                                                    if (hiloServidor.usuario.getUsuario()
                                                            .equals(usuario2.getUsuario())) {
                                                        hiloServidor.contactoConectado(usuario1);
                                                    }
                                                }
                                            }
                                        }
                                    }).start();

                                }
                            }
                            if (!ok) {
                                logger.info("Error al iniciar sesión");
                                assert usuario != null;
                                usuario.setAccionServer("login error");
                                clientee.getObjectOutputStream().writeObject(usuario);
                            }
                        }

                        case "registro" -> {
                            // Aquí puedes realizar las acciones necesarias con el objeto Usuario recibido
                            for (Usuario usuario1 : MySQL.selectAllUsers()) {
                                assert usuario != null;
                                if (usuario1.getUsuario().equals(usuario.getUsuario())) {
                                    logger.info("Este usuario ya está registrado");
                                    // clientee.getBufferedWriter().write("Este usuario ya está registrado\n");
                                    // clientee.getBufferedWriter().flush();
                                    usuario1.setAccionServer("Este usuario ya está registrado");
                                    Usuario usuarioEnviar = new Usuario();
                                    usuarioEnviar.setAccionServer("Este usuario ya está registrado");

                                    clientee.getObjectOutputStream().writeObject(usuarioEnviar);
                                    ok = true;
                                }
                            }
                            if (!ok) {
                                assert usuario != null;
                                MySQL.insertarUsuario(usuario);
                                usuario.setAccionServer("Usuario insertado correctamente");
                                clientee.getObjectOutputStream().writeObject(usuario);
                            }
                        }
                        case "getPropiedades" -> {
                            ArrayList<Propiedad> propiedades = MySQL.selectAllPropiedades();
                            clientee.getObjectOutputStream().writeObject(propiedades);
                            logger.info("Propiedades enviadas");
                            primerasPropiedades = propiedades.size();
                            operativo = true;
                        }
                        case "getFotosPropiedades" -> {
                            logger.info("Enviando fotos de la propiedad " + propiedad.getId());
                            clientee.getObjectOutputStream().writeObject(MySQL.selectAllFotos(propiedad.getId()));
                            clientee.getObjectOutputStream().flush();

                            primerasGetFotos++;
                            if (primerasGetFotos == primerasPropiedades) {
                                logger.info("Fotos enviadas, fin de la apertura de sesión");
                                operativo = true;
                            }

                        }
                        case "insertarPropiedad" -> {
                            MySQL.insertarPropiedad(propiedad);
                            logger.info("Propiedad insertada" + propiedad.toString());

                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarPropiedades();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    Iterator<Usuario> iterator = usuariosNoOperativos.iterator();
                                    while (iterator.hasNext()) {
                                        Usuario usuario1 = iterator.next();
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarPropiedades();
                                                iterator.remove();
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "actualizarPropiedad" -> {
                            logger.info("Actualizando propiedad, cod postal: " + propiedad.getCodigoPostal());
                            MySQL.actualizarPropiedad(propiedad);
                            logger.info("Propiedad actualizada" + propiedad.toString());
                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarPropiedades();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    for (Usuario usuario1 : usuariosNoOperativos) {
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarPropiedades();
                                                usuariosNoOperativos.remove(usuario1);
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "enviarMensajes" -> {
                            logger.info("Sección msg");
                            boolean existe = false;
                            for (HiloServidor hiloServidor : hilosServidor) {
                                assert mensaje != null;
                                if (hiloServidor.usuario.getUsuario()
                                        .equals(mensaje.getUsuarioReceptor().getUsuario())) {
                                    mensaje.getUsuarioEmisor().setPassword(null);
                                    // REFACTOR: Guardar en BD para Web Dashboard
                                    MySQL.insertarMensaje(mensaje);
                                    hiloServidor.actualizarChat(mensaje);
                                    existe = true;
                                }
                            }
                            if (!existe) {
                                if (!hiloMensajesNoLlegan) {
                                    hiloMensajesNoLlegan();
                                }
                                mensajes.add(mensaje);
                            }

                        }
                        case "obtenerUsuariosOnline" -> {
                            for (HiloServidor hiloServidor : hilosServidor) {
                                for (Usuario usuarioContacto : this.usuario.getContactos()) {
                                    if (hiloServidor.usuario.getUsuario().equals(usuarioContacto.getUsuario())) {
                                        usuarioContacto.setContactos(null);
                                        usuarioContacto.setPassword(null);
                                        logger.info(usuarioContacto);
                                        if (!this.usuario.equals(usuarioContacto)) {
                                            usuariosOnline.add(usuarioContacto);
                                        }
                                    }
                                }
                            }

                            try {
                                // Limpiar el conjunto antes de la serialización
                                usuariosOnline.forEach(usuario1 -> {
                                    usuario1.setContactos(null);
                                    usuario1.setPassword(null);
                                });

                                clientee.getObjectOutputStream().writeObject(usuariosOnline);
                            } catch (IOException e) {
                                logger.error(
                                        "Error al enviar la actualización de desconexión de usuario de usuariosOnline: "
                                                + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        case "actListContacto" -> {
                            logger.info("Actualizando lista de contactos");
                            assert usuario != null;
                            Usuario contacto = null;
                            logger.info("Contactos: " + usuario.getContactos());
                            boolean existe = false;
                            for (Usuario usuario1 : MySQL.selectAllUsers()) {
                                if (usuario1.getUsuario().equals(usuario.getContactos().get(0).getUsuario())) {
                                    existe = true;
                                    contacto = usuario1;
                                    contacto.setContactos(null);
                                    contacto.setPassword(null);
                                    break;
                                }
                            }
                            Usuario usuarioEnviar = new Usuario();
                            if (existe) {
                                MySQL.insertarContacto(usuario);
                                usuarioEnviar.setUsuario(contacto.getUsuario());
                                usuarioEnviar.setNombre(contacto.getNombre());
                                usuarioEnviar.setApellidos(contacto.getApellidos());
                                usuarioEnviar.setImagenUsuario(contacto.getImagenUsuario());
                                usuarioEnviar.setAccionServer("contacto insertado");
                                clientee.getObjectOutputStream().writeObject(usuarioEnviar);
                            } else {
                                usuarioEnviar.setAccionServer("usuario no existe");
                                clientee.getObjectOutputStream().writeObject(usuario);
                            }
                        }
                        case "eliminarPropiedad" -> {
                            MySQL.eliminarPropiedad(propiedad);
                            logger.info("Propiedad eliminada");
                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarPropiedades();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    for (Usuario usuario1 : usuariosNoOperativos) {
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarPropiedades();
                                                usuariosNoOperativos.remove(usuario1);
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "addResena" -> {
                            assert resenaPropiedad != null;
                            MySQL.addResenaPropiedad(resenaPropiedad);
                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarResenaPropiedad();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    for (Usuario usuario1 : usuariosNoOperativos) {
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarResenaPropiedad();
                                                usuariosNoOperativos.remove(usuario1);
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "eliminarResena" -> {
                            assert resenaPropiedad != null;
                            MySQL.eliminarResenaPropiedad(resenaPropiedad);
                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarResenaPropiedad();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    for (Usuario usuario1 : usuariosNoOperativos) {
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarResenaPropiedad();
                                                usuariosNoOperativos.remove(usuario1);
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "alquilar" -> {
                            logger.info("Alquilando propiedad");
                            assert alquiler != null;
                            MySQL.alquilarPropiedad(alquiler);
                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarPropiedades();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    for (Usuario usuario1 : usuariosNoOperativos) {
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarPropiedades();
                                                usuariosNoOperativos.remove(usuario1);
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "insertarTarjeta" -> {
                            assert tarjeta != null;
                            MySQL.insertarTarjeta(tarjeta);

                        }
                        case "insertarFacturacion" -> {
                            assert facturacion != null;
                            MySQL.insertarFacturacion(facturacion);
                        }
                        case "eliminarTarjeta" -> {
                            assert tarjeta != null;
                            MySQL.eliminarTarjeta(tarjeta);
                        }
                        case "cancelarAlquiler" -> {
                            MySQL.cancelarAlquiler(alquiler);
                            ArrayList<Usuario> usuariosNoOperativos = new ArrayList<>();
                            for (HiloServidor hiloServidor : hilosServidor) {
                                if (hiloServidor.operativo) {
                                    hiloServidor.actualizarPropiedades();
                                } else {
                                    usuariosNoOperativos.add(hiloServidor.usuario);
                                }
                            }
                            new Thread(() -> {
                                while (!usuariosNoOperativos.isEmpty()) {
                                    for (Usuario usuario1 : usuariosNoOperativos) {
                                        for (HiloServidor hiloServidor : hilosServidor) {
                                            if (hiloServidor.usuario.getUsuario().equals(usuario1.getUsuario())) {
                                                hiloServidor.actualizarPropiedades();
                                                usuariosNoOperativos.remove(usuario1);
                                            }
                                        }
                                    }
                                }
                            }).start();
                        }
                        case "eliminarContacto" -> {
                            assert usuario != null;
                            MySQL.eliminarContacto(usuario);
                        }
                        case "actualizarUsuario" -> {
                            assert usuario != null;
                            MySQL.actualizarUsuario(usuario);
                            usuario.setAccionServer("actualizarContacto");
                            actualizarContactos(usuario);
                        }
                        case "eliminarUsuario" -> {
                            assert usuario != null;
                            MySQL.eliminarUsuario(usuario);
                        }
                    }
                }
            } catch (SocketException e) {
                logger.warn("Se ha interrumpido la conexión con el cliente " + clientee.getNombreUsuario()
                        + espacio + e.getMessage());
                usuariosOnline.remove(usuario);
                try {
                    systemExit();
                } catch (IOException ex) {
                    logger.error("Error al salir: " + ex.getMessage());
                }
            } catch (IOException e) {
                try {
                    if (e.getMessage().contains("serialVersionUID")) {
                        logger.fatal(
                                "CLIENTE OBSOLETO, POR FAVOR, ACTUALIZE SU APLICACIÓN (Las librerías de la sección Comun en el cliente y el servidor deben ser iguales)");
                    } else {
                        logger.error("Error en el hilo " + e.getMessage());
                        e.printStackTrace();
                    }
                    try {
                        systemExit();
                    } catch (IOException ex) {
                        logger.error("Error al salir: " + ex.getMessage());
                    }
                } catch (NullPointerException ee) {
                    try {
                        systemExit();
                    } catch (IOException ex) {
                        System.err.println("Error al salir: " + ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Método para cerrar la conexión con el cliente y detener el hilo.
     */
    public void systemExit() throws IOException {
        hilosServidor.remove(this);
        for (HiloServidor hiloServidor : hilosServidor) {
            hiloServidor.contactoDesconectado();
        }
        this.socketUsuario.close();
        this.interrupt();
    }

    /**
     * Método para actualizar el chat con un nuevo mensaje.
     * 
     * @param mensaje El mensaje a enviar.
     */
    public void actualizarChat(Mensaje mensaje) {
        new Thread(() -> {
            try {
                while (!operativo) {
                    Thread.sleep(1000);
                }
                System.out.println("Enviando mensaje de " + mensaje.getUsuarioEmisor().getUsuario() + " a "
                        + usuario.getUsuario());
                clientee.getObjectOutputStream().writeObject(mensaje);
            } catch (Exception e) {
                System.err.println("Error al insertar el mensaje: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Método para manejar la desconexión de un contacto.
     */
    public void contactoDesconectado() {
        while (!operativo) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Error al dormir el hilo: " + e.getMessage());
            }
        }
        ArrayList<Usuario> eliminacionUsuario = new ArrayList<>();
        for (HiloServidor hiloServidor : hilosServidor) {
            eliminacionUsuario.add(hiloServidor.getUsuario());
            System.out.println("Usuarios online " + hiloServidor.getUsuario());
        }
        try {
            clientee.getObjectOutputStream().writeObject(eliminacionUsuario);
        } catch (IOException e) {
            System.err.println(
                    "Error al enviar la actualización de desconexión de usuario de usuariosOnline: " + e.getMessage());
        }
    }

    /**
     * Método para manejar la conexión de un contacto.
     * 
     * @param usuario El usuario que se ha conectado.
     */
    public void contactoConectado(Usuario usuario) {
        new Thread(() -> {
            while (!operativo) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Error al dormir el hilo: " + e.getMessage());
                }
            }
            System.out.println(
                    "El usuario " + usuario.getUsuario() + "está mostrando online a " + clientee.getNombreUsuario());
            usuariosOnline.add(usuario);
            ArrayList<Usuario> usuarios = new ArrayList<>();
            for (Usuario usuario1 : usuariosOnline) {
                if (!usuario1.getUsuario().equals(clientee.getNombreUsuario())) {
                    usuarios.add(usuario1);
                }
            }

            try {
                System.out.println("Usuarios online: " + usuarios.size());
                clientee.getObjectOutputStream().writeObject(usuarios);
            } catch (IOException e) {
                System.err.println("Error al enviar la actualización de desconexión de usuario de usuariosOnline: "
                        + e.getMessage());
            }
        }).start();
    }

    /**
     * Método para actualizar las propiedades.
     */
    private void actualizarPropiedades() {
        ArrayList<Propiedad> propiedades = MySQL.selectAllPropiedades();
        try {
            clientee.getObjectOutputStream().writeObject(propiedades);
        } catch (IOException e) {
            System.err.println("Error al actualizar las propiedades: " + e.getMessage());
        }
        System.out.println("Propiedades enviadas");
        primerasPropiedades = propiedades.size();
    }

    /**
     * Método para actualizar los contactos de un usuario.
     * 
     * @param usuario El usuario cuyos contactos se van a actualizar.
     */
    private void actualizarContactos(Usuario usuario) {
        for (HiloServidor hiloServidor : hilosServidor) {
            if (hiloServidor.operativo) {
                for (Usuario usuario1 : hiloServidor.usuario.getContactos()) {
                    if (usuario1.getUsuario().equals(usuario.getUsuario())) {
                        try {
                            hiloServidor.clientee.getObjectOutputStream().writeObject(usuario);
                        } catch (IOException e) {
                            System.err.println("Error al actualizar los contactos: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * Método para actualizar las reseñas de una propiedad.
     */
    public Usuario getUsuario() {
        return usuario;
    }

    /**
     * Método para manejar los mensajes que no llegan.
     */
    private void actualizarResenaPropiedad() {
        ArrayList<ResenaPropiedad> resenaPropiedades = MySQL.selectAllResenas();
        try {
            clientee.getObjectOutputStream().writeObject(resenaPropiedades);
        } catch (IOException e) {
            System.err.println("Error al actualizar las reseñas: " + e.getMessage());
        }
        System.out.println("Reseñas enviadas");
    }

    private void hiloMensajesNoLlegan() {
        new Thread(() -> {
            while (!mensajes.isEmpty()) {
                try {
                    Iterator<Mensaje> iterator = mensajes.iterator();
                    while (iterator.hasNext()) {
                        Mensaje mensaje = iterator.next();
                        for (HiloServidor hiloServidor : hilosServidor) {
                            if (hiloServidor.usuario.getUsuario().equals(mensaje.getUsuarioReceptor().getUsuario())) {
                                hiloServidor.actualizarChat(mensaje);
                                iterator.remove(); // Remove the message from the list after it has been sent
                                break; // Break the loop as the message has been sent
                            }
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.err.println("Error al dormir el hilo: " + e.getMessage());
                }
            }
            hiloMensajesNoLlegan = true;
        }).start();
    }

}
