package ProyectoFinal.Cliente.Celdas;

import ProyectoFinal.Cliente.Controladores.PantallaPropiedad;
import ProyectoFinal.Cliente.Controladores.Principal;
import ProyectoFinal.Cliente.GestorPantallas;
import ProyectoFinal.Cliente.Librerias.Resize;
import ProyectoFinal.Comun.Propiedad;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase PropiedadesCeldas
 * Esta clase implementa un Callback para personalizar la forma en que se muestran las propiedades en una ListView.
 * Cada instancia de esta clase representa una celda en la lista de propiedades.
 */
public class PropiedadesCeldas implements Callback<ListView<Propiedad>, ListCell<Propiedad>> {


    /** Imagen que se muestra en la parte derecha de la celda. */
    private final Image iconoDerecha;
    /** Indica si la propiedad es del usuario actual. */
    private final boolean mia;
    /** Referencia al controlador principal de la aplicación. */
    private final Principal principal;
    /** Caché de imágenes para evitar la carga repetida de imágenes. */
    private final Map<String, Image> imageCache;
    /** Pool de hilos para cargar imágenes de forma asíncrona. */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3); // Crea un pool de hilos

    /**
     * Constructor de la clase PropiedadesCeldas
     * @param iconoDerecha Imagen que se muestra en la parte derecha de la celda.
     * @param principal Referencia al controlador principal de la aplicación.
     * @param mia Indica si la propiedad es del usuario actual.
     */
    public PropiedadesCeldas(Image iconoDerecha, Principal principal, boolean mia) {
        this.iconoDerecha = iconoDerecha;
        this.mia = mia;
        this.principal = principal;
        this.imageCache = principal.getImageCache(); // Obtén la referencia de la caché de imágenes desde Principal
    }

    /**
     * Método para cargar la imagen de la propiedad en la celda.
     * @param propiedad La propiedad cuya imagen se va a cargar.
     * @param foto El ImageView donde se va a cargar la imagen.
     */
    private void loadImage(Propiedad propiedad, ImageView foto) {
        String propiedadId = String.valueOf(propiedad.getId()); // Asumiendo que Propiedad tiene un método getId() para obtener un identificador único
        Image cachedImage = imageCache.get(propiedadId);

        if (cachedImage != null) {
            Platform.runLater(() -> foto.setImage(cachedImage));
        } else {
            executorService.submit(() -> {
                if (!propiedad.getFotos().isEmpty()) {
                    byte[] imagenBytes = propiedad.getFotos().get(0).getImagenBytes();
                    ByteArrayInputStream bis = new ByteArrayInputStream(imagenBytes);
                    Image imagen = new Image(bis);
                    imageCache.put(propiedadId, imagen); // Almacena la imagen en la caché
                    Platform.runLater(() -> foto.setImage(imagen)); // Actualiza la imagen en el hilo de la interfaz de usuario
                }
            });
        }
    }

    /**
     * Método que se llama para cada elemento de la ListView para crear una celda personalizada.
     * @param param La ListView que contiene las propiedades.
     * @return Una ListCell personalizada para mostrar la propiedad.
     */
    @Override
    public ListCell<Propiedad> call(ListView<Propiedad> param) {
        int espacioEntrElem = 8;
        int tamanoImagen = 100;

        return new ListCell<>() {
            final HBox hBox = new HBox(espacioEntrElem); // Espaciado entre elementos
            final HBox hboxImg = new HBox();
            final VBox vBox = new VBox();
            final ImageView foto = new ImageView();
            final Text nombre = new Text();
            final Text ubicacion = new Text();
            final ImageView iconoDerechaFinal = new ImageView();

            {
                // Configura los elementos aquí
                foto.setFitWidth(tamanoImagen);
                foto.setFitHeight(tamanoImagen);
                nombre.setStyle(
                        "-fx-font-size: 2.5em; " +
                                "-fx-font-family: 'Arial', sans-serif; " +
                                "-fx-text-alignment: center; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-fill: #333333;"
                );
                ubicacion.setStyle(
                        "-fx-font-size: 1.2em; " +
                                "-fx-font-family: 'Arial', sans-serif; " +
                                "-fx-text-alignment: center; " +
                                "-fx-font-weight: normal; " +
                                "-fx-text-fill: #666666;"
                );
                iconoDerechaFinal.setFitWidth(20);
                iconoDerechaFinal.setFitHeight(20);
                iconoDerechaFinal.setImage(iconoDerecha);

                hboxImg.setOnMouseEntered(event -> iconoDerechaFinal.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);"));

                hboxImg.setOnMouseExited(event -> iconoDerechaFinal.setStyle(""));

                hboxImg.setOnMousePressed(event -> iconoDerechaFinal.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px; -fx-effect: innershadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);"));

                hboxImg.setOnMouseReleased(event -> iconoDerechaFinal.setStyle("-fx-background-color: transparent; -fx-border-color: black; -fx-border-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 20, 0, 0, 0);"));

                vBox.getChildren().addAll(nombre, ubicacion); // Añade nombre y ubicacion al VBox

                StackPane stackPane = new StackPane();
                hboxImg.getChildren().add(iconoDerechaFinal);
                hboxImg.setAlignment(Pos.BOTTOM_RIGHT); // Alinea el HBox a la derecha
                hboxImg.setMaxWidth(iconoDerechaFinal.getFitWidth());
                hboxImg.setMaxHeight(iconoDerechaFinal.getFitHeight());
                hboxImg.setStyle("-fx-background-color: transparent;");

                stackPane.getChildren().addAll(vBox, hboxImg);
                StackPane.setAlignment(iconoDerechaFinal, Pos.BOTTOM_RIGHT); // Alinea el icono a la parte inferior derecha
                StackPane.setAlignment(hboxImg, Pos.BOTTOM_RIGHT);
                hBox.getChildren().addAll(foto, stackPane); // Añade el StackPane al HBox
                vBox.setPrefWidth(getPrefWidth() - tamanoImagen - espacioEntrElem);
                vBox.prefWidthProperty().bind(hBox.widthProperty());
                stackPane.setPrefWidth(param.getPrefWidth());
                hBox.setAlignment(Pos.CENTER_LEFT); // Alinea el HBox a la izquierda

                setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!isEmpty())) { // Doble clic para abrir la pantalla de la propiedad
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("pantallaPropiedad.fxml"));
                            Parent root = loader.load();
                            PantallaPropiedad controller = loader.getController();
                            if (mia || Principal.getUsuario().getUsuario().equals("invitado")) {
                                controller.desactivarPantalla();
                            }

                            controller.setPropiedad(getItem());
                            if (mia){
                                controller.agregarAlquileres();
                            }
                            Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Propiedad " + getItem().getNombre());
                            stage.getIcons().add(new Image(Objects.requireNonNull(GestorPantallas.class.getResourceAsStream("/ProyectoFinal/imgs/logo.png"))));
                            stage.initStyle(StageStyle.UNDECORATED);

                            Resize.addResizeListener(stage);

                            stage.show();
                        } catch (IOException e) {
                            System.err.println("No se pudo cargar la pantalla de la propiedad" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Propiedad propiedad, boolean empty) {
                super.updateItem(propiedad, empty);
                this.setPrefWidth(param.getPrefWidth() - tamanoImagen - espacioEntrElem);
                if (empty || propiedad == null) {
                    setText(null);
                    setGraphic(null);
                } else {

                    nombre.setText(getItem().getNombre());
                    ubicacion.setText(getItem().getComunidad() + " - " + getItem().getProvincia() + " - " + getItem().getLocalidad() + " - " + getItem().getDireccion());
                    loadImage(getItem(), foto);

                    hBox.setMaxWidth(param.getPrefWidth());
                    hBox.setPrefWidth(param.getPrefWidth());

                    hboxImg.setOnMouseClicked(event -> {
                        if (!mia) {
                            principal.mostarMapaImg(getItem());
                        } else {
                            principal.mostrarActualizarPropiedades(getItem());
                        }
                    });

                    setGraphic(hBox);
                }
            }
        };
    }
}
