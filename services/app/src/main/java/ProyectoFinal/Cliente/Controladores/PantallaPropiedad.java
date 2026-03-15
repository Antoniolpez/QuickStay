package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Cliente.GestorPantallas;
import ProyectoFinal.Cliente.Launcher;
import ProyectoFinal.Cliente.Librerias.ResizeListener;
import ProyectoFinal.Comun.Alquiler;
import ProyectoFinal.Comun.Propiedad;
import ProyectoFinal.Comun.ResenaPropiedad;
import ProyectoFinal.Comun.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador de la pantalla de propiedad.
 * Esta clase se encarga de gestionar la interacción del usuario con la pantalla de propiedad, para poder alquilar.
 */
public class PantallaPropiedad implements PropertyChangeListener {
    @FXML
    public Pane paneImg;
    @FXML
    public TextArea descripTextArea;
    @FXML
    public Label namePropLabel;
    @FXML
    public Label ubiPropLabel;
    @FXML
    public Label tipoPropLabel;
    @FXML
    public HBox contenedorImgVbox;
    @FXML
    public ImageView listaFotos;
    @FXML
    public VBox vboxCalendario;
    public TextField entradaField;
    public TextField salidaField;
    public TextField resenaField;
    @FXML
    public ComboBox<String> puntuaCombo;
    public VBox resenaVBox;
    public TextField numPersonasField;
    public Button alquilaboton;
    public ListView alquilaList;
    public ScrollPane scrollPane;
    private Propiedad propiedad;
    private boolean numPersonasOK = false;
    private Calendario calendarioController;
    private double[] screen = new double[4];
    private final List<Alquiler> alquileresFiltrados = new ArrayList<>();
    private int currentImageIndex = 0;


