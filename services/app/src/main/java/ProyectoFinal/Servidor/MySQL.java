package ProyectoFinal.Servidor;

import ProyectoFinal.Comun.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Clase MySQL.
 * Esta clase se encarga de gestionar la conexión con la base de datos y
 * realizar operaciones CRUD.
 * REFACTORIZADO: Imágenes en disco y Chat en BD.
 */
public class MySQL {

    private static final Logger logger = LogManager.getLogger(MySQL.class);
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static Connection conexion;
    private static String URL;
    private static String user;
    private static String password;

    /**
     * Método para cargar la configuración desde el archivo properties.
     */
    private static void loadConfig() {
        try (java.io.InputStream input = MySQL.class.getClassLoader().getResourceAsStream("config.properties")) {
            java.util.Properties prop = new java.util.Properties();
            if (input == null) {
                logger.warn("No se encontró config.properties, usando valores por defecto para localhost.");
                URL = "jdbc:mysql://localhost:3306/humhouse";
                user = "ProyectoFinal";
                password = "root";
                return;
            }
            prop.load(input);
            URL = prop.getProperty("db.url", "jdbc:mysql://localhost:3306/humhouse");
            user = prop.getProperty("db.user", "ProyectoFinal");
            password = prop.getProperty("db.password", "root");
        } catch (java.io.IOException ex) {
            logger.error("Error cargando configuración: " + ex.getMessage());
        }
    }

    /**
     * Método para conectar con la base de datos.
     * 
     * @return Verdadero si la conexión fue exitosa, falso en caso contrario.
     */
    public static boolean conectar() {
        loadConfig();
        try {
            Class.forName(DRIVER);
            logger.info("Conectando a: " + URL);
            conexion = DriverManager.getConnection(URL, user, password);
            logger.info("Se ha detectado una base de datos");
        } catch (ClassNotFoundException | SQLException e) {
            logger.fatal("No se ha podido cargar el controlador " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Método para seleccionar todas las claves de usuario.
     * 
     * @return Una lista con todas las claves de usuario.
     */
    public static ArrayList<String> selectAllUserKeys() {
        ArrayList<String> keys = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("SELECT id_usuario FROM encryption_keys");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String idUsuario = resultSet.getString("id_usuario");
                keys.add(idUsuario);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            logger.error("ERROR:: al seleccionar todas las claves" + e.getMessage());
        }
        return keys;
    }

    /**
     * Método para seleccionar todos los usuarios.
     * 
     * @return Una lista con todos los usuarios.
     */
    public static ArrayList<Usuario> selectAllUsers() {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("SELECT * FROM usuario");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String idUsuario = resultSet.getString("usuario");
                String password = resultSet.getString("password");
                String nombre = resultSet.getString("nombre");
                String apellidos = resultSet.getString("apellidos");
                String fechaNacimiento = resultSet.getString("fecha_nacimiento");
                String email = resultSet.getString("email");
                String numTelefono = resultSet.getString("num_telefono");
                byte[] imagen = resultSet.getBytes("foto_usuario");
                ArrayList<Tarjeta> tarjetas = selectTarjeta(idUsuario);
                ArrayList<Facturacion> facturas = selectAllFacturas(idUsuario);
                ArrayList<Propiedad> propiedades = selectAllPropiedades(idUsuario);
                Usuario usuario = new Usuario(idUsuario, nombre, apellidos, fechaNacimiento, password, email,
                        numTelefono, null, imagen, tarjetas, facturas, propiedades, "");
                usuario.setContactos(getContactos(usuario));
                usuarios.add(usuario);
            }

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todos los usuarios" + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Método para seleccionar todas las facturas de un usuario.
     * 
     * @param idUsuario El ID del usuario.
     * @return Una lista con todas las facturas del usuario.
     */
    private static ArrayList<Facturacion> selectAllFacturas(String idUsuario) {
        ArrayList<Facturacion> facturas = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("SELECT * FROM facturacion WHERE id_usuario = ?");
            preparedStatement.setString(1, idUsuario);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int idPropiedad = resultSet.getInt("id_propiedad");
                LocalDate fechaFacturacion = resultSet.getTimestamp("fecha_facturacion").toLocalDateTime()
                        .toLocalDate();
                String idTarjeta = resultSet.getString("id_tarjeta");
                String nifUsuario = resultSet.getString("nif_usuario");

                Facturacion facturacion = new Facturacion(id, selectTarjetaID(idTarjeta), selectUser(idUsuario),
                        selectPropiedad(idPropiedad), fechaFacturacion, nifUsuario);
                facturas.add(facturacion);
            }

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todas las facturas" + e.getMessage());
        }
        return facturas;
    }

