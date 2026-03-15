package ProyectoFinal.Cliente.Librerias;

import ProyectoFinal.Cliente.Local;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class TextoAutoCompletado {
    private final SortedSet<String> entradasProvincias;
    private final SortedSet<String> entradasLocalidades;
    private final ContextMenu ventanaEmergente;
    private final TextField codPostalPrefijoField;
    private final TextField codPostalField;
    private final TextField localidadField;
    private final TextField comunidadesField;
    private final ArrayList<Ubicacion> PROVINCIAS = Local.getAllProvincias();
    private final ArrayUbicacion<Ubicacion> LOCALIDADES = Local.getAllLocalidades();
    private final int MAXCARACTERES = 3;
    private final JMapViewer mapViewer;


    public TextoAutoCompletado(TextField houseAddressProvincia, TextField codPostalPrefijoField, TextField codPostalField, TextField localidadField, TextField comunidadesField, JMapViewer mapViewer) {
        this.codPostalPrefijoField = codPostalPrefijoField;
        this.codPostalField = codPostalField;
        this.localidadField = localidadField;
        this.comunidadesField = comunidadesField;
        this.mapViewer = mapViewer;
        this.entradasProvincias = new TreeSet<>();
        this.entradasLocalidades = new TreeSet<>();
        this.ventanaEmergente = new ContextMenu();
        ventanaEmergente.setStyle("-fx-background-color: #225F5C; -fx-text-fill: #92DCD8;");
        setListener(houseAddressProvincia);
        System.out.println("Número de provincias:" + PROVINCIAS.size());
    }







    private void setListener(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            String enteredText = textField.getText();
            if (enteredText == null || enteredText.isEmpty()) {
                ventanaEmergente.hide();
            } else {
                entradasProvincias.clear();
                ventanaEmergente.getItems().clear();

                // Aquí puedes buscar en tus entradas las que coinciden con el texto ingresado y agregarlas a las sugerencias.
                // Luego puedes mostrar las sugerencias en entriesPopup.

                assert PROVINCIAS != null;
                if (textField.isFocused()) {
                    for (Ubicacion ubicacion : PROVINCIAS) {
                        if (ubicacion.getProvincia().toLowerCase().startsWith(enteredText.toLowerCase())) {
                            entradasProvincias.add(ubicacion.getProvincia());
                            codPostalPrefijoField.setText(ubicacion.getCodigoPostal());
                        }
                    }

                    for (String entry : entradasProvincias) {
                        MenuItem item = new MenuItem(entry);
                        item.setOnAction(actionEvent -> {
                            textField.setText(entry);
                            ventanaEmergente.hide();
                        });
                        item.setStyle("-fx-text-fill: #92DCD8;");
                        ventanaEmergente.getItems().add(item);
                    }
                    if (!entradasProvincias.isEmpty()) {
                        ventanaEmergente.show(textField, javafx.geometry.Side.BOTTOM, 0, 0);
                        textField.setStyle("");
                    } else {
                        ventanaEmergente.hide();
                        textField.setStyle("-fx-border-color: red;");
                    }
                }
            }
        });
        localidadField.textProperty().addListener((observable, oldValue, newValue) -> {
            String enteredText = localidadField.getText();
            if (enteredText == null || enteredText.isEmpty()) {
                ventanaEmergente.hide();
            } else {
                entradasLocalidades.clear();
                ventanaEmergente.getItems().clear();

                // Aquí puedes buscar en tus entradas las que coinciden con el texto ingresado y agregarlas a las sugerencias.
                // Luego puedes mostrar las sugerencias en entriesPopup.

                assert LOCALIDADES != null;
                for (Ubicacion ubicacion : LOCALIDADES.getUbicaciones()) {
                    if (ubicacion.getLocalidad().toLowerCase().startsWith(enteredText.toLowerCase())) {
                        entradasLocalidades.add(ubicacion.getLocalidad());
                        codPostalPrefijoField.setText(ubicacion.getCodigoPostal());
                        if (ubicacion.getProvincia().contains("/")){
                            textField.setText(ubicacion.getProvincia().substring(0,ubicacion.getProvincia().indexOf("/")));
                        }else {
                            textField.setText(ubicacion.getProvincia());
                        }
                        if (textField.getText().equalsIgnoreCase("Illes Balears")){
                            textField.setText("Islas Baleares");
                        }
                        textField.setStyle("-fx-border-color: none");
                        comunidadesField.setText(ubicacion.getComunidad());
                        comunidadesField.setStyle("-fx-border-color: none");
                    }
                }
                for (String entry : entradasLocalidades) {
                    MenuItem item = getMenuItem(entry, textField);
                    item.setStyle("-fx-text-fill: #92DCD8;");
                    ventanaEmergente.getItems().add(item);
                    if (ventanaEmergente.getItems().size() > 15) {
                        break;
                    }
                }

                if (!entradasLocalidades.isEmpty()) {
                    ventanaEmergente.show(localidadField, javafx.geometry.Side.BOTTOM, 0, 0);
                    localidadField.setStyle("");
                } else {
                    ventanaEmergente.hide();
                    //localidadField.setStyle("-fx-border-color: red;");
                }
            }
        });
        codPostalField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (newValue != null && newValue.length() > MAXCARACTERES || !Objects.requireNonNull(newValue).matches("\\d*")) {
                    codPostalField.setText(oldValue);
                }
            }
        });
    }

    private MenuItem getMenuItem(String entry, TextField textField) {
        MenuItem item = new MenuItem(entry);
        item.setOnAction(actionEvent -> {
            localidadField.setText(entry);
            assert LOCALIDADES != null;
            textField.setText(LOCALIDADES.getProvincia(entry));
            comunidadesField.setText(LOCALIDADES.getComunidad(textField.getText()));
            ventanaEmergente.hide();
            System.out.println("Latitud: " + LOCALIDADES.getLatitud(LOCALIDADES.getLocalidad(entry)) + " Longitud: " + LOCALIDADES.getLongitud(LOCALIDADES.getLocalidad(entry)));
            mapViewer.setDisplayPosition(new Coordinate(LOCALIDADES.getLatitud(LOCALIDADES.getLocalidad(entry)), LOCALIDADES.getLongitud(LOCALIDADES.getLocalidad(entry))), 9);
        });
        return item;
    }

    public ArrayUbicacion<Ubicacion> getLOCALIDADES() {
        return LOCALIDADES;
    }
}




