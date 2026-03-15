package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.*;
import ProyectoFinal.Cliente.Celdas.ContactosCeldas;
import ProyectoFinal.Cliente.Celdas.PropiedadesCeldas;
import ProyectoFinal.Cliente.Chat.TreeSetMsg;
import ProyectoFinal.Cliente.Chat.HiloReceptorDatos;
import ProyectoFinal.Cliente.Librerias.ArrayUbicacion;
import ProyectoFinal.Cliente.Librerias.ResizeListener;
import ProyectoFinal.Cliente.Librerias.TextoAutoCompletado;
import ProyectoFinal.Cliente.Librerias.Ubicacion;
import ProyectoFinal.Comun.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.embed.swing.SwingFXUtils;

import java.awt.*;
import javafx.scene.control.TextArea;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;


public class Principal implements Initializable {
    public TextField ubicacionField;
    public TextField hostField;
    // Panel izquierdo
    public Button mostrarChatBt;
    public Button mostrarMapaBt;
    public Button mostrarInsertarPropiedadBt;
    public Button mostrarMisPropiedadesBt;
    public Button mostrarPropiedadesBt;
    public Label labelOnlineChat;
    public HBox topleft;
    public HBox settingsHbox;
    public VBox chatActualVbox;
    public VBox chatEnterovbox;
    // Panel superior
    @FXML
    private HBox top;
    //Panel central
    @FXML
    private BorderPane pantallaPrincipal;
    // Sección de propiedades
    @FXML
    private ListView<Propiedad> allHousesList;
    @FXML
    private ListView<Propiedad> userHousesList;
    @FXML
    private TextField houseAddress;
    @FXML
    private TextField houseAddressProvincia;
    @FXML
    private TextArea houseDescription;
    @FXML
    private TextField houseTipo;
    @FXML
    private Button addHouseButton;
    @FXML
    private Label labelnombreUsuario;
    @FXML
    private AnchorPane panelAgregarPropiedad;
    @FXML
    private ImageView configLoginImg;
    @FXML
    private ProgressBar progressBarAll;
    @FXML
    private HBox fotosHBox;
    @FXML
    private Label errorMaxFotLabel;
    @FXML
    private TextField houseName;
    @FXML
    private ScrollPane allHousesListscroll;
    @FXML
    private BorderPane allHousesListAnchorPane;
    @FXML
    private ScrollPane userHousesListscroll;
    @FXML
    private BorderPane userHousesListAnchorPane;
    @FXML
    public HBox hboxMisPropiedades;
    @FXML
    private AnchorPane contenedorMapa;
    @FXML
    private AnchorPane contenedorChat;
    @FXML
    private AnchorPane contenedorMapaAdd;
    @FXML
    private SwingNode mapaSwingNodeAdd;
    @FXML
    private SwingNode mapaSwingNode;
    @FXML
    private TextField prefijoCodPost;
    @FXML
    private TextField codPost;
    @FXML
    private TextField localidadField;
    @FXML
    private TextField comunidadesField;
    @FXML
    private TextField pedaniaField;
    @FXML
    private ListView<Usuario> listaContactos = new ListView<>();
    private final ObservableList<Usuario> contactos = FXCollections.observableArrayList();
    private final ObservableList<Usuario> filteredContactos = FXCollections.observableArrayList();

    public TextField buscarContactoField;
    public TextField agregarContactoField;
    public TextField eliminarContactoField;

    private JMapViewer mapViewer;
    private JMapViewer mapViewerAdd;
    private static Usuario usuario;
    private final ObservableList<Propiedad> allHouses = FXCollections.observableArrayList();
    private final ObservableList<Propiedad> allHousesClon = FXCollections.observableArrayList();
    private final ObservableList<Propiedad> userHouses = FXCollections.observableArrayList();
    private Usuario userLastChat = null;
    @FXML
    private Label chatContactName;
    @FXML
    private ScrollPane chatMessages;
    @FXML
    private TextField messageField;

    private static final int SIZEFOTOS = 100;
    private ArrayUbicacion<Ubicacion> ubicaciones = new ArrayUbicacion<>();
    private final Image MAPA =new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/mapa.png")));
    private final Image MODIFICAR = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/modify.png")));

    private static double[] screen = new double[4];

    private boolean datosObtenidos;
    private boolean direccionesCargadas;
    private static Thread hiloReceptorDatos;
    private Usuario usuarioEnChat;

    private static ArrayList<Usuario> usuariosOnline = new ArrayList<>();
    private static FXMLLoader loader;
    private static Node nodo;
    private Propiedad propiedadAntigua = null;
    boolean esnull = false;
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private static boolean actualizacionContactos = false;

    public static Thread getHiloReceptorDatos() {
        return hiloReceptorDatos;
    }