    /**
     * Método para seleccionar un usuario.
     * 
     * @param idUsuario El ID del usuario.
     * @return El usuario seleccionado.
     */
    private static Usuario selectUser(String idUsuario) {
        Usuario usuario = null;

        String sql = "SELECT * FROM usuario WHERE usuario = ?";

        try (PreparedStatement preparedStatement = conexion.prepareStatement(sql)) {
            preparedStatement.setString(1, idUsuario);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String usuarioId = resultSet.getString("usuario");
                    String nombre = resultSet.getString("nombre");
                    String apellidos = resultSet.getString("apellidos");
                    String fechaNacimiento = resultSet.getString("fecha_nacimiento");
                    String email = resultSet.getString("email");
                    String numTelefono = resultSet.getString("num_telefono");
                    // REFACTOR: Leer URL y cargar bytes desde disco
                    String fotoUrl = resultSet.getString("foto_url");
                    byte[] imagen = loadBytesFromUrl(fotoUrl);
                    usuario = new Usuario(usuarioId, nombre, apellidos, fechaNacimiento, null, email,
                            numTelefono, null, imagen, null, null, null, "");
                } else {
                    System.out.println("No se encontró un usuario con el ID: " + idUsuario);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar el usuario" + e.getMessage());
        }

        return usuario;
    }

