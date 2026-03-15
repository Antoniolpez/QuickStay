package ProyectoFinal.Cliente;

import ProyectoFinal.Cliente.Chat.TreeSetMsg;
import ProyectoFinal.Cliente.Controladores.Principal;
import ProyectoFinal.Cliente.Librerias.ArrayUbicacion;
import ProyectoFinal.Cliente.Librerias.NumerosTelefono;
import ProyectoFinal.Cliente.Librerias.Ubicacion;
import ProyectoFinal.Comun.Mensaje;
import ProyectoFinal.Comun.Usuario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Local {
    private static String nombreUsuario;
    private static String password;
    private static boolean guardar = false;
    private static ArrayList<Ubicacion> codigosPostales;
    private static File fileData;
    private static File fileProvincias;
    private static File fileLocalidades;
    private static File fileSocketProperties;
    private static final String USERHOME = System.getProperty("user.home");

    private static Usuario usuario;
    public static void setNombreUsuario(String nombreUsuario) {
        Local.nombreUsuario = nombreUsuario;
    }

    public static void setPassword(String password) {
        Local.password = password;
    }

    public static void setGuardar(boolean guardar) {
        Local.guardar = guardar;
    }

    public static void setUsuario(Usuario usuario) {
        Local.usuario = usuario;
    }

    public static void loginSave(){
        String loginData = String.valueOf(guardar).concat(" ").concat(nombreUsuario).concat(" ").concat(password);
        escribirLineaEspecifica(0, loginData, fileData);
    }
    public static void saveStateScreen(double width, double height, double posX, double posY) {
        String screenData = width + " " + height + " " + posX + " " + posY;
        escribirLineaEspecifica(1, screenData, fileData);
    }

    public static String leerLineaEspecifica(int nlinea){
        String lineaEspecifica = null;
        fileData = new File(USERHOME + "/Humhouse/data/data.txt");
        try {
            if (!fileData.exists()) {
                System.out.println("No se ha encontrado el archivo de guardado.");
                crearRutaFileData();
            }
            BufferedReader br = new BufferedReader(new FileReader(fileData.getAbsolutePath()));
            int i = 0;
            while(i <= nlinea){
                lineaEspecifica = br.readLine();
                if(lineaEspecifica == null){
                    break;
                }
                // Ignora las líneas vacías
                if(!lineaEspecifica.trim().isEmpty()){
                    i++;
                }
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("No se ha encontrado el archivo de guardado." + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return lineaEspecifica;
    }

    public static void escribirLineaEspecifica(int nlinea, String texto, File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            // Si el archivo tiene menos líneas que nlinea, añade líneas vacías hasta que tenga nlinea + 1
            while (lines.size() <= nlinea) {
                lines.add("");
            }
            // Reemplaza la línea nlinea con el texto
            lines.set(nlinea, texto);
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo." + e.getMessage());
        }
    }

    public static void crearRutaFileSocketProperties(){
        fileSocketProperties = new File(USERHOME + "/Humhouse/SocketProperties/SocketProperties.conf");
        if (!fileSocketProperties.exists()) {
            try {

                new File(USERHOME + "/Humhouse/SocketProperties").mkdirs();
                fileSocketProperties.createNewFile();
                escribirLineaEspecifica(0, "Host: 7.tcp.eu.ngrok.io", fileSocketProperties);
                escribirLineaEspecifica(1, "Port: 14917", fileSocketProperties);
            } catch (IOException e) {
                System.err.println("Error al crear el archivo." + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static String[] getSocketProperties() {
        String[] socketProperties = new String[4];
        try {
            fileSocketProperties = new File(USERHOME + "/Humhouse/SocketProperties/SocketProperties.conf");
            if (!fileSocketProperties.exists()) {
                crearRutaFileSocketProperties();
            }
            BufferedReader br = new BufferedReader(new FileReader(fileSocketProperties));
            String linea;
            int i = 0;
            while ((linea = br.readLine()) != null) {
                socketProperties[i] = linea.substring(linea.indexOf(":") + 2);
                i++;
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("No se ha encontrado el archivo de propiedades del socket." + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return socketProperties;
    }


    private static void crearRutaFileData(){
        fileData = new File(USERHOME + "/Humhouse/data/data.txt");
        if (!fileData.exists()) {
            try {

                new File(USERHOME + "/Humhouse/data").mkdirs();
                fileData.createNewFile();
            } catch (IOException e) {
                System.err.println("Error al crear el archivo." + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void crearRutaFileProvincias(){
        fileProvincias = new File(USERHOME + "/Humhouse/data/Provincias.txt");
        if (!fileProvincias.exists()) {
            try {
                new File(USERHOME + "/Humhouse/data/").mkdirs();
                fileProvincias.createNewFile();
                fileProvincias.setWritable(false);
            } catch (IOException e) {
                System.err.println("Error al crear el archivo." + e.getMessage());
            }
        }
    }

    private static void crearRutaFileLocalidades(){
        fileLocalidades = new File(USERHOME + "/Humhouse/data/Localidades.txt");
        if (!fileLocalidades.exists()) {
            try {
                new File(USERHOME + "/Humhouse/data/").mkdirs();
                fileLocalidades.createNewFile();
                fileLocalidades.setWritable(false);
            } catch (IOException e) {
                System.err.println("Error al crear el archivo." + e.getMessage());
            }
        }
    }

    public static ArrayList<Ubicacion> getAllProvincias(){
        try {
            fileProvincias = new File(USERHOME + "/Humhouse/data/Provincias.txt");
            if (!fileProvincias.exists()) {
                crearRutaFileProvincias();
            }
            BufferedReader br = new BufferedReader(new FileReader(fileProvincias));
            String linea;
            String nombreProvincia;
            String  codPostal;
            codigosPostales = new ArrayList<>();
            while ((linea = br.readLine()) != null) {
                nombreProvincia = linea.substring(0, linea.indexOf(";"));
                codPostal = linea.substring(linea.indexOf(";") + 1);
                codigosPostales.add(new Ubicacion(nombreProvincia,"", codPostal));
            }
            br.close();
            return codigosPostales;
        } catch (FileNotFoundException e) {
            System.err.println("No se ha encontrado el archivo de provincias." + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return null;
    }



    public static double[] getScreenData() {
        double[] screenData = new double[4];
        try {
            String dataLine = leerLineaEspecifica(1); // Lee la primera línea del archivo
            if (dataLine != null) {
                String[] data = dataLine.split(" ");
                for (int i = 0; i < data.length; i++) {
                    screenData[i] = Double.parseDouble(data[i]);
                }
            }else{
                screenData = null;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error al convertir los datos de la pantalla." + e.getMessage());
        }
        return screenData;
    }

    public static ArrayUbicacion<Ubicacion> getAllLocalidades() {
        try {
            fileLocalidades = new File(USERHOME + "/Humhouse/data/Localidades.txt");
            if (!fileLocalidades.exists()) {
                crearRutaFileLocalidades();
            }
            BufferedReader br = new BufferedReader(new FileReader(fileLocalidades));
            String linea;
            ArrayUbicacion<Ubicacion> ubicaciones = new ArrayUbicacion<>();
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                String comunidad = datos[0];
                String provincia = datos[1];
                String localidad = datos[2];
                double latitud = Double.parseDouble(datos[3]);
                double longitud = Double.parseDouble(datos[4]);
                double altitud = Double.parseDouble(datos[5]);
                int poblacionTotal = Integer.parseInt(datos[6]);
                int poblacion1 = Integer.parseInt(datos[7]);
                int poblacion2 = Integer.parseInt(datos[8]);
                String codigoPostal = getCodigoPostalPorProvincia(provincia);
                ubicaciones.addUbicacion(new Ubicacion(comunidad, provincia, localidad, latitud, longitud, altitud, poblacionTotal, poblacion1, poblacion2, codigoPostal));
            }
            br.close();
            System.out.println("Localidades cargadas correctamente." + ubicaciones.getALL() + " localidades cargadas.");
            return ubicaciones;
        } catch (FileNotFoundException e) {
            System.err.println("No se ha encontrado el archivo de localidades." + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return null;
    }

    public static ArrayList<NumerosTelefono> leerPrefijosNumerosTelefono() {
        ArrayList<NumerosTelefono>  prefijos = new ArrayList<>();
        try {
            File file = new File(USERHOME + "/Humhouse/data/PrefijosTelefonicos.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                String pais = datos[0];
                String prefijo = datos[1];
                prefijos.add(new NumerosTelefono(pais, prefijo));
            }
            br.close();
        } catch (FileNotFoundException e) {
            System.err.println("No se ha encontrado el archivo de prefijos telefónicos." + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
        return prefijos;
    }



    public static String getCodigoPostalPorProvincia(String provinciaBuscada) {
        for (Ubicacion ubicacion : codigosPostales) {
            if (ubicacion.getProvincia().equalsIgnoreCase(provinciaBuscada)) {
                return ubicacion.getCodigoPostal();
            }
        }
        return "null"; // Devuelve null si no se encuentra la provincia
    }

    public static File getFileData() {
        return fileData;
    }

    public static void escribirEnChat(Mensaje mensajeObj) {
        Usuario usuarioEmisor = mensajeObj.getUsuarioEmisor();
        Usuario usuarioReceptor = mensajeObj.getUsuarioReceptor();
        String mensaje = mensajeObj.getMensaje();
        LocalDateTime fechaLlegada = mensajeObj.getFechaLlegada();
        File file;
        if(usuarioReceptor.getUsuario().equals(Principal.getUsuario().getUsuario())){
            file = getFileChatUsers(usuarioEmisor);
        }else{
            file = getFileChatUsers(usuarioReceptor);
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(usuarioEmisor.getUsuario() + ";" + mensaje + ";" + usuarioReceptor.getUsuario() + ";" + fechaLlegada.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    public static void leerChat(Usuario usuarioAjeno) {
        System.out.println("Leyendo chat con: " + usuarioAjeno.getUsuario());
        File file = getFileChatUsers(usuarioAjeno);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                System.out.println("Leyendo línea: " + linea);
                try {
                    String[] datos = linea.split(";");
                    if (datos.length < 4) {
                        System.err.println("Formato de línea incorrecto: " + linea);
                        continue;
                    }
                    Usuario usuarioEmisor = new Usuario();
                    usuarioEmisor.setUsuario(datos[0]);
                    String mensaje = datos[1];
                    Usuario usuarioReceptor = new Usuario();
                    usuarioReceptor.setUsuario(datos[2]);
                    LocalDateTime fechaMensaje = LocalDateTime.parse(datos[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    TreeSetMsg.getInstance().addMensajeLocal(new Mensaje(usuarioEmisor, mensaje, usuarioReceptor, fechaMensaje));
                    System.out.println("Mensaje leído: " + mensaje);
                } catch (ArrayIndexOutOfBoundsException | DateTimeParseException e) {
                    System.err.println("Error al procesar la línea: " + linea + " - " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("No se ha encontrado el archivo de chat: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }
    }


    private static File getFileChatUsers(Usuario usuario) {
        File file = new File(USERHOME + "/Humhouse/Chats/ChatUser"+ usuario.getUsuario() +".txt");
        if (!file.exists()){
            try {
                File fileChat = new File(USERHOME + "/Humhouse/Chats/");
                fileChat.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error al crear el archivo." + e.getMessage());
            }
        }
        return file;
    }
}