    /**
     * Método para inicializar la pantalla de propiedad.
     * Este método se encarga de configurar los elementos de la pantalla y de establecer los listeners necesarios.
     */
    public void initialize() {

        alquilaList.setVisible(false);
        scrollPane.setVisible(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        numPersonasField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Comprueba si la nueva entrada contiene solo dígitos
                numPersonasField.setText(newValue.replaceAll("[^\\d]", "")); // Elimina cualquier carácter que no sea un dígito
            }
            if (newValue.startsWith("0")) { // Comprueba si la nueva entrada comienza con 0
                numPersonasField.setStyle("-fx-border-color: red");
                numPersonasOK = false;
            } else {
                numPersonasField.setStyle("-fx-border-color: none");
                numPersonasOK = true;
            }
        });


        puntuaCombo.getItems().addAll("1 ★", "2 ★", "3 ★", "4 ★", "5 ★");
        try {
            FXMLLoader loader = new FXMLLoader();
            Parent calendario = loader.load(Objects.requireNonNull(GestorPantallas.class.getResource("calendario.fxml")).openStream());
            vboxCalendario.getChildren().add(calendario);

            // Obtener una referencia al controlador del calendario
            calendarioController = loader.getController();
            // Establecer los valores de entradaField y salidaField en el calendario
            calendarioController.setEntradaField(entradaField);

            calendarioController.setSalidaField(salidaField);

            puntuaCombo.setOnMouseClicked(event -> puntuaCombo.setStyle("-fx-border-color: none"));

            resenaField.setOnAction(event -> {
                new Thread(() -> {
                    String text = resenaField.getText();
                    int id = 0;
                    for (ResenaPropiedad resena : propiedad.getResenas()) {
                        if (resena.getId() == id) {
                            id++;
                        } else {
                            break;
                        }
                    }
                    Object selectedItem = puntuaCombo.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        String selectedItemString = selectedItem.toString();
                        if (selectedItemString.isEmpty()) {
                            puntuaCombo.setStyle("-fx-border-color: red");
                        } else {
                            ResenaPropiedad resena = new ResenaPropiedad(id, Principal.getUsuario().getUsuario(), propiedad.getId(), LocalDate.now(), Float.parseFloat(selectedItemString.substring(0,1)), text);
                            System.out.println(text);
                            resenaField.clear();
                            Platform.runLater(() -> puntuaCombo.getSelectionModel().clearSelection());
                            BufferesUser.addResena(resena);
                        }
                    } else {
                        puntuaCombo.setStyle("-fx-border-color: red");
                    }
                }).start();
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Método para establecer la propiedad que se va a mostrar en la pantalla.
     * @param propiedad La propiedad que se va a mostrar.
     */
    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
        propiedad.addPropertyChangeListener(this);
        namePropLabel.setText(propiedad.getNombre());
        descripTextArea.setText(propiedad.getDescripcion());
        ubiPropLabel.setText(propiedad.getUbicacion());
        tipoPropLabel.setText(propiedad.getTipo() + " de " + propiedad.getPropietario().getUsuario());

        actualizarResenas();
        //deshabilitarFechasOcupadas(calendarioController);

        try {
            cargarImagen(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No hay imagen");
        }
        // Deshabilitar fechas ocupadas
        deshabilitarFechasYHoras();
    }

    /**
     * Método para deshabilitar las fechas y horas que ya están ocupadas en el calendario.
     */
    private void deshabilitarFechasYHoras() {
        calendarioController.deshabilitarFechas(propiedad.getAlquileres());
    }


    /**
     * Método para actualizar las reseñas de la propiedad que se muestra en la pantalla.
     */
    private void actualizarResenas() {
    resenaVBox.getChildren().clear();
    List<ResenaPropiedad> resenas = new ArrayList<>(propiedad.getResenas());
    Collections.sort(resenas, Comparator.comparing(ResenaPropiedad::getFecha));
    for (ResenaPropiedad resena : resenas) {
        HBox resenaBox = new HBox();
        resenaBox.setSpacing(10);

        Label usuarioLabel = new Label(resena.getUsuario());
        Label fechaLabel = new Label(resena.getFecha().toString());
        Label estrellasLabel = new Label(String.valueOf(resena.getEstrellas())+" ★ ");
        Label comentarioLabel = new Label(resena.getComentario());
        if (resena.getUsuario().equals(Principal.getUsuario().getUsuario())) {
            Image image = new Image((Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/delete.png")))); // Asegúrate de que la ruta a la imagen es correcta
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(15); // Ajusta el tamaño de la imagen como desees
            imageView.setFitWidth(15);

            Button deleteButton = new Button();
            deleteButton.setGraphic(imageView);
            deleteButton.setOnAction(event -> {
                Iterator<ResenaPropiedad> iterator = propiedad.getResenas().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equals(resena)) {
                        iterator.remove();
                        break;
                    }
                }
                BufferesUser.eliminarResena(resena);
                Platform.runLater(this::actualizarResenas);
            });
            resenaBox.getChildren().addAll(usuarioLabel, fechaLabel, estrellasLabel, comentarioLabel, deleteButton);
        }else{
            resenaBox.getChildren().addAll(usuarioLabel, fechaLabel, estrellasLabel, comentarioLabel);
        }

        resenaVBox.getChildren().add(resenaBox);
    }
}

    /**
     * Método que se ejecuta cuando se produce un cambio en una propiedad.
     * @param evt El evento que desencadena este método.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("resenas".equals(evt.getPropertyName())) {
            Platform.runLater(this::actualizarResenas);
        }
    }

    /**
     * Método para cargar una imagen de la propiedad.
     * @param id El id de la imagen que se va a cargar.
     */
    public void cargarImagen(int id) {
        if (propiedad.getFotos() != null && !propiedad.getFotos().isEmpty()) {
            try {
                listaFotos.setImage(new Image(new ByteArrayInputStream(propiedad.getFotos().get(id).getImagenBytes())));
            } catch (Exception e) {
                System.out.println("No se pudo cargar la imagen");
            }
            // Configurar el ImageView
            //listaFotos.setImage(image);
            //listaFotos.setFitHeight(250);
            //listaFotos.setFitWidth(250);
        } else {
            System.out.println("No hay imagen");
        }
    }

    /**
     * Método para agregar el propietario de la propiedad a la lista de contactos del usuario.
     */
    public void agregarContacto() {
        ListView<Usuario> listaContactos = (ListView<Usuario>) GestorPantallas.getPantalla2().lookup("#listaContactos");
        for (int i = 0; i < listaContactos.getItems().size(); i++) {
            if (listaContactos.getItems().get(i).equals(propiedad.getPropietario())) {
                System.out.println("Ya existe el contacto");
                return;
            }
        }
        BufferesUser.actualizarContactos(propiedad.getPropietario());
        listaContactos.getItems().add(propiedad.getPropietario());
    }

    /**
     * Método para desactivar la pantalla de propiedad.
     */
    public void desactivarPantalla() {
        paneImg.setDisable(true);
        descripTextArea.setDisable(true);
        namePropLabel.setDisable(true);
        ubiPropLabel.setDisable(true);
        tipoPropLabel.setDisable(true);
        resenaField.setDisable(true);
        puntuaCombo.setDisable(true);
        resenaVBox.setDisable(true);
        numPersonasField.setDisable(true);
        entradaField.setDisable(true);
        salidaField.setDisable(true);
        alquilaboton.setDisable(true);
    }

    public void agregarAlquileres(){
        numPersonasField.setVisible(false);
        numPersonasField.setMaxWidth(0);
        numPersonasField.setMinWidth(0);
        entradaField.setVisible(false);
        entradaField.setMaxWidth(0);
        entradaField.setMinWidth(0);
        salidaField.setVisible(false);
        salidaField.setMaxWidth(0);
        salidaField.setMinWidth(0);
        alquilaboton.setVisible(false);

        alquilaList.setVisible(true);
        scrollPane.setVisible(true);

        alquilaList.getItems().clear();
        if (propiedad.getAlquileres() != null) {
            alquilaList.getItems().addAll(propiedad.getAlquileres());
        }
        if (propiedad.getAlquileres().isEmpty()){
            Label label = new Label("No hay alquileres");
            alquilaList.getItems().add(label);
        }
        calendarioController.setConfigMisPropiedades(this);

    }

    public void filtrarAlquileres() {
        LocalDate fechaSeleccionada = calendarioController.getFechaSeleccionada();
        String horaSeleccionada = calendarioController.getHoraSeleccionada();
        if (fechaSeleccionada != null && horaSeleccionada != null) {
            for (Alquiler alquiler : propiedad.getAlquileres()) {

                LocalDate fechaAlquiler = alquiler.getCheckin().toLocalDate();
                String horaAlquiler = alquiler.getCheckin().toLocalTime().toString().substring(0, 5); // Obtener la hora en formato HH:mm

                if (fechaAlquiler.equals(fechaSeleccionada) && horaAlquiler.equals(horaSeleccionada)) {
                    alquileresFiltrados.add(alquiler);
                }
            }
            alquilaList.getItems().clear();
            alquilaList.getItems().addAll(alquileresFiltrados);
        }
    }

    /**
     * Método para comprobar si las fechas de entrada y salida son válidas.
     * @return true si las fechas son válidas, false en caso contrario.
     */
    private boolean checkFechas() {
        if (entradaField.getText().isEmpty() || salidaField.getText().isEmpty()) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'a las' HH:mm");
        LocalDateTime entrada = LocalDateTime.parse(entradaField.getText(), formatter);
        LocalDateTime salida = LocalDateTime.parse(salidaField.getText(), formatter);
        return entrada.isBefore(salida);
    }

    /**
     * Método para mostrar la pantalla de facturación.
     */
    public void mostrarPantallas() {
        try {
            if (numPersonasOK && checkFechas()) {
                Factura.setPropiedad(propiedad);
                Factura.setCheckIn(entradaField.getText());
                Factura.setCheckOut(salidaField.getText());
                FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("facturacion.fxml"));
                Parent root = loader.load();
                Factura controller = loader.getController();
                controller.setEstadoPantallaPropiedad((Stage) alquilaboton.getScene().getWindow());
                controller.setNumPersonas(Integer.parseInt(numPersonasField.getText()));
                root.getStylesheets().add(Objects.requireNonNull(Launcher.class.getResource("facturacion.css")).toExternalForm());
                controller.setNombreCompleto(Principal.getUsuario().getNombre() + " " + Principal.getUsuario().getApellidos());
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Facturación");
                stage.getIcons().add(new Image(Objects.requireNonNull(GestorPantallas.class.getResourceAsStream("/ProyectoFinal/imgs/logo.png"))));

                stage.show();
            }else{
                if (!checkFechas()) {
                    entradaField.setStyle("-fx-border-color: red");
                    salidaField.setStyle("-fx-border-color: red");
                }else{
                    numPersonasField.setStyle("-fx-border-color: red");
                    entradaField.setStyle("-fx-border-color: none");
                    salidaField.setStyle("-fx-border-color: none");
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo cargar la pantalla de la propiedad" + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Método para cambiar el cursor a mano cuando el ratón entra en el panel de imágenes.
     */
    public void mouseEntered(){
        paneImg.getScene().setCursor(Cursor.HAND);
    }

    /**
     * Método para cambiar el cursor a predeterminado cuando el ratón sale del panel de imágenes.
     */
    @FXML
    public void mouseExited(){
        paneImg.getScene().setCursor(Cursor.DEFAULT);
    }

    /**
     * Método para cerrar la aplicación.
     */
    public void systemExit(){
        try {
            Principal.getHiloReceptorDatos().interrupt();
        }catch (NullPointerException e){
            System.err.println("Error al interrumpir el hilo de recepción de datos: " + e.getMessage());
        }
        Stage primaryStage = (Stage) alquilaboton.getScene().getWindow();
        primaryStage.close();
    }

    /**
     * Método para maximizar la ventana de la aplicación.
     */
    public void maximizar(){
        Stage primaryStage = (Stage) alquilaboton.getScene().getWindow();

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

    /**
     * Método para minimizar la ventana de la aplicación.
     */
    public void minimizar(){
        Stage primaryStage = (Stage) ubiPropLabel.getScene().getWindow();
        primaryStage.setIconified(true);
    }


    /**
     * Método para avanzar en el array de imágenes de la propiedad.
     */
    public void nextImage() {
        if (propiedad.getFotos() != null && !propiedad.getFotos().isEmpty()) {
            currentImageIndex++;
            if (currentImageIndex >= propiedad.getFotos().size()) {
                currentImageIndex = 0; // Si sobrepasamos el final de la lista, volvemos al principio
            }
            cargarImagen(currentImageIndex);
        }
    }

    /**
     * Método para retroceder en el array de imágenes de la propiedad.
     */
    public void previousImage() {
        if (propiedad.getFotos() != null && !propiedad.getFotos().isEmpty()) {
            currentImageIndex--;
            if (currentImageIndex < 0) {
                currentImageIndex = propiedad.getFotos().size() - 1; // Si sobrepasamos el inicio de la lista, vamos al final
            }
            cargarImagen(currentImageIndex);
        }
    }


}