    /**
     * Método para seleccionar todas las tarjetas de un usuario.
     * 
     * @param idUsuario El ID del usuario.
     * @return Una lista con todas las tarjetas del usuario.
     */
    private static ArrayList<Tarjeta> selectTarjeta(String idUsuario) {
        ArrayList<Tarjeta> tarjetas = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("SELECT * FROM tarjeta WHERE id_usuario = ?");
            preparedStatement.setString(1, idUsuario);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String numero = resultSet.getString("num_tarjeta");
                String fechaCaducidad = resultSet.getString("fecha_vencimiento");
                int cvv = resultSet.getInt("cvv");
                String direccionFacturacion = resultSet.getString("direccion_facturacion");
                String idUsuarioTarjeta = resultSet.getString("id_usuario");
                Usuario usuario = selectUser(idUsuarioTarjeta);
                Tarjeta tarjeta = new Tarjeta(numero, usuario, fechaCaducidad, cvv, direccionFacturacion, "");
                tarjetas.add(tarjeta);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todas las tarjetas" + e.getMessage());
        }
        return tarjetas;
    }

    /**
     * Método para seleccionar una tarjeta por su ID.
     * 
     * @param numTarjeta El número de la tarjeta.
     * @return La tarjeta seleccionada.
     */
    private static Tarjeta selectTarjetaID(String numTarjeta) {
        Tarjeta tarjeta = null;
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("SELECT * FROM tarjeta WHERE num_tarjeta = ?");
            preparedStatement.setString(1, numTarjeta);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String numero = resultSet.getString("num_tarjeta");
                String fechaCaducidad = resultSet.getString("fecha_vencimiento");
                int cvv = resultSet.getInt("cvv");
                String direccionFacturacion = resultSet.getString("direccion_facturacion");
                String idUsuarioTarjeta = resultSet.getString("id_usuario");
                Usuario usuario = selectUser(idUsuarioTarjeta);
                tarjeta = new Tarjeta(numero, usuario, fechaCaducidad, cvv, direccionFacturacion, "");
            }

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todas las tarjetas" + e.getMessage());
        }
        return tarjeta;
    }

    /**
     * Método para seleccionar todas las propiedades.
     * 
     * @return Una lista con todas las propiedades.
     */
    public static ArrayList<Propiedad> selectAllPropiedades() {
        ArrayList<Propiedad> propiedades = new ArrayList<>();
        System.out.println("Listando  todas las propiedades");
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("SELECT * FROM propiedad");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String tipo = resultSet.getString("tipo");
                String comunidad = resultSet.getString("comunidad");
                String provincia = resultSet.getString("provincia");
                String localidad = resultSet.getString("localidad");
                String pedania = resultSet.getString("pedania");
                String direccion = resultSet.getString("calle");
                float latitud = resultSet.getFloat("latitud");
                float longitud = resultSet.getFloat("longitud");
                float altitud = resultSet.getFloat("altitud");
                int codPostal = resultSet.getInt("codigo_postal");
                String usuario = resultSet.getString("id_propietario");
                String descripcion = resultSet.getString("descripcion");
                float precioHora = resultSet.getFloat("precio_hora");
                Propiedad propiedad = new Propiedad(id, nombre, tipo, comunidad, provincia, localidad, pedania,
                        direccion, latitud, longitud, altitud, codPostal, precioHora, selectUser(usuario), null,
                        descripcion);
                propiedad.setResenas(selectResenasPropiedad(id));
                propiedad.setFotos(selectAllFotos(id));
                propiedad.setAlquileres(selectAllAlquileres(propiedad));

                propiedades.add(propiedad);
            }

            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al seleccionar todas las propiedades sisisisi: " + e.getMessage());
            e.printStackTrace();
        }
        return propiedades;
    }

    /**
     * Método para seleccionar todas las propiedades de un usuario.
     * 
     * @param idUsuario El ID del usuario.
     * @return Una lista con todas las propiedades del usuario.
     */
    private static ArrayList<Propiedad> selectAllPropiedades(String idUsuario) {
        ArrayList<Propiedad> propiedades = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("SELECT * FROM propiedad where id_propietario = ?");
            preparedStatement.setString(1, idUsuario);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String tipo = resultSet.getString("tipo");
                String comunidad = resultSet.getString("comunidad");
                String provincia = resultSet.getString("provincia");
                String localidad = resultSet.getString("localidad");
                String pedania = resultSet.getString("pedania");
                String direccion = resultSet.getString("calle");
                float latitud = resultSet.getFloat("latitud");
                float longitud = resultSet.getFloat("longitud");
                float altitud = resultSet.getFloat("altitud");
                int codPostal = resultSet.getInt("codigo_postal");
                String usuario = resultSet.getString("id_propietario");
                String descripcion = resultSet.getString("descripcion");
                float precioHora = resultSet.getFloat("precio_hora");
                Propiedad propiedad = new Propiedad(id, nombre, tipo, comunidad, provincia, localidad, pedania,
                        direccion, latitud, longitud, altitud, codPostal, precioHora, selectUser(usuario), null,
                        descripcion);
                propiedad.setResenas(selectResenasPropiedad(id));
                propiedad.setFotos(selectAllFotos(id));
                propiedad.setAlquileres(selectAllAlquileres(propiedad));

                propiedades.add(propiedad);
            }

            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al seleccionar todas las propiedades sisisisi: " + e.getMessage());
            e.printStackTrace();
        }
        return propiedades;
    }

    /**
     * Método para seleccionar todas las reseñas de una propiedad.
     * 
     * @param idPropiedad El ID de la propiedad.
     * @return Una lista con todas las reseñas de la propiedad.
     */
    private static ArrayList<ResenaPropiedad> selectResenasPropiedad(int idPropiedad) {
        ArrayList<ResenaPropiedad> resenas = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("SELECT * FROM resena_propiedad WHERE id_propiedad = ?");
            preparedStatement.setInt(1, idPropiedad);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String idUsuario = resultSet.getString("usuario");
                LocalDate fecha = resultSet.getDate("fecha").toLocalDate();
                float estrellas = resultSet.getFloat("estrellas");
                String comentario = resultSet.getString("info");

                ResenaPropiedad resena = new ResenaPropiedad(id, idUsuario, idPropiedad, fecha, estrellas, comentario);
                resenas.add(resena);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todas las reseñas de la propiedad" + e.getMessage());
        }
        return resenas;
    }

    /**
     * Método para seleccionar todas las fotos de una propiedad.
     * 
     * @param idPropiedad El ID de la propiedad.
     * @return Una lista con todas las fotos de la propiedad.
     */
    public static ArrayList<FotosPropiedad> selectAllFotos(int idPropiedad) {
        ArrayList<FotosPropiedad> fotos = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("SELECT * FROM fotos_propiedad");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                // REFACTOR: Leer URL y cargar bytes desde disco
                String fotoUrl = resultSet.getString("url");
                byte[] imagen = loadBytesFromUrl(fotoUrl);
                String formato = resultSet.getString(3);
                int idPropiedadObtenida = resultSet.getInt(4);
                if (idPropiedadObtenida != idPropiedad) {
                    continue;
                }
                FotosPropiedad foto = new FotosPropiedad(id, imagen, formato, idPropiedadObtenida);
                fotos.add(foto);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todas las fotos " + e.getMessage());
        }
        return fotos;
    }

    /**
     * Método para seleccionar todos los alquileres de una propiedad.
     * 
     * @param propiedad La propiedad.
     * @return Una lista con todos los alquileres de la propiedad.
     */
    private static ArrayList<Alquiler> selectAllAlquileres(Propiedad propiedad) {
        ArrayList<Alquiler> alquileres = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("SELECT * FROM alquila WHERE propiedad_alquilada = ?");
            preparedStatement.setInt(1, propiedad.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String usuarioAlquila = resultSet.getString("usuario_alquila");
                Timestamp checkinTimestamp = resultSet.getTimestamp("checkin");
                Timestamp checkoutTimestamp = resultSet.getTimestamp("checkout");
                LocalDateTime checkin = checkinTimestamp.toLocalDateTime();
                LocalDateTime checkout = checkoutTimestamp.toLocalDateTime();
                int numPersonas = resultSet.getInt("num_personas");
                Alquiler alquiler = new Alquiler(id, selectUser(usuarioAlquila), propiedad, numPersonas, checkin,
                        checkout, 0);
                alquileres.add(alquiler);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todos los alquileres " + e.getMessage());
        }
        return alquileres;
    }

    /**
     * Método para insertar un usuario.
     * 
     * @param usuario El usuario a insertar.
     */
    public static void insertarUsuario(Usuario usuario) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO usuario VALUES(?,?,?,?,?,?,?,?)");
            preparedStatement.setString(1, usuario.getUsuario());
            preparedStatement.setString(2, usuario.getPassword());
            preparedStatement.setString(3, usuario.getNombre());
            preparedStatement.setString(4, usuario.getApellidos());
            preparedStatement.setString(5, usuario.getFechaNacimiento());
            preparedStatement.setString(6, usuario.getEmail());
            preparedStatement.setString(6, usuario.getEmail());
            // REFACTOR: Guardar bytes en disco y escribir URL
            String path = saveImageToDisk(usuario.getImagenUsuario(), "user_" + usuario.getUsuario());
            preparedStatement.setString(7, path); // Guardamos URL en vez de Bytes
            preparedStatement.setString(8, usuario.getNumTelefono());
            preparedStatement.execute();
            preparedStatement.close();
            System.out.println("Usuario insertado correctamente");
        } catch (SQLException e) {
            System.err.println("ERROR::Valores duplicados" + e.getMessage());
        }
    }

    /**
     * Método para insertar una propiedad.
     * 
     * @param propiedad La propiedad a insertar.
     */
    public static void insertarPropiedad(Propiedad propiedad) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO propiedad VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            System.out.println("Insertando propiedad");
            System.out.println("Nº1: " + propiedad.getId());
            preparedStatement.setInt(1, propiedad.getId());
            preparedStatement.setString(2, propiedad.getNombre());
            preparedStatement.setString(3, propiedad.getTipo());
            preparedStatement.setString(4, propiedad.getPropietario().getUsuario());
            preparedStatement.setString(5, propiedad.getDescripcion());
            preparedStatement.setString(6, propiedad.getComunidad());
            preparedStatement.setString(7, propiedad.getProvincia());
            preparedStatement.setString(8, propiedad.getLocalidad());
            preparedStatement.setString(9, propiedad.getPedania());
            preparedStatement.setFloat(10, propiedad.getLatitud());
            preparedStatement.setFloat(11, propiedad.getLongitud());
            preparedStatement.setFloat(12, propiedad.getAltitud());
            preparedStatement.setInt(13, propiedad.getCodigoPostal());
            preparedStatement.setString(14, propiedad.getDireccion());
            preparedStatement.setFloat(15, propiedad.getPrecioHora());

            preparedStatement.execute();
            preparedStatement.close();
            if (propiedad.getFotos() != null) {
                for (FotosPropiedad foto : propiedad.getFotos()) {
                    insertarFoto(foto);
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR::" + e.getMessage());
        }
    }

    /**
     * Método para insertar una foto.
     * 
     * @param foto La foto a insertar.
     */
    public static void insertarFoto(FotosPropiedad foto) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO fotos_propiedad VALUES(?,?,?,?)");
            System.out.println("Insertando foto");
            System.out.println("Nº1: " + foto.getId());
            System.out.println("Nº2. " + foto.getIdPropiedad());
            preparedStatement.setInt(1, foto.getId());
            preparedStatement.setInt(1, foto.getId());
            // REFACTOR: Guardar bytes en disco y escribir URL
            String path = saveImageToDisk(foto.getImagenBytes(), "prop_" + foto.getIdPropiedad() + "_" + foto.getId());
            preparedStatement.setString(2, path); // Guardamos URL
            preparedStatement.setString(3, foto.getFormato());
            preparedStatement.setInt(4, foto.getIdPropiedad());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: " + e.getMessage());
        }
    }

    /**
     * Método para insertar un contacto.
     * 
     * @param usuario El usuario al que se le va a insertar el contacto.
     */
    public static void insertarContacto(Usuario usuario) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO lista_contactos VALUES(?,?)");
            preparedStatement.setString(1, usuario.getUsuario());
            preparedStatement.setString(2, usuario.getContactos().get(usuario.getContactos().size() - 1).getUsuario());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al insertar Contacto" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método para alquilar una propiedad.
     * Este método es synchronized para evitar problemas de concurrencia.
     * 
     * @param alquiler El alquiler a insertar.
     */
    public synchronized static void alquilarPropiedad(Alquiler alquiler) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO alquila VALUES(?,?,?,?,?,?)");
            preparedStatement.setInt(1, alquiler.getId());
            preparedStatement.setInt(3, alquiler.getPropiedadAlquilada().getId());
            preparedStatement.setString(2, alquiler.getUsuarioAlquila().getUsuario());
            preparedStatement.setInt(4, alquiler.getNumPersonas());
            preparedStatement.setString(5, alquiler.getCheckin().toString());
            preparedStatement.setString(6, alquiler.getCheckout().toString());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: " + e.getMessage());
        }
    }

    /**
     * Método para actualizar un usuario.
     * Este método es synchronized para evitar problemas de concurrencia.
     * 
     * @param usuario El usuario a actualizar.
     */
    public synchronized static void actualizarUsuario(Usuario usuario) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "UPDATE usuario SET nombre = ?, apellidos = ?,password = ?, num_telefono = ?, foto_usuario = ?, email = ? WHERE usuario = ?");
            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getApellidos());
            preparedStatement.setString(3, usuario.getPassword());
            preparedStatement.setString(4, usuario.getNumTelefono());
            preparedStatement.setBytes(5, usuario.getImagenUsuario());
            preparedStatement.setString(6, usuario.getEmail());
            preparedStatement.setString(7, usuario.getUsuario());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al actualizar usuario" + e.getMessage());
        }
    }

    /**
     * Método para seleccionar una propiedad.
     * 
     * @param idPropiedad El ID de la propiedad.
     * @return La propiedad seleccionada.
     */
    private static Propiedad selectPropiedad(int idPropiedad) {
        Propiedad propiedad = null;
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("SELECT * FROM propiedad WHERE id = ?");
            preparedStatement.setInt(1, idPropiedad);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String nombre = resultSet.getString("nombre");
                String tipo = resultSet.getString("tipo");
                String comunidad = resultSet.getString("comunidad");
                String provincia = resultSet.getString("provincia");
                String localidad = resultSet.getString("localidad");
                String pedania = resultSet.getString("pedania");
                String direccion = resultSet.getString("calle");
                float latitud = resultSet.getFloat("latitud");
                float longitud = resultSet.getFloat("longitud");
                float altitud = resultSet.getFloat("altitud");
                int codPostal = resultSet.getInt("codigo_postal");
                String usuario = resultSet.getString("id_propietario");
                String descripcion = resultSet.getString("descripcion");
                float precioHora = resultSet.getFloat("precio_hora");
                propiedad = new Propiedad(id, nombre, tipo, comunidad, provincia, localidad, pedania, direccion,
                        latitud, longitud, altitud, codPostal, precioHora, selectUser(usuario), null, descripcion);
                propiedad.setResenas(selectResenasPropiedad(id));
                propiedad.setFotos(selectAllFotos(id));
            }

            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar la propiedad" + e.getMessage());
        }
        return propiedad;
    }

    /**
     * Método para obtener los contactos de un usuario.
     * 
     * @param usuario El usuario.
     * @return Una lista con los contactos del usuario.
     */
    public static ArrayList<Usuario> getContactos(Usuario usuario) {
        ArrayList<Usuario> contactos = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("""
                    SELECT c.*
                       FROM usuario AS u
                       JOIN lista_contactos AS lc ON u.usuario = lc.idUsuario
                       JOIN usuario AS c ON lc.contacto = c.usuario
                       WHERE u.usuario = ?""");
            preparedStatement.setString(1, usuario.getUsuario());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String usuarioContacto = resultSet.getString("usuario");
                String nombre = resultSet.getString("nombre");
                String apellidos = resultSet.getString("apellidos");
                String fechaNacimiento = resultSet.getString("fecha_nacimiento");
                String email = resultSet.getString("email");
                String numTelefono = resultSet.getString("num_telefono");
                byte[] imagen = resultSet.getBytes("foto_usuario");
                contactos.add(new Usuario(usuarioContacto, nombre, apellidos, fechaNacimiento, null,
                        email, numTelefono, null, imagen, null, null, null, ""));
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar los contactos" + e.getMessage());
        }
        return contactos;
    }

    /**
     * Método para actualizar una propiedad.
     * Este método es synchronized para evitar problemas de concurrencia.
     * 
     * @param propiedad La propiedad a actualizar.
     */
    public synchronized static void actualizarPropiedad(Propiedad propiedad) {
        PreparedStatement preparedStatement;
        try {

            borrarFotosPropiedad(propiedad.getId());
            if (propiedad.getFotos() != null) {
                System.out.println("Insertando fotos");
                for (FotosPropiedad foto : propiedad.getFotos()) {
                    System.out.println("Insertando foto");
                    insertarFoto(foto);
                }
            }

            preparedStatement = conexion.prepareStatement(
                    "UPDATE propiedad SET nombre = ?, tipo = ?, comunidad = ?, provincia = ?, localidad = ?, pedania = ?, calle = ?, latitud = ?, longitud = ?, altitud = ?, codigo_postal = ?, id_propietario = ?, descripcion = ? WHERE id = ?");
            preparedStatement.setString(1, propiedad.getNombre());
            preparedStatement.setString(2, propiedad.getTipo());
            preparedStatement.setString(3, propiedad.getComunidad());
            preparedStatement.setString(4, propiedad.getProvincia());
            preparedStatement.setString(5, propiedad.getLocalidad());
            preparedStatement.setString(6, propiedad.getPedania());
            preparedStatement.setString(7, propiedad.getDireccion());
            preparedStatement.setFloat(8, propiedad.getLatitud());
            preparedStatement.setFloat(9, propiedad.getLongitud());
            preparedStatement.setFloat(10, propiedad.getAltitud());
            preparedStatement.setInt(11, propiedad.getCodigoPostal());
            preparedStatement.setString(12, propiedad.getPropietario().getUsuario());
            preparedStatement.setString(13, propiedad.getDescripcion());
            preparedStatement.setInt(14, propiedad.getId());
            preparedStatement.execute();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println("ERROR:: al actualizar propiedad" + e.getMessage());
        }
    }

    /**
     * Método para borrar las fotos de una propiedad.
     * 
     * @param idPropiedad El ID de la propiedad.
     */
    private static void borrarFotosPropiedad(int idPropiedad) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM fotos_propiedad WHERE idPropiedad = ?");
            preparedStatement.setInt(1, idPropiedad);
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al borrar fotos de la propiedad" + e.getMessage());
        }
    }

    /**
     * Método para eliminar una propiedad.
     * 
     * @param propiedad La propiedad a eliminar.
     */
    public static void eliminarPropiedad(Propiedad propiedad) {
        PreparedStatement preparedStatement;
        try {
            if (!propiedad.getAlquileres().isEmpty()) {
                return;
            }

            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM resena_propiedad WHERE id_propiedad = ?");
            preparedStatement.setInt(1, propiedad.getId());
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM fotos_propiedad WHERE idPropiedad = ?");
            preparedStatement.setInt(1, propiedad.getId());
            preparedStatement.execute();
            preparedStatement.close();

            // Queda eliminar las reseñas existentes de la propiedad

            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM propiedad WHERE id = ?");
            preparedStatement.setInt(1, propiedad.getId());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al borrar propiedad");
        }
    }

    /**
     * Método para añadir una reseña a una propiedad.
     * 
     * @param resenaPropiedad La reseña a añadir.
     */
    public static void addResenaPropiedad(ResenaPropiedad resenaPropiedad) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO resena_propiedad VALUES(?,?,?,?,?,?)");
            preparedStatement.setInt(1, resenaPropiedad.getId());
            preparedStatement.setString(2, resenaPropiedad.getUsuario());
            preparedStatement.setInt(3, resenaPropiedad.getIdPropiedad());
            preparedStatement.setString(4, resenaPropiedad.getComentario());
            preparedStatement.setDate(5, Date.valueOf(resenaPropiedad.getFecha()));
            preparedStatement.setFloat(6, resenaPropiedad.getEstrellas());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: " + e.getMessage());
        }
    }

    /**
     * Método para seleccionar todas las reseñas.
     * 
     * @return Una lista con todas las reseñas.
     */
    public static ArrayList<ResenaPropiedad> selectAllResenas() {
        ArrayList<ResenaPropiedad> resenas = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = conexion.prepareStatement("SELECT * FROM resena_propiedad");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String idUsuario = resultSet.getString("usuario");
                int idPropiedad = resultSet.getInt("id_propiedad");
                LocalDate fecha = resultSet.getDate("fecha").toLocalDate();
                float estrellas = resultSet.getFloat("estrellas");
                String comentario = resultSet.getString("info");

                ResenaPropiedad resena = new ResenaPropiedad(id, idUsuario, idPropiedad, fecha, estrellas, comentario);
                resenas.add(resena);
            }
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al seleccionar todas las reseñas" + e.getMessage());
        }
        return resenas;
    }

    /**
     * Carga los bytes de una imagen desde una URL local.
     * 
     * @param url La URL (ruta) de la imagen.
     * @return Los bytes de la imagen.
     */
    private static byte[] loadBytesFromUrl(String url) {
        if (url == null || url.isEmpty())
            return null;
        File file = new File(url.startsWith("/") ? "/var/www/html/quickstay" + url : url);
        if (!file.exists())
            return null; // Retorna null o imagen por defecto
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            System.err.println("Error leyendo imagen de disco: " + e.getMessage());
            return null;
        }
    }

    private static String saveImageToDisk(byte[] bytes, String fileName) {
        if (bytes == null)
            return "/images/default.png";
        String relativePath = "/images/" + fileName + ".jpg";
        String absolutePath = "/var/www/html/quickstay" + relativePath;
        try {
            File file = new File(absolutePath);
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(bytes);
            }
            return relativePath;
        } catch (IOException e) {
            System.err.println("Error guardando imagen en disco: " + e.getMessage());
            return "/images/default.png";
        }
    }

    // --- MÉTODOS CHAT PERSISTENTE (MODO DIOS) ---

    public static void insertarMensaje(Mensaje mensaje) {
        try {
            PreparedStatement ps = conexion.prepareStatement(
                    "INSERT INTO mensajes (remitente, destinatario, mensaje, fecha) VALUES (?, ?, ?, NOW())");
            ps.setString(1, mensaje.getUsuarioEmisor().getUsuario());
            ps.setString(2, mensaje.getUsuarioReceptor().getUsuario());
            ps.setString(3, mensaje.getMensaje());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error guardando mensaje en BD: " + e.getMessage());
        }
    }

    // NOTE: This assumes 'Mensaje' class has compatible constructor, forcing
    // adaptation if needed
    /**
     * Método para eliminar una reseña de una propiedad.
     * 
     * @param resenaPropiedad La reseña a eliminar.
     */
    public static void eliminarResenaPropiedad(ResenaPropiedad resenaPropiedad) {
        try {
            PreparedStatement preparedStatement = conexion
                    .prepareStatement("DELETE FROM resena_propiedad WHERE id = ?");
            preparedStatement.setInt(1, resenaPropiedad.getId());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al borrar reseña");
        }
    }

    /**
     * Método para insertar una tarjeta.
     * 
     * @param tarjeta La tarjeta a insertar.
     */
    public static void insertarTarjeta(Tarjeta tarjeta) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO tarjeta VALUES(?,?,?,?,?,?)");
            preparedStatement.setString(1, tarjeta.getNumero());
            preparedStatement.setString(2, tarjeta.getFechaCaducidad());
            preparedStatement.setString(3,
                    tarjeta.getTitular().getNombre().concat(" ").concat(tarjeta.getTitular().getApellidos()));
            preparedStatement.setInt(4, tarjeta.getCvv());
            preparedStatement.setString(5, tarjeta.getDireccionFacturacion());
            preparedStatement.setString(6, tarjeta.getTitular().getUsuario());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al insertar tarjeta" + e.getMessage());
        }
    }

    /**
     * Método para eliminar una tarjeta.
     * 
     * @param tarjeta La tarjeta a eliminar.
     */
    public static void eliminarTarjeta(Tarjeta tarjeta) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM tarjeta WHERE num_tarjeta = ?");
            preparedStatement.setString(1, tarjeta.getNumero());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al borrar tarjeta");
        }
    }

    /**
     * Método para cancelar un alquiler.
     * 
     * @param alquiler El alquiler a cancelar.
     */
    public static void cancelarAlquiler(Alquiler alquiler) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM alquila WHERE id = ?");
            preparedStatement.setInt(1, alquiler.getId());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al borrar alquiler");
        }
    }

    /**
     * Método para eliminar un contacto.
     * 
     * @param usuario El usuario al que se le va a eliminar el contacto.
     */
    public static void eliminarContacto(Usuario usuario) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM lista_contactos WHERE idUsuario = ? AND contacto = ?");
            preparedStatement.setString(1, usuario.getUsuario());
            preparedStatement.setString(2, usuario.getContactos().get(usuario.getContactos().size() - 1).getUsuario());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al borrar contacto");
        }
    }

    /**
     * Método para insertar una facturación.
     * 
     * @param facturacion La facturación a insertar.
     */
    public static void insertarFacturacion(Facturacion facturacion) {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = conexion.prepareStatement(
                    "INSERT INTO facturacion (id, id_usuario, id_propiedad, fecha_facturacion, id_tarjeta,nif_usuario) VALUES (?,?,?,?,?,?)");
            preparedStatement.setInt(1, facturacion.getId());
            preparedStatement.setString(2, facturacion.getUsuario().getUsuario());
            preparedStatement.setInt(3, facturacion.getPropiedad().getId());

            LocalDate fechalocal = facturacion.getFecha();
            Date fechaSql = Date.valueOf(fechalocal);
            preparedStatement.setDate(4, fechaSql);
            preparedStatement.setString(5, facturacion.getTarjeta().getNumero());

            preparedStatement.setString(6, facturacion.getNif());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.out.println("ERROR:: al insertar facturación");
            e.printStackTrace(); // Adding stack trace for better error diagnostics
        }
    }

    /**
     * Método para eliminar un usuario.
     * 
     * @param usuario El usuario a eliminar.
     */
    public static void eliminarUsuario(Usuario usuario) {
        PreparedStatement preparedStatement;
        try {
            try {
                for (Propiedad propiedad : usuario.getPropiedades()) {
                    eliminarPropiedad(propiedad);
                }
            } catch (NullPointerException e) {
                System.out.println("No hay propiedades que borrar");
            }
            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM resena_propiedad WHERE usuario = ?");
            preparedStatement.setString(1, usuario.getUsuario());
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM lista_contactos WHERE idUsuario = ? OR contacto = ?");
            preparedStatement.setString(1, usuario.getUsuario());
            preparedStatement.setString(2, usuario.getUsuario());
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = conexion.prepareStatement(
                    "DELETE FROM usuario WHERE usuario = ?");
            preparedStatement.setString(1, usuario.getUsuario());
            preparedStatement.execute();
            preparedStatement.close();
        } catch (SQLException e) {
            System.err.println("ERROR:: al borrar usuario");
        }
    }
}