    public void actualizarListaUsuariosOnline(ArrayList<Usuario> usuarios){
        usuariosOnline.clear();
        usuariosOnline.addAll(usuarios);
        if (usuarioEnChat != null) {
            new Thread(() -> {
                boolean encontrado = false;
                for (Usuario usuario : Principal.usuario.getContactos()){
                    for (Usuario usuario1 : usuariosOnline) {
                        if (usuario.getUsuario().equals(usuarioEnChat.getUsuario())) {
                            if (usuario.getUsuario().equals(usuario1.getUsuario())){
                                Platform.runLater(() -> labelOnlineChat.setText("Conectado"));
                                encontrado = true;
                                break;
                            }else{
                                Platform.runLater(() -> labelOnlineChat.setText("Desconectado"));
                            }
                        }
                    }
                    if (!encontrado){
                        Platform.runLater(() -> labelOnlineChat.setText("Desconectado"));
                    }
                }

            }).start();
        }
    }

    public static void setUsuario(Usuario usuario) {
        Principal.usuario = usuario;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatEnterovbox.setVisible(false);
        try {
            if (Local.leerLineaEspecifica(2).contains("true")) {
                Node node = top.getChildren().get(0);
                top.getChildren().remove(node);
            } else {
                pantallaPrincipal.setTop(top);
            }
        }catch (NullPointerException e){
            Local.escribirLineaEspecifica(2, "WindowsMode: " + "false", Local.getFileData());
        }
        mapViewer = new JMapViewer();
        mapViewerAdd = new JMapViewer();

        new Thread(() -> {
            loader = new FXMLLoader(Objects.requireNonNull(GestorPantallas.class.getResource("propiedadUD.fxml")));
            try {
                nodo = loader.load();
            } catch (IOException e) {
                System.err.println("Error al cargar la pantalla de la propiedad: " + e.getMessage());
            }
        }).start();


        System.out.println("Inicializando controlador principal");
        progressBarAll.setVisible(true);
        new Thread(this::obtenerDatosBD).start();
        new Thread(this::cargarDirecciones).start();
        new Thread(() -> {
            cargarMapa();
            cargarMapaAdd();
            System.out.println("Mapa cargado " + System.currentTimeMillis());
        }).start();

        Platform.runLater(() -> {
            progressBarAll.setVisible(false);
            System.out.println("Buscador de propiedades con: " + allHouses.size() + " propiedades");
            ubicacionField.textProperty().addListener((observable, oldValue, newValue) -> {
                buscadorPropiedades();
            });
            hostField.textProperty().addListener((observable, oldValue, newValue) -> {
                buscadorPropiedades();
            });
        });


        errorMaxFotLabel.setVisible(false);
        errorMaxFotLabel.setText("Has seleccionado más de 10 fotos. El número máximo de fotos permitidas es 10.");
        errorMaxFotLabel.setStyle("-fx-text-fill: red");
        agregarAddHboxFotos();
        allHousesListscroll.setVisible(true);
        userHousesListscroll.setVisible(false);
        panelAgregarPropiedad.setVisible(false);
        contenedorChat.setVisible(false);

        messageField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                enviarMensaje();
            }
        });



        if (usuario.getUsuario().equals("invitado")) {
            addHouseButton.setDisable(true);
            houseAddress.setDisable(true);
            houseDescription.setDisable(true);
            userHousesList.setDisable(true);
            houseTipo.setDisable(true);
            labelnombreUsuario.setText(System.getenv("COMPUTERNAME"));
            Label recordatorio = new Label("Inicie sesión para ver sus propiedades, o regístrese para añadir propiedades.");
            HBox.setHgrow(recordatorio, Priority.ALWAYS);
            recordatorio.setAlignment(Pos.CENTER_LEFT);

            topleft.getChildren().add(1, recordatorio);
            panelAgregarPropiedad.setDisable(true);
            File directorio = new File("./src/main/resources/ProyectoFinal/imgs/");
            if (!directorio.exists()) {
                directorio.mkdirs();
            }
            File archivo = new File("./src/main/resources/ProyectoFinal/imgs/loginOut.png");
            configLoginImg.setImage(new Image(archivo.toURI().toString()));
            settingsHbox.setOnMouseClicked(event -> GestorPantallas.cerrarSesion(false));
        } else {
            settingsHbox.setOnMouseClicked(event -> settingsShow());
            labelnombreUsuario.setText(usuario.getNombre());
            for (int i = 0; i < usuario.getContactos().size(); i++) {
                listaContactos.getItems().add(usuario.getContactos().get(i));
                contactos.add(usuario.getContactos().get(i));
            }
            Local.setUsuario(usuario);
            modificarCeldasContacto();

            try {

                if(Local.leerLineaEspecifica(3) == null) {
                    Local.escribirLineaEspecifica(3, "Pantalla maximizada: " + false + " en pantalla " + 1 , Local.getFileData());
                }
                int opcion = Integer.parseInt(Local.leerLineaEspecifica(3).split(" ")[5]);

                if (opcion != 1){
                    labelnombreUsuario.requestFocus();
                    mostrarPropiedadesBt.setStyle("");
                }

                switch (opcion) {
                    case 1:
                        mostrarPropiedades();
                        break;
                    case 2:
                        mostrarMisPropiedades();
                        break;
                    case 3:
                        mostrarInsertarPropiedad();
                        break;
                    case 4:
                        mostrarMapa();
                        break;
                    case 5:
                        mostrarChat();
                        break;
                    default:
                        System.out.println("Opción no válida");
                        mostrarPropiedades();
                }
            }catch (NullPointerException e){
                System.err.println("Error al leer la opción de la pantalla: " + e.getMessage());
            }
        }


        prefijoCodPost.setDisable(true);
        new Thread(() -> {
            System.out.println("Ajustando ancho de las listas");
            allHousesList.setCellFactory(new PropiedadesCeldas(MAPA, this, false));
            userHousesList.setCellFactory(new PropiedadesCeldas(MODIFICAR, this, true));

        }).start();


        // Asegúrate de que este código se ejecuta después de que FXMLLoader ha cargado tu FXML
        GestorPantallas.getPrimaryStage().setOnShown(windowEvent -> {
            new Thread(() -> {
                while (!datosObtenidos || !direccionesCargadas) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.err.println("Error al dormir el hilo: " + e.getMessage());
                    }
                }
                HiloReceptorDatos.setPrincipal(this);
                hiloReceptorDatos = new HiloReceptorDatos();
                System.out.println("Hilo de recepción de datos iniciado " + System.currentTimeMillis());
                setupBuscarContactoField();
                setupAgregarContactoField();
                setupEliminarContactoField();
            }).start();
        });







        System.out.println("Controlador principal inicializado " + System.currentTimeMillis());
    }
    private void cargarMapaAdd() {
        // Crear el JMapViewer
        // Configurar el JMapViewer
        mapViewerAdd.setZoom(9);
        mapViewerAdd.setZoomControlsVisible(true);

        // Crear un SwingNode y agregar el JMapViewer a él
        SwingUtilities.invokeLater(() -> mapaSwingNodeAdd.setContent(mapViewerAdd));
        mapViewerAdd.setVisible(false);
    }
    private void cargarMapa() {
        // Crear el JMapViewer
        // Configurar el JMapViewer
        mapViewer.setZoom(9);
        mapViewer.setDisplayPosition(new Coordinate(40.41831, -3.70256), 6);
        mapViewer.setZoomControlsVisible(true);

        // Crear un SwingNode y agregar el JMapViewer a él
        SwingUtilities.invokeLater(() -> mapaSwingNode.setContent(mapViewer));
        mapViewer.setVisible(false);
    }

    private void ponerCoordenadas(Propiedad propiedad) {
        Coordinate coordenadas = new Coordinate(propiedad.getLatitud(), propiedad.getLongitud());
        MapMarkerDot marker = new MapMarkerDot(coordenadas) {

            @Override
            public void paint(Graphics g, Point position, int radio) {
                int size = radio * 5;
                int x = position.x - size / 2; // Ajuste para centrar la imagen horizontalmente
                int y = position.y - size / 2; // Ajuste para centrar la imagen verticalmente
                BufferedImage img = null;
                try {
                    img = ImageIO.read(new File("./src/main/resources/ProyectoFinal/imgs/pingHouse.png"));
                } catch (IOException e) {
                    System.err.println("Error al cargar la imagen del marcador de las propiedades: " + e.getMessage());
                }
                if (img != null) {
                    if (g instanceof Graphics2D g2) {
                        Composite oldComposite = g2.getComposite();
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                        Color backColor = this.getBackColor();
                        if (backColor == null) {
                            backColor = Color.WHITE; // Establecer un color de fondo predeterminado
                        }
                        g2.setPaint(backColor);
                        g.drawImage(img, x, y, size, size, null);
                        g2.setComposite(oldComposite);
                    }
                    if (this.getLayer() == null || this.getLayer().isVisibleTexts()) {
                        paintText(g, position);
                    }
                }
            }
        };

        mapViewer.addMapMarker(marker);
    }


    private void cargarDirecciones(){
        TextoAutoCompletado textoAutoCompletado = new TextoAutoCompletado(houseAddressProvincia, prefijoCodPost, codPost, localidadField, comunidadesField, mapViewerAdd);
        ubicaciones = textoAutoCompletado.getLOCALIDADES();
        direccionesCargadas = true;
        System.out.println("Direcciones cargadas");
    }

    private void agregarAddHboxFotos(){
        Image image = new Image(Objects.requireNonNull(getClass().getResource("/ProyectoFinal/imgs/add.png")).toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(SIZEFOTOS);
        imageView.setFitWidth(SIZEFOTOS);

        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> agregarFotos());
        fotosHBox.getChildren().add(imageView);
    }

    private void obtenerDatosBD() {
        System.out.println("Obteniendo datos de la BD");

        for (Propiedad propiedad : obtenerPropiedades()) {
            if (propiedad.getPropietario().getUsuario().equals(usuario.getUsuario())) {
                userHouses.add(propiedad);
                userHousesList.setItems(userHouses);
            } else {
                allHouses.add(propiedad);
                allHousesList.setItems(allHouses);
            }
            ponerCoordenadas(propiedad);
        }

        for (Propiedad propiedad : allHousesList.getItems()) {
            allHousesClon.add(propiedad.clone());
        }

        usuariosOnline = BufferesUser.getUsuariosOnline();
        System.out.println("Usuarios online obtenidos: " + usuariosOnline.size());

        datosObtenidos = true;
    }

    public void mostarMapaImg(Propiedad propiedad){
        if (propiedad != null){
            mostrarMapa();
            mapViewer.setDisplayPosition(new Coordinate(propiedad.getLatitud(), propiedad.getLongitud()), mapViewer.getZoom());
        }
    }

    private ArrayList<Propiedad> obtenerPropiedades() {
        return BufferesUser.getPropiedades(usuario);
    }

    public void resetBorder(KeyEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof TextField textField) {
            textField.setStyle("-fx-border-color: none");
        } else if (mouseEvent.getSource() instanceof TextArea textArea) {
            textArea.setStyle("-fx-border-color: none");
        }
    }


    private boolean checkCampos(){
        boolean isValid = true;
        if (houseName.getText().isEmpty() || houseName.getText().length() > 30){
            houseName.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            houseName.setStyle("-fx-border-color: none");
        }
        if (houseAddress.getText().isEmpty() || houseAddress.getText().length() > 50){
            houseAddress.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            houseAddress.setStyle("-fx-border-color: none");
        }
        if (houseDescription.getText().isEmpty() || houseDescription.getText().length() > 200){
            houseDescription.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            houseDescription.setStyle("-fx-border-color: none");
        }
        if (houseTipo.getText().isEmpty() || houseTipo.getText().length() > 30){
            houseTipo.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            houseTipo.setStyle("-fx-border-color: none");
        }
        if (houseAddressProvincia.getText().isEmpty() || houseAddressProvincia.getText().length() > 30){
            houseAddressProvincia.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            houseAddressProvincia.setStyle("-fx-border-color: none");
        }

        if(codPost.getText().isEmpty() || codPost.getText().length() != 3){
            codPost.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            codPost.setStyle("-fx-border-color: none");
        }

        if(localidadField.getText().isEmpty() || localidadField.getText().length() > 30){
            localidadField.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            localidadField.setStyle("-fx-border-color: none");
        }

        if(comunidadesField.getText().isEmpty() || comunidadesField.getText().length() > 30 ){
            comunidadesField.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            comunidadesField.setStyle("-fx-border-color: none");
        }

        if(pedaniaField.getText().isEmpty() || pedaniaField.getText().length() > 30 )   {
            pedaniaField.setStyle("-fx-border-color: red");
            isValid = false;
        } else {
            pedaniaField.setStyle("-fx-border-color: none");
        }

        return isValid;
    }

    public void addPropiedad() {
        if (!checkCampos()){
            return;
        }
        Set<Integer> existingIds = new HashSet<>();
        for (Propiedad propiedad : userHousesList.getItems()) {
            existingIds.add(propiedad.getId());
        }
        for (Propiedad propiedad : allHousesList.getItems()) {
            existingIds.add(propiedad.getId());
        }

        int id = 1;
        while (existingIds.contains(id)) {
            id++;
        }
        String nombre = houseName.getText();
        String comunidad = comunidadesField.getText();
        String provincia = houseAddressProvincia.getText();
        String localidad = localidadField.getText();
        String pedania = pedaniaField.getText();
        float latitud = (float) ubicaciones.getLatitud(localidad);
        float longitud = (float) ubicaciones.getLongitud(localidad);
        float altitud = (float) ubicaciones.getAltitud(localidad);
        float precioHora = 0;
        int codPostal = Integer.parseInt(prefijoCodPost.getText() + codPost.getText());
        String direccion = houseAddress.getText();
        String descripcion = houseDescription.getText();
        String tipo = houseTipo.getText();
        ArrayList<FotosPropiedad> fotosPropiedad = new ArrayList<>();
        int idFoto = 1;
        for (Node node : fotosHBox.getChildren()) {
            if (node instanceof ImageView imageView) {
                String url = imageView.getImage().getUrl();
                File file = new File(url);
                String fileName = file.getName();
                if (fileName.equals("add.png")) {
                    continue;
                }
                int indicePunto = url.lastIndexOf(".");
                String formato = indicePunto == -1 ? "" : url.substring(indicePunto + 1);
                FotosPropiedad foto = new FotosPropiedad(idFoto, imageToBytes(imageView, formato), formato, id);
                fotosPropiedad.add(foto);

                idFoto++;
            }
        }
        Propiedad propiedad = new Propiedad(id, nombre, tipo, comunidad, provincia, localidad, pedania ,direccion, latitud, longitud, altitud, codPostal ,precioHora, usuario, fotosPropiedad,descripcion);
        ponerCoordenadas(propiedad);
        userHouses.add(propiedad);
        userHousesList.setItems(userHouses);
        enviarPropiedad(propiedad);
        //BufferesUser.addPropiedad(propiedad); Guardar la propiedad en la base de datos
        vaciarCamposAddPropiedad();
    }

    public void vaciarCamposAddPropiedad(){
        houseName.clear();
        houseAddress.clear();
        houseDescription.clear();
        houseTipo.clear();
        pedaniaField.clear();
        houseAddressProvincia.clear();
        prefijoCodPost.clear();
        codPost.clear();
        localidadField.clear();
        comunidadesField.clear();
        Iterator<Node> iterator = fotosHBox.getChildren().iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node instanceof ImageView imageView) {
                String url = imageView.getImage().getUrl();
                File file = new File(url);
                String fileName = file.getName();
                if (!fileName.equals("add.png")) {
                    iterator.remove();
                }
            }
        }
    }

    public void mostrarPropiedades() {
        GestorPantallas.setNumeroPantalla(1);
        mostrarPropiedadesBt.setStyle("-fx-background-color: #318B86");
        mostrarMisPropiedadesBt.setStyle("");
        mostrarInsertarPropiedadBt.setStyle("");
        mostrarMapaBt.setStyle("");
        mostrarChatBt.setStyle("");

        panelAgregarPropiedad.setVisible(false);
        allHousesListscroll.setVisible(true);
        allHousesListAnchorPane.setVisible(true);
        userHousesListscroll.setVisible(false);
        userHousesListAnchorPane.setVisible(false);
        mapViewer.setVisible(false);
        contenedorMapaAdd.setVisible(false);
        contenedorMapa.setVisible(false);
        contenedorChat.setVisible(false);
    }
    public void mostrarMisPropiedades() {
        GestorPantallas.setNumeroPantalla(2);
        mostrarMisPropiedadesBt.setStyle("-fx-background-color: #318B86");
        mostrarPropiedadesBt.setStyle("");
        mostrarInsertarPropiedadBt.setStyle("");
        mostrarMapaBt.setStyle("");
        mostrarChatBt.setStyle("");

        panelAgregarPropiedad.setVisible(false);
        allHousesListscroll.setVisible(false);
        allHousesListAnchorPane.setVisible(false);
        userHousesListscroll.setVisible(true);
        userHousesListAnchorPane.setVisible(true);
        mapViewer.setVisible(false);
        mapViewerAdd.setVisible(false);
        contenedorMapaAdd.setVisible(false);
        contenedorMapa.setVisible(false);
        contenedorChat.setVisible(false);
    }
    public void mostrarInsertarPropiedad() {
        GestorPantallas.setNumeroPantalla(3);
        mostrarInsertarPropiedadBt.setStyle("-fx-background-color: #309791");
        mostrarPropiedadesBt.setStyle("");
        mostrarMisPropiedadesBt.setStyle("");
        mostrarMapaBt.setStyle("");
        mostrarChatBt.setStyle("");

        panelAgregarPropiedad.setVisible(true);
        allHousesListscroll.setVisible(false);
        allHousesListAnchorPane.setVisible(false);
        userHousesListscroll.setVisible(false);
        userHousesListAnchorPane.setVisible(false);
        mapViewer.setVisible(false);
        mapViewerAdd.setVisible(true);
        contenedorMapa.setVisible(false);
        contenedorMapaAdd.setVisible(true);
        contenedorChat.setVisible(false);
    }
    public void mostrarMapa() {
        GestorPantallas.setNumeroPantalla(4);
        mostrarMapaBt.setStyle("-fx-background-color: #309791");
        mostrarPropiedadesBt.setStyle("");
        mostrarMisPropiedadesBt.setStyle("");
        mostrarInsertarPropiedadBt.setStyle("");
        mostrarChatBt.setStyle("");

        panelAgregarPropiedad.setVisible(false);
        allHousesListscroll.setVisible(false);
        allHousesListAnchorPane.setVisible(false);
        userHousesListscroll.setVisible(false);
        userHousesListAnchorPane.setVisible(false);
        mapViewer.setVisible(true);
        mapViewerAdd.setVisible(false);
        contenedorMapa.setVisible(true);
        contenedorMapaAdd.setVisible(false);
        contenedorChat.setVisible(false);
    }
    public void mostrarChat() {
        GestorPantallas.setNumeroPantalla(5);

        mostrarChatBt.setStyle("-fx-background-color: #318B86");
        mostrarPropiedadesBt.setStyle("");
        mostrarMisPropiedadesBt.setStyle("");
        mostrarInsertarPropiedadBt.setStyle("");
        mostrarMapaBt.setStyle("");

        panelAgregarPropiedad.setVisible(false);
        allHousesListscroll.setVisible(false);
        allHousesListAnchorPane.setVisible(false);
        userHousesListscroll.setVisible(false);
        userHousesListAnchorPane.setVisible(false);
        mapViewer.setVisible(false);
        mapViewerAdd.setVisible(false);
        contenedorMapa.setVisible(false);
        contenedorMapaAdd.setVisible(false);
        contenedorChat.setVisible(true);
    }


    public void actualizarPropiedades(ArrayList<Propiedad> listaPropiedades) {
        Platform.runLater(() -> {
            allHouses.clear();
            userHouses.clear();
            allHousesList.getItems().clear();
            userHousesList.getItems().clear();
            ArrayList<Propiedad> usuarioHouses = new ArrayList<>();
            mapViewer.removeAllMapMarkers();
            for (Propiedad propiedad : listaPropiedades) {
                ponerCoordenadas(propiedad);
                if (propiedad.getPropietario().getUsuario().equals(usuario.getUsuario())) {
                    userHouses.add(propiedad);
                    usuarioHouses.add(propiedad);
                } else {
                    allHouses.add(propiedad);
                }
            }
            allHousesList.setItems(allHouses);
            userHousesList.setItems(userHouses);
            usuario.setPropiedades(usuarioHouses);
        });
    }
    private void agregarFotos(){
        errorMaxFotLabel.setVisible(false);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar fotos");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if ((files != null && files.size() <= 10) ) {
            for (File file : files) {
                if (file.length() <= 8388608 && fotosHBox.getChildren().size() <= 11) {
                    System.out.println("Añadiendo foto: " + file.getName());
                    ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                    imageView.setFitHeight(SIZEFOTOS);
                    imageView.setFitWidth(SIZEFOTOS);
                    imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> imageView.setOpacity(0.7));
                    imageView.addEventHandler(MouseEvent.MOUSE_EXITED, event -> imageView.setOpacity(1.0));
                    imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> fotosHBox.getChildren().remove(imageView));
                    fotosHBox.getChildren().add(imageView);

                } else {
                    System.err.println("La foto " + file.getName() + " es demasiado grande. El tamaño máximo permitido es 16MB.");
                }
            }
        } else if (files != null) {
            errorMaxFotLabel.setVisible(true);
            System.err.println("Has seleccionado más de 10 fotos. El número máximo de fotos permitidas es 10.");
        }
    }

    private byte[] imageToBytes(ImageView imageView, String formato) {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(imageView.getImage(), null);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, formato, byteOutput);
            System.out.println("Imagen convertida a bytes");
            System.out.println("Tamaño de la imagen: " + byteOutput.size());
            return byteOutput.toByteArray();
        } catch (IOException e) {
            System.err.println("Error al convertir la imagen a bytes: " + e.getMessage());
        }
        return null;
    }

    private void enviarPropiedad(Propiedad propiedad){
        propiedad.setAccionServer("insertarPropiedad");
        try {
            BufferesUser.getObjectOutputStream().writeObject(propiedad);
            BufferesUser.getObjectOutputStream().flush();
        } catch (IOException e) {
            System.err.println("Error al enviar la propiedad al servidor: " + e.getMessage());
        }
    }

    @FXML
    public void mouseEntered(){
        allHousesList.getScene().setCursor(Cursor.HAND);
    }
    @FXML
    public void mouseExited(){
        allHousesList.getScene().setCursor(Cursor.DEFAULT);
    }

    public void settingsShow() {
        if (usuario.getUsuario().equals("invitado")){
            GestorPantallas.mostrarPantalla(1, true);
        }else{
            GestorPantallas.mostrarSettings();
        }
    }
    public void systemExit(){
        try {
            hiloReceptorDatos.interrupt();
        }catch (NullPointerException e){
            System.err.println("Error al interrumpir el hilo de recepción de datos: " + e.getMessage());
        }
        GestorPantallas.systemExit();
    }
    public void maximizar(){
        Stage primaryStage = (Stage) allHousesList.getScene().getWindow();

        if (GestorPantallas.isMaximized()){
            primaryStage.setWidth(screen[0]);
            primaryStage.setHeight(screen[1]);
            primaryStage.setX(screen[2]);
            primaryStage.setY(screen[3]);
            GestorPantallas.setMaximized(false);
            ResizeListener.setMaximized(false);
        }else{
            GestorPantallas.setMaximized(true);
            ResizeListener.setMaximized(true);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            if (screenBounds.getWidth() != primaryStage.getWidth() && screenBounds.getHeight() != primaryStage.getHeight()) {
                screen = new double[]{primaryStage.getWidth(), primaryStage.getHeight(), primaryStage.getX(), primaryStage.getY()};
            }

            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());



        }
    }
    public void minimizar(){
        Stage primaryStage = (Stage) allHousesList.getScene().getWindow();
        primaryStage.setIconified(true);
    }

    private void modificarCeldasContacto(){
        listaContactos.setCellFactory(param -> new ListCell<>() {
            boolean localChatCargado = false;
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setOnMouseClicked(event -> {
                        if (!localChatCargado){
                            Local.leerChat(item);
                            localChatCargado = true;
                        }
                        usuarioEnChat = item;
                        chatEnterovbox.setVisible(true);
                        chatContactName.setText(item.getNombre());
                        conectado(item);
                        mostrarChat(item);

                    });
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProyectoFinal/Cliente/Celdas/celdaContacto.fxml"));
                        Node node = loader.load();
                        ContactosCeldas controller = loader.getController();
                        controller.setInfo(item);
                        setGraphic(node);
                    } catch (IOException e) {
                        setText("Error al cargar la celda de contacto");
                        System.err.println("Error al cargar la celda de contacto: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void conectado(Usuario usuarioAContectar){
        new Thread(() -> {
            for (Usuario usuario : usuariosOnline) {
                if (usuario.getUsuario().equals(usuarioAContectar.getUsuario())) {
                    Platform.runLater(() -> labelOnlineChat.setText("Conectado"));
                    break;
                }else{
                    Platform.runLater(() -> labelOnlineChat.setText("Desconectado"));
                }
            }
        }).start();
    }



    public void mostrarChat(Usuario user) {
        if (userLastChat == null) {
            chatMessages.setVisible(true);
        }
        if (!user.equals(userLastChat)) {
            chatActualVbox.setSpacing(10);
            userLastChat = user;
            chatActualVbox.getChildren().clear();
            System.out.println("Mostrando chat con: " + user.getUsuario());
            TreeSetMsg chatUser = TreeSetMsg.getInstance().getChatUser(user);
            System.out.println("Mensajes: " + chatUser.size());
            for (Mensaje mensaje : chatUser) {
                Label messageLabel = new Label(mensaje.getMensaje());
                HBox hBox = new HBox(messageLabel);
                if (mensaje.getUsuarioEmisor().getUsuario().equals(Principal.usuario.getUsuario())) {
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    messageLabel.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 5px; -fx-border-radius: 5px; -fx-background-radius: 5px");
                    chatActualVbox.getChildren().add(hBox);
                } else {
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    messageLabel.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 5px; -fx-border-radius: 5px; -fx-background-radius: 5px");
                    chatActualVbox.getChildren().add(hBox);
                }
            }
            bajarBarra();
        }
    }


    public void mostrarMensaje(String msg, Usuario usuario) {
        Platform.runLater(() -> {
            if (!msg.isEmpty()) {
                Label label = new Label(msg);
                label.getStyleClass().add("labelChat");
                label.setSnapToPixel(true);
                HBox hBox = new HBox(label);
                if (usuario == null) {
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 5px; -fx-border-radius: 5px; -fx-background-radius: 5px");
                } else if (usuario.getUsuario().equals(Principal.usuario.getUsuario())) {
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    label.setStyle("-fx-background-color: #DCF8C6; -fx-padding: 5px; -fx-border-radius: 5px; -fx-background-radius: 5px");
                    messageField.clear();
                    TreeSetMsg.getInstance().addMensaje(new Mensaje(Principal.getUsuario(), msg, usuario, LocalDateTime.now()));
                    chatActualVbox.getChildren().add(hBox);
                    bajarBarra();
                } else {
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    label.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 5px; -fx-border-radius: 5px; -fx-background-radius: 5px");
                    TreeSetMsg.getInstance().addMensaje(new Mensaje(usuario, msg, Principal.getUsuario(), LocalDateTime.now()));
                    if ((!(userLastChat == null)) && usuario.getUsuario().equals(userLastChat.getUsuario())) {
                        System.out.println("Mostrando mensaje: " + usuario.getUsuario());
                        chatActualVbox.getChildren().add(hBox);
                        bajarBarra();
                    }
                }
            }
        });
    }


    public void enviarMensaje(){
        String mensaje = messageField.getText();
        if (!mensaje.isEmpty()){
            mostrarMensaje(mensaje, usuario);
            Mensaje mensajeObj = new Mensaje(usuario, mensaje , userLastChat, LocalDateTime.now());
            TreeSetMsg.getInstance().addMensaje(mensajeObj);

            System.out.println("Enviando mensaje: " + usuario.toString());
            try {
                BufferesUser.getObjectOutputStream().writeObject(mensajeObj);
                BufferesUser.getObjectOutputStream().flush();
            } catch (IOException e) {
                System.err.println("Error al enviar el mensaje: " + e.getMessage());
            }
        }
    }

    public void bajarBarra() {
        // Simular un desplazamiento hacia abajo en la barra de desplazamiento
        chatMessages.setVvalue(1.0);
        // Retraso breve para simular un desplazamiento sutil
        PauseTransition pause = new PauseTransition(Duration.millis(50)); // Ajusta la duración según sea necesario
        pause.setOnFinished(event -> {
            // Restaurar el valor de desplazamiento
            chatMessages.setVvalue(1.0);
        });
        pause.play();
    }

    public void buscadorPropiedades() {
        String ubicacion = ubicacionField.getText().toLowerCase(); // Convertir el texto a minúsculas para la comparación
        String propietario = hostField.getText().toLowerCase(); // Convertir el texto a minúsculas para la comparación

        // Limpiar la lista visual antes de agregar nuevas propiedades
        allHousesList.getItems().clear();

        List<Propiedad> filteredProperties = allHousesClon.stream()
                .filter(propiedad -> {
                    boolean matchesUbicacion = ubicacion.isEmpty() || propiedad.getUbicacion().toLowerCase().contains(ubicacion);
                    boolean matchesPropietario = propietario.isEmpty() ||
                            propiedad.getPropietario().getUsuario().toLowerCase().contains(propietario) ||
                            propiedad.getPropietario().getNombre().toLowerCase().contains(propietario);
                    return matchesUbicacion && matchesPropietario;
                })
                .toList();

        allHousesList.getItems().addAll(filteredProperties);
    }



    public void mostrarActualizarPropiedades(Propiedad propiedad){
        try {
            if (propiedadAntigua == null){
                propiedadAntigua = propiedad;
                esnull = true;
            }

            if (propiedadAntigua.equals(propiedad) && !esnull){
                hboxMisPropiedades.getChildren().remove(nodo);
                propiedadAntigua = propiedad;
                esnull = true;
            }else {
                // Cargar el archivo FXML
                // Obtener el controlador del archivo FXML
                hboxMisPropiedades.getChildren().remove(nodo);
                propiedadAntigua = propiedad;
                PropiedadUD propiedadUD = loader.getController();
                propiedadUD.setHouseDescription(propiedad.getDescripcion());
                propiedadUD.setHouseName(propiedad.getNombre());
                propiedadUD.setHouseTipo(propiedad.getTipo());
                propiedadUD.setPropiedad(propiedad);
                propiedadUD.setPrecioHora(propiedad.getPrecioHora());
                propiedadUD.setPrincipal(this);
                propiedadUD.setFotosHBox(propiedad.getFotos());
                hboxMisPropiedades.getChildren().add(nodo);
                esnull = false;
            }

        } catch (Exception e) {
            System.err.println("Error al cargar la pantalla de la propiedad: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void ocultarActualizarPropiedades(){
        hboxMisPropiedades.getChildren().remove(nodo);
    }




    public void actualizarResenas(ArrayList<ResenaPropiedad> lista) {
        int numThreads = Math.min(lista.size(), Runtime.getRuntime().availableProcessors());
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        allHousesList.getItems().forEach(propiedad -> propiedad.getResenas().clear());
        userHousesList.getItems().forEach(propiedad -> propiedad.getResenas().clear());
        for (ResenaPropiedad resena : lista) {
            executorService.submit(() -> {
                for (Propiedad propiedad : allHousesList.getItems()) {
                    if (propiedad.getId() == resena.getIdPropiedad()) {
                        synchronized (propiedad) {
                            propiedad.addResena(resena);
                        }
                    }
                }
                for (Propiedad propiedad : userHousesList.getItems()) {
                    if (propiedad.getId() == resena.getIdPropiedad()) {
                        synchronized (propiedad) {
                            propiedad.addResena(resena);
                        }
                    }
                }
            });
        }
        executorService.shutdown();
    }


    public void actualizarPropiedadEnLista(Propiedad propiedadActualizada, List<FotosPropiedad> fotosPropiedad) {
        // Actualiza la caché de imágenes
        for (FotosPropiedad foto : fotosPropiedad) {
            Image imagen = new Image(new ByteArrayInputStream(foto.getImagenBytes()));
            String propiedadId = String.valueOf(propiedadActualizada.getId());
            imageCache.put(propiedadId, imagen);
        }

        // Fuerza la actualización de la lista de propiedades
        Platform.runLater(() -> {
            // Aquí debes actualizar la ListView donde se muestran las propiedades
            // Suponiendo que la ListView se llama listaPropiedades
            allHousesList.refresh();
            userHousesList.refresh();
        });
    }

    public Map<String, Image> getImageCache() {
        return imageCache;
    }

    public static double[] getScreen() {
        return screen;
    }
    public static void setScreen(double[] screen) {
        Principal.screen = screen;
    }

    private void setupBuscarContactoField() {
        buscarContactoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                Platform.runLater(() -> listaContactos.setItems(contactos));
            } else {
                ObservableList<Usuario> filtered = contactos.filtered(usuario -> usuario.getUsuario().toLowerCase().contains(newValue.toLowerCase()));
                if (!filtered.isEmpty()) {
                    filteredContactos.setAll(filtered);
                    Platform.runLater(() -> listaContactos.setItems(filteredContactos));
                }
            }
        });
        // Initialize with all contacts
        filteredContactos.setAll(contactos);
    }

    private void setupAgregarContactoField() {
        agregarContactoField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String newContactId = agregarContactoField.getText();
                if (newContactId != null && !newContactId.trim().isEmpty()) {
                    Usuario newUsuario = new Usuario();
                    newUsuario.setUsuario(newContactId.trim());
                    if (!contactos.contains(newUsuario)) {
                        agregarContactoField.clear();
                        buscarContactoField.clear();
                        BufferesUser.actualizarContactos(newUsuario);
                        int numContactos = contactos.size();
                        new Thread(() -> {
                            try {
                                while (!actualizacionContactos){
                                    Thread.sleep(1000);
                                }
                                System.out.println("Actualizando contactos");
                                if (numContactos == Principal.getUsuario().getContactos().size()) {
                                    agregarContactoField.setStyle("-fx-border-color: red");
                                }else{
                                    agregarContactoField.setStyle("-fx-border-color: none");
                                    for (Usuario usuario1 : Principal.getUsuario().getContactos()) {
                                        if (usuario1.getUsuario().equals(newContactId)) {
                                            Platform.runLater(() -> {
                                                contactos.add(usuario1);
                                                filteredContactos.setAll(contactos);
                                                listaContactos.setItems(filteredContactos);
                                            });
                                            break;
                                        }
                                    }
                                }
                            } catch (InterruptedException e) {
                                System.err.println("Error al dormir el hilo: " + e.getMessage());
                            }
                        }).start();
                    }
                }
            }
        });
    }
    public static  void actualizacionContactos(){
        Principal.actualizacionContactos = true;
    }

    private void setupEliminarContactoField() {
        eliminarContactoField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String contactIdToRemove = eliminarContactoField.getText();
                if (contactIdToRemove != null && !contactIdToRemove.trim().isEmpty()) {
                    contactos.removeIf(usuario -> usuario.getUsuario().equals(contactIdToRemove.trim()));
                    eliminarContactoField.clear();
                    buscarContactoField.clear();
                    chatContactName.setText("");
                    labelOnlineChat.setText("");
                    chatEnterovbox.setVisible(false);
                    filteredContactos.setAll(contactos);
                    listaContactos.setItems(filteredContactos);
                    Usuario contactToRemove = new Usuario();
                    contactToRemove.setUsuario(contactIdToRemove);
                    BufferesUser.eliminarContacto(contactToRemove);
                }
            }
        });
    }
}