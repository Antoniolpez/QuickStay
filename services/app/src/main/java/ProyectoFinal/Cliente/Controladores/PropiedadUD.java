package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Comun.FotosPropiedad;
import ProyectoFinal.Comun.Propiedad;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PropiedadUD {
    public TextField houseName;
    public TextArea houseDescription;
    public TextField houseTipo;
    public HBox fotosHBox;
    public Button removeHouseButton;
    public Button updateButton;
    public TextField housePrecio;
    private Propiedad propiedad;
    private Principal principal;

    public void initialize() {
        removeHouseButton.setOnMouseClicked(event -> {
            houseName.clear();

            houseDescription.clear();
            houseTipo.clear();
            fotosHBox.getChildren().clear();
            housePrecio.clear();
            BufferesUser.eliminarPropiedad(propiedad);
            principal.ocultarActualizarPropiedades();
        });

        updateButton.setOnMouseClicked(event -> {
            Propiedad  propiedad = new Propiedad();
            propiedad.setId(this.propiedad.getId());
            propiedad.setNombre(houseName.getText());
            propiedad.setPropietario(Principal.getUsuario());
            propiedad.setDescripcion(houseDescription.getText());
            propiedad.setTipo(houseTipo.getText());
            propiedad.setAltitud(this.propiedad.getAltitud());
            propiedad.setLongitud(this.propiedad.getLongitud());
            propiedad.setLatitud(this.propiedad.getLatitud());
            propiedad.setDireccion(this.propiedad.getDireccion());
            propiedad.setComunidad(this.propiedad.getComunidad());
            propiedad.setProvincia(this.propiedad.getProvincia());
            propiedad.setLocalidad(this.propiedad.getLocalidad());
            propiedad.setPrecioHora(Float.parseFloat(housePrecio.getText()));
            ArrayList<FotosPropiedad> fotosPropiedad = new ArrayList<>();
            int idFoto = 1;
            for (Node node : fotosHBox.getChildren()) {
                if (node instanceof ImageView imageViews) {
                    String url = imageViews.getImage().getUrl();
                    if (url == null) {
                        try {
                            String formato = imageViews.getId().substring(imageViews.getId().lastIndexOf("."));
                            FotosPropiedad foto = new FotosPropiedad(idFoto, imageToBytes(imageViews, formato), formato, propiedad.getId());
                            fotosPropiedad.add(foto);
                            idFoto++;
                        } catch (Exception e) {
                            System.err.println("Error al convertir la imagen a bytes: " + e.getMessage());
                        }
                        continue;
                    }else{
                        File file = new File(url);
                        String fileName = file.getName();
                        if (fileName.equals("add.png")) {
                            continue;
                        }
                        int indicePunto = url.lastIndexOf(".");
                        String formato = indicePunto == -1 ? "" : url.substring(indicePunto + 1);
                        FotosPropiedad foto = new FotosPropiedad(idFoto, imageToBytes(imageViews, formato), formato, propiedad.getId());
                        fotosPropiedad.add(foto);
                        idFoto++;
                    }

                }
            }
            propiedad.setFotos(fotosPropiedad);
            BufferesUser.actualizarPropiedad(propiedad);

            // Actualiza la caché de imágenes y la lista de propiedades
            principal.actualizarPropiedadEnLista(propiedad, fotosPropiedad);
            principal.ocultarActualizarPropiedades();
        });
    }



    private void agregarImagenAdd() {
        URL imageUrl = getClass().getResource("/ProyectoFinal/imgs/add.png");
        if (imageUrl != null) {
            Image image = new Image(imageUrl.toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(100);
            imageView.setFitWidth(100);

            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> agregarFotos());
            Platform.runLater(() -> {
                // Agrega la imagen "add" al inicio del HBox
                fotosHBox.getChildren().add(0, imageView);
            });
        } else {
            System.out.println("La imagen no existe");
        }
    }



    public void setHouseName(String houseName) {
        this.houseName.setText(houseName);
    }

    public void setHouseDescription(String houseDescription) {
        this.houseDescription.setText(houseDescription);
    }

    public void setHouseTipo(String houseTipo) {
        this.houseTipo.setText(houseTipo);
    }

    public void setFotosHBox(ArrayList<FotosPropiedad> fotos) {
        try {
            Platform.runLater(() -> {
                fotosHBox.getChildren().clear();
                agregarImagenAdd();  // Primero agrega la imagen "add"

                for (FotosPropiedad foto : fotos) {
                    Image image = new Image(new ByteArrayInputStream(foto.getImagenBytes()));
                    ImageView imageView = new ImageView(image);
                    imageView.setId(foto.getId() + "_" + foto.getIdPropiedad() + "_" + foto.getFormato());
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(100);
                    imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> imageView.setOpacity(0.7));
                    imageView.addEventHandler(MouseEvent.MOUSE_EXITED, event -> imageView.setOpacity(1.0));
                    imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> fotosHBox.getChildren().remove(imageView));
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                    imageView.setCache(true);
                    fotosHBox.getChildren().add(imageView);
                }
            });
        } catch (Exception e) {
            System.out.println("No hay fotos para mostrar");
        }
    }


    public void setPropiedad(Propiedad propiedad) {
        this.propiedad = propiedad;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public void resetBorder(KeyEvent mouseEvent) {
        System.out.println("reset");
        if (mouseEvent.getSource() instanceof TextField) {
            TextField miTextField = (TextField) mouseEvent.getSource();
            miTextField.setStyle("-fx-border-color: none");
            // Aquí puedes trabajar con miTextField
        } else if (mouseEvent.getSource() instanceof TextArea) {
            TextArea miTextArea = (TextArea) mouseEvent.getSource();
            miTextArea.setStyle("-fx-border-color: none");
        }
    }

    private void agregarFotos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar fotos");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setSelectedExtensionFilter(fileChooser.getExtensionFilters().get(0));

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null && files.size() <= 10) {
            for (File file : files) {
                if (file.length() <= 8388608 && fotosHBox.getChildren().size() <= 11) {
                    System.out.println("Añadiendo foto: " + file.getName());
                    ImageView imageView = new ImageView(new Image(file.toURI().toString()));
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> imageView.setOpacity(0.7));
                    imageView.addEventHandler(MouseEvent.MOUSE_EXITED, event -> imageView.setOpacity(1.0));
                    imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> fotosHBox.getChildren().remove(imageView));
                    Platform.runLater(() -> {
                        // Asegura que la imagen "add" permanezca al inicio
                        fotosHBox.getChildren().add(fotosHBox.getChildren().size() - 1, imageView);
                    });
                } else {
                    System.err.println("La foto " + file.getName() + " es demasiado grande. El tamaño máximo permitido es 16MB.");
                }
            }
        } else if (files != null) {
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

    public void setPrecioHora(float precioHora) {
        housePrecio.setText(String.valueOf(precioHora));
    }
}
