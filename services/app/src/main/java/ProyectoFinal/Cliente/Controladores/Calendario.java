package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Comun.Alquiler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

/**
 * La clase Calendario se encarga de la gestión y visualización del calendario en la interfaz de usuario.
 * Concretamente es parte de la pantalla de alquiler de propiedad
 */
public class Calendario {

    @FXML
    private TableView<ObservableList<String>> tablaCalendario;
    @FXML
    private Label etiquetaMes;
    @FXML
    private ListView<String> listaHoras;
    @FXML
    private Button botonMesAnterior;
    @FXML
    private Button botonMesSiguiente;
    private YearMonth mesActual;
    @FXML
    private TextField entradaFieldCalendario;

    @FXML
    private TextField salidaFieldCalendario;
    /** Variables para el manejo de los clics en el calendario */
    private boolean primerClick = true;
    /** Variables para el manejo de los clics en el calendario */
    private LocalDate fechaPrimerClick;
    /** Variables para el manejo de los clics en el calendario */
    private String fechaPrimerClickString;
    /** Variables para el manejo de los clics en el calendario */
    private String horaPrimerClick;
    /** Lista de horas ocupadas de un día */
    private final Map<LocalDate, List<String>> horasOcupadasV2 = new HashMap<>();
    /*** Mapa de celdas de la tabla de calendario */
    private final Map<Point, TableCell<ObservableList<String>, String>> cellMap = new HashMap<>();

    /**
     * Este método se ejecuta al inicializar la clase. Configura los elementos de la interfaz de usuario y establece los eventos de los botones.
     */
    public void initialize() {
        botonMesAnterior.getStyleClass().add("button-style");
        botonMesSiguiente.getStyleClass().add("button-style");
        listaHoras.setDisable(true);
        mesActual = YearMonth.now(); // Configura el mes actual al iniciar
        configurarListaHoras();
        configurarTablaCalendario();
        actualizarCalendario();
        // Configura los eventos de los botones de navegación
        botonMesAnterior.setOnAction(event -> irAlMesAnterior());
        botonMesSiguiente.setOnAction(event -> irAlMesSiguiente());

        listaHoras.setOnMouseClicked(event -> {
            String horaSeleccionada = obtenerHoraSeleccionada();
            if (horaSeleccionada != null) {
                manejarClicEnDia(fechaPrimerClickString, horaSeleccionada);
            }
            String diaSeleccionado = obtenerDiaSeleccionado();
            if (diaSeleccionado != null) {
                int dia = Integer.parseInt(diaSeleccionado);
                LocalDate fecha = mesActual.atDay(dia);
                fechaPrimerClickString = diaSeleccionado;
                actualizarListaHoras(fecha);
            }
        });

        tablaCalendario.setOnMouseClicked(event -> {
            String diaSeleccionado = obtenerDiaSeleccionado();
            if (diaSeleccionado != null) {
                int dia = Integer.parseInt(diaSeleccionado);
                LocalDate fecha = mesActual.atDay(dia);
                fechaPrimerClickString = diaSeleccionado;
                actualizarListaHoras(fecha);
            }
        });
    }

    /**
     * Este método obtiene el día seleccionado en la tabla del calendario.
     * @return El día seleccionado como una cadena de texto.
     */
    private String obtenerDiaSeleccionado() {
        TablePosition pos = tablaCalendario.getSelectionModel().getSelectedCells().get(0);
        int row = pos.getRow();
        int col = pos.getColumn();
        return tablaCalendario.getColumns().get(col).getCellData(row).toString();
    }

    /**
     * Este método maneja los clics en los días del calendario. Si es el primer clic, guarda la fecha y la hora. Si es el segundo clic, guarda la fecha y la hora de salida.
     * @param fecha La fecha seleccionada.
     * @param hora La hora seleccionada.
     */
    private void manejarClicEnDia(String fecha, String hora) {
        if (fecha == null){
            return;
        }
        if (!fecha.isEmpty() && !hora.isEmpty()) {
            int dia = Integer.parseInt(fecha);
            LocalDate date = mesActual.atDay(dia);

            // Si es el primer clic, guarda la fecha y la hora
            if (primerClick) {
                entradaFieldCalendario.clear();
                salidaFieldCalendario.clear();
                fechaPrimerClick = date;
                horaPrimerClick = obtenerHoraSeleccionada();
                primerClick = false;
                String entrada = fechaPrimerClick + " a las " + horaPrimerClick;
                agregarEntrada(entrada);
            } else {

                String salida = date + " a las " + obtenerHoraSeleccionada();
                agregarSalida(salida);

                // Reinicia el estado
                primerClick = true;
            }
        }
    }

    /**
     * Este método obtiene la hora seleccionada en la lista de horas.
     * @return La hora seleccionada como una cadena de texto.
     */
    private String obtenerHoraSeleccionada() {
        return listaHoras.getSelectionModel().getSelectedItem();
    }

    /**
     * Este método agrega la fecha y hora de salida al campo de salida.
     * @param salida La fecha y hora de salida como una cadena de texto.
     */
    private void agregarSalida(String salida) {
        if (salidaFieldCalendario.getText().isEmpty()) {
            salidaFieldCalendario.setText(salida);
            salidaFieldCalendario.setStyle("-fx-border-color: none");
        } else {
            salidaFieldCalendario.setText(salidaFieldCalendario.getText() + ", " + salida);
            salidaFieldCalendario.setStyle("-fx-border-color: none");
        }
    }

    /**
     * Este método agrega la fecha y hora de entrada al campo de entrada.
     * @param entrada La fecha y hora de entrada como una cadena de texto.
     */
    private void agregarEntrada(String entrada){
        if (entradaFieldCalendario.getText().isEmpty()) {
            entradaFieldCalendario.setText(entrada);
            entradaFieldCalendario.setStyle("-fx-border-color: none");
        } else {
            entradaFieldCalendario.setText(entradaFieldCalendario.getText() + ", " + entrada);
            entradaFieldCalendario.setStyle("-fx-border-color: none");
        }
    }

    /**
     * Este método configura la tabla del calendario, añadiendo columnas para cada día de la semana.
     */
    private void configurarTablaCalendario() {
        // Días de la semana en español
        String[] diasDeLaSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

        // Añadir columnas para cada día de la semana
        for (int i = 0; i < 7; i++) {
            TableColumn<ObservableList<String>, String> columna = new TableColumn<>(diasDeLaSemana[i]);
            columna.setResizable(false);
            columna.setPrefWidth(60);
            final int columnIndex = i;
            columna.setCellValueFactory(cellData -> {
                ObservableList<String> row = cellData.getValue();
                if (columnIndex < row.size()) {
                    return new javafx.beans.property.SimpleStringProperty(row.get(columnIndex));
                } else {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            });
            // Deshabilitar la ordenación
            columna.setSortable(false);
            // Añadir estilo CSS para centrar el texto y cambiar el color
            columna.setStyle("-fx-alignment: CENTER; -fx-text-fill: white;");
            // Configurar fábrica de celdas personalizada
            columna.setCellFactory(new Callback<TableColumn<ObservableList<String>, String>, TableCell<ObservableList<String>, String>>() {
                @Override
                public TableCell<ObservableList<String>, String> call(TableColumn<ObservableList<String>, String> param) {
                    return new TableCell<ObservableList<String>, String>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else {
                                setText(item);
                                setStyle("-fx-alignment: CENTER; -fx-text-fill: #225F5C;");
                                setOnMouseClicked(event -> {
                                    if (!item.isEmpty()) {
                                        fechaPrimerClickString = item;
                                        listaHoras.setDisable(false);
                                    }
                                });
                            }
                            Point point = new Point(getIndex(), columnIndex);
                            cellMap.put(point, this);
                        }
                    };
                }
            });
            tablaCalendario.getColumns().add(columna);
        }
    }

    /**
     * Este método configura la lista de horas, añadiendo una entrada para cada hora del día.
     */
    private void configurarListaHoras() {
        ObservableList<String> horas = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            horas.add(String.format("%02d:00", i));
        }
        listaHoras.setItems(horas);
    }

    /**
     * Este método actualiza el calendario, mostrando los días del mes actual.
     */
    private void actualizarCalendario() {
        // Actualizar la etiqueta del mes
        etiquetaMes.setText(mesActual.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + mesActual.getYear());
        etiquetaMes.setStyle("-fx-text-fill: #92DCD8");
        // Limpiar datos previos
        tablaCalendario.getItems().clear();

        // Añadir datos para el mes actual
        LocalDate primerDiaDelMes = mesActual.atDay(1);
        int diaDeLaSemana = primerDiaDelMes.getDayOfWeek().getValue() % 7; // Ajustar para que el lunes sea el primer día de la semana
        int diasEnMes = mesActual.lengthOfMonth();

        ObservableList<String> semana = FXCollections.observableArrayList();
        for (int i = 0; i < diaDeLaSemana; i++) {
            semana.add("");
        }
        for (int dia = 1; dia <= diasEnMes; dia++) {
            semana.add(String.valueOf(dia));
            if (semana.size() == 7) {
                tablaCalendario.getItems().add(FXCollections.observableArrayList(semana));
                semana.clear();
            }
        }
        if (!semana.isEmpty()) {
            tablaCalendario.getItems().add(FXCollections.observableArrayList(semana));
        }
    }

    /**
     * Este método cambia el mes actual al mes anterior y actualiza el calendario.
     */
    @FXML
    private void irAlMesAnterior() {
        mesActual = mesActual.minusMonths(1);
        actualizarCalendario();
    }

    /**
     * Este método cambia el mes actual al mes siguiente y actualiza el calendario.
     */
    @FXML
    private void irAlMesSiguiente() {
        mesActual = mesActual.plusMonths(1);
        actualizarCalendario();
    }

    /**
     * Este método establece el campo de entrada en el calendario.
     * @param value El campo de entrada.
     */
    public void setEntradaField(TextField value) {
        this.entradaFieldCalendario = value;
    }

    /**
     * Este método establece el campo de salida en el calendario.
     * @param value El campo de salida.
     */
    public void setSalidaField(TextField value) {
        this.salidaFieldCalendario = value;
    }

    /**
     * Este método deshabilita las fechas en el calendario que están ocupadas.
     * @param fechasOcupadas Una lista de alquileres que representan las fechas ocupadas.
     */
    public void deshabilitarFechas(ArrayList<Alquiler> fechasOcupadas) {
        for (Alquiler alquiler : fechasOcupadas) {
            LocalDateTime fechaActual = alquiler.getCheckin();
            LocalDateTime fechaFin = alquiler.getCheckout();

            while (!fechaActual.isAfter(fechaFin)) {
                LocalDate fecha = fechaActual.toLocalDate();
                List<String> horas = horasOcupadasV2.getOrDefault(fecha, new ArrayList<>());

                // Añadir solo las horas específicas entre checkin y checkout
                if (fechaActual.toLocalDate().isEqual(fechaFin.toLocalDate())) {
                    // Misma fecha, añadir horas entre checkin y checkout
                    for (int i = fechaActual.getHour(); i <= fechaFin.getHour(); i++) {
                        horas.add(String.format("%02d:00", i));
                    }
                } else {
                    // Diferentes fechas, añadir todas las horas del día para fechas intermedias
                    if (fechaActual.toLocalDate().isEqual(alquiler.getCheckin().toLocalDate())) {
                        // Día de checkin, añadir horas desde checkin hasta el final del día
                        for (int i = fechaActual.getHour(); i < 24; i++) {
                            horas.add(String.format("%02d:00", i));
                        }
                    } else if (fechaActual.toLocalDate().isEqual(alquiler.getCheckout().toLocalDate())) {
                        // Día de checkout, añadir horas desde el inicio del día hasta checkout
                        for (int i = 0; i <= fechaFin.getHour(); i++) {
                            horas.add(String.format("%02d:00", i));
                        }
                    } else {
                        // Día intermedio, añadir todas las horas del día
                        for (int i = 0; i < 24; i++) {
                            horas.add(String.format("%02d:00", i));
                        }
                    }
                }

                horasOcupadasV2.put(fecha, horas);
                fechaActual = fechaActual.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            }
        }


        for (Map.Entry<LocalDate, List<String>> entry : horasOcupadasV2.entrySet()) {
            LocalDate fecha = entry.getKey();
            List<String> horas = entry.getValue();
            if (horas.size() == 24) {
                deshabilitarDia(String.valueOf(fecha.getDayOfMonth()));
            }
        }

        // Deshabilitar fechas en el calendario
        tablaCalendario.getItems().forEach(row -> row.forEach(dia -> {
            if (!dia.isEmpty()) {
                LocalDate fecha = mesActual.atDay(Integer.parseInt(dia));
                if (horasOcupadasV2.containsKey(fecha) && horasOcupadasV2.get(fecha).size() == 24) {
                    deshabilitarDia(dia);
                }
            }
        }));
    }


    /**
     * Este método deshabilita un día específico en el calendario.
     * @param dia El día a deshabilitar.
     */
    private void deshabilitarDia(String dia) {
        tablaCalendario.getItems().forEach(row -> row.forEach(d -> {
            if (d.equals(dia)) {
                int rowIndex = tablaCalendario.getItems().indexOf(row);
                int colIndex = row.indexOf(d);
                TableCell<ObservableList<String>, String> cell = getCell(rowIndex, colIndex);
                if (cell != null) {
                    cell.setDisable(true);
                    cell.setStyle("-fx-background-color: #ffc0cb;"); // Cambiar el color de fondo
                }
            }
        }));
    }

    /**
     * Este método obtiene una celda específica de la tabla del calendario.
     * @param row El índice de la fila de la celda.
     * @param col El índice de la columna de la celda.
     * @return La celda en la posición especificada.
     */
    private TableCell<ObservableList<String>, String> getCell(int row, int col) {
        return cellMap.get(new Point(row, col));
    }

    /**
     * Este método actualiza la lista de horas, deshabilitando las horas que están ocupadas en una fecha específica.
     * @param fecha La fecha en la que se actualizarán las horas.
     */
    private void actualizarListaHoras(LocalDate fecha) {
        List<String> horas = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            horas.add(String.format("%02d:00", i));
        }
        listaHoras.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item);
                    if (horasOcupadasV2.containsKey(fecha) && horasOcupadasV2.get(fecha).contains(item)) {
                        setDisable(true);
                        setStyle("-fx-background-color: red;"); // Cambiar el color de fondo
                    } else {
                        setDisable(false);
                        setStyle("");
                    }
                }
            }
        });
        listaHoras.setItems(FXCollections.observableArrayList(horas));
    }


    /**
     * Este método obtiene la fecha seleccionada en el calendario.
     * @return La fecha seleccionada.
     */
    public LocalDate getFechaSeleccionada() {
        String diaSeleccionado = obtenerDiaSeleccionado();
        if (diaSeleccionado != null) {
            int dia = Integer.parseInt(diaSeleccionado);
            LocalDate fecha = mesActual.atDay(dia);
            fechaPrimerClickString = diaSeleccionado;
            return fecha;
        }
        return null;
    }

    /**
     * Este método obtiene la hora seleccionada en el calendario.
     * @return La hora seleccionada.
     */
    public String getHoraSeleccionada() {
        return obtenerHoraSeleccionada();
    }


    public void setConfigMisPropiedades(PantallaPropiedad pantallaPropiedad){
        listaHoras.setOnMouseClicked(event -> {
            pantallaPropiedad.filtrarAlquileres();
        });
    }
}
