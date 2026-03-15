package ProyectoFinal.Cliente.Controladores;

import ProyectoFinal.Cliente.BufferesUser;
import ProyectoFinal.Cliente.Librerias.Jaspersoft;
import ProyectoFinal.Comun.Alquiler;
import ProyectoFinal.Comun.Facturacion;
import ProyectoFinal.Comun.Propiedad;
import ProyectoFinal.Comun.Tarjeta;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * La clase Factura se encarga de la gestión y visualización de la factura en la interfaz de usuario.
 */
public class Factura {

    private static final String CONTROL_STRING = "TRWAGMYFPDXBNJZSQVHLCKE";

    @FXML
    public BorderPane mainPane;
    @FXML
    public TextField nombreCompleto;
    @FXML
    public TextField nif;
    @FXML
    public Label labelNombre;
    @FXML
    public TextField numeroTarjeta;
    @FXML
    public TextField fechaCaducidad;
    @FXML
    public TextField cvv;
    @FXML
    public TextField direccionFacturacion;
    @FXML
    public ComboBox tarjetasComboBox;
    @FXML
    public CheckBox guardarCheckBox;
    @FXML
    public ImageView logoTarjetaImg;
    @FXML
    public Label pagoLabel;
    /** Tarjeta de crédito que se ha usado para pagar **/
    private static Tarjeta tarjeta;
    /** Propiedad que se va a alquilar  **/
    private static Propiedad propiedad;
    @FXML
    public Label confirmacionLabel;
    @FXML
    public StackPane facturaStackPane;
    @FXML
    public Button eliminarTarjetaBoton;
    @FXML
    public Button botonPagar;
    @FXML
    private StackPane step1Pane, step2Pane, step3Pane;
    @FXML
    private Label stepLabel1, stepLabel2, stepLabel3;
    @FXML
    private Button prevButton, nextButton;

    /** Indice de los pasos a seguir**/
    private int currentStep = 0;

    /** Errores en los campos de entrada **/
    private boolean fechaCaducidadError = false;
    /** Errores en los campos de entrada **/
    private boolean nifError = false;
    /** Fecha de entrada **/
    private static String checkIn;
    /** Fecha de salida **/
    private static String checkOut;
    /** Número de personas **/
    private int numPersonas;
    /** Estado de la pantalla principal**/
    private Stage estadoPantallaPropiedad;
    /** Guardar estado de la factura **/
    private boolean pagado = false;

    /**
     * Este método se ejecuta al inicializar la clase. Configura los elementos de la interfaz de usuario y establece los eventos de los botones.
     */
    @FXML
    private void initialize() {
        try {
            eliminarTarjetaBoton.setDisable(true);

            float pagoTotal = (propiedad.getPrecioHora() * calcularHoras(checkIn, checkOut));
            pagoTotal = pagoTotal * 1.01f; // Añade un 1% de interés
            pagoLabel.setText("Pago total: " + pagoTotal  + "€");
            tarjetasComboBox.getItems().add(0, "Ninguna tarjeta seleccionada");
            tarjetasComboBox.getSelectionModel().select(0);
            for (Tarjeta tarjeta : Principal.getUsuario().getTarjeta()) {
                String numeroFormateado = tarjeta.getNumero().replaceAll(".{4}(?!$)", "$0 ");
                tarjetasComboBox.getItems().add(numeroFormateado);
            }
            tarjetasComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.equals("Ninguna tarjeta seleccionada")) {
                        guardarCheckBox.setDisable(false);
                        numeroTarjeta.setDisable(false);
                        fechaCaducidad.setDisable(false);
                        cvv.setDisable(false);
                        direccionFacturacion.setDisable(false);
                        numeroTarjeta.clear();
                        fechaCaducidad.clear();
                        cvv.clear();
                        direccionFacturacion.clear();
                        return;
                    }else{
                        eliminarTarjetaBoton.setDisable(false);
                        eliminarTarjetaBoton.setOnAction(event -> {
                            for (Tarjeta tarjeta : Principal.getUsuario().getTarjeta()) {
                                if (tarjeta.getNumero().equals(tarjetasComboBox.getSelectionModel().getSelectedItem().toString().replaceAll("\\s", ""))) {
                                    BufferesUser.eliminarTarjeta(tarjeta);
                                    tarjetasComboBox.getItems().remove(tarjetasComboBox.getSelectionModel().getSelectedItem());
                                    tarjetasComboBox.getSelectionModel().select(0);
                                    break;
                                }
                            }
                        });
                    }
                    // Si se selecciona una tarjeta, deshabilita el CheckBox
                    guardarCheckBox.setDisable(true);
                    numeroTarjeta.setDisable(true);
                    for (Tarjeta tarjeta : Principal.getUsuario().getTarjeta()) {
                        if (tarjeta.getNumero().equals(tarjetasComboBox.getSelectionModel().getSelectedItem().toString().replaceAll("\\s", ""))) {
                            fechaCaducidad.setText(tarjeta.getFechaCaducidad());
                            cvv.setText(String.valueOf(tarjeta.getCvv()));
                            direccionFacturacion.setText(tarjeta.getDireccionFacturacion());
                            numeroTarjeta.setText(tarjetasComboBox.getSelectionModel().getSelectedItem().toString());
                            fechaCaducidad.setText(tarjeta.getFechaCaducidad());
                            cvv.setText(String.valueOf(tarjeta.getCvv()));
                            direccionFacturacion.setText(tarjeta.getDireccionFacturacion());
                            break;
                        }
                    }
                    direccionFacturacion.setDisable(true);
                    fechaCaducidad.setDisable(true);
                    cvv.setDisable(true);
                    guardarCheckBox.setSelected(false);
            });
            numeroTarjeta.textProperty().addListener((observable, oldValue, newValue) -> {
                String formatted = newValue.replaceAll("[^\\d ]", "");
                if (formatted.length() > 19) {
                    formatted = formatted.substring(0, 19);
                }
                int length = formatted.length();
                long spaces = formatted.chars().filter(ch -> ch == ' ').count();
                if ((length + 1) % 5 == 0 && length > 0 && !formatted.endsWith(" ") && spaces < 3) {
                    formatted += " ";
                }
                if (!newValue.equals(formatted)) {
                    numeroTarjeta.setText(formatted);
                }
            });
            cvv.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    cvv.setText(newValue.replaceAll("[^\\d]", ""));
                }
                if (cvv.getText().length() > 3) {
                    String s = cvv.getText().substring(0, 3);
                    cvv.setText(s);
                }
            });
            fechaCaducidad.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // Si el nuevo valor de la propiedad focused es false, es decir, el TextField ha perdido el foco
                    String fecha = fechaCaducidad.getText();
                    if (fecha.length() == 5) {
                        String[] parts = fecha.split("/");
                        if (parts.length == 2) {
                            try {
                                int month = Integer.parseInt(parts[0]);
                                int year = Integer.parseInt(parts[1]);
                                int currentYear = LocalDate.now().getYear() % 100; // últimos dos dígitos del año actual
                                int currentMonth = LocalDate.now().getMonthValue();

                                // Verificar mes válido (1-12)
                                if (month < 1 || month > 12) {
                                    fechaCaducidad.setStyle("-fx-border-color: red");
                                    fechaCaducidadError = true;
                                    return;
                                }else{
                                    fechaCaducidad.setStyle("-fx-border-color: none");
                                    fechaCaducidadError = false;
                                }

                                // Verificar año y mes válidos
                                if (year < currentYear || (year == currentYear && month < currentMonth)) {
                                    fechaCaducidad.setStyle("-fx-border-color: red");
                                    fechaCaducidadError = true;
                                }else{
                                    fechaCaducidad.setStyle("-fx-border-color: none");
                                    fechaCaducidadError = false;
                                }
                            } catch (NumberFormatException e) {
                                // Dejar la fecha como está si no se puede convertir a número
                                fechaCaducidad.setStyle("-fx-border-color: red");
                                fechaCaducidadError = true;
                            }
                        }
                    }
                }
            });

            fechaCaducidad.textProperty().addListener((observable, oldValue, newValue) -> {
                // Permitir solo dígitos
                String newValueFiltered = newValue.replaceAll("[^\\d/]", "");
                if (!newValue.matches("\\d*") && !newValue.contains("/")) {
                    if (!newValueFiltered.equals(oldValue)) {
                        fechaCaducidad.setText(newValueFiltered);
                    }
                    return;
                }
                // Limitar la longitud a 5 caracteres (contando la '/')
                if (fechaCaducidad.getText().length() > 5) {
                    fechaCaducidad.setText(oldValue);
                }

                // Añadir '/' después de los primeros dos dígitos, si la longitud es exactamente 2
                if (fechaCaducidad.getText().length() == 2 && !oldValue.endsWith("/")) {
                    fechaCaducidad.setText(fechaCaducidad.getText() + "/");
                }

                // Eliminar '/' si es el último carácter y la longitud es distinta de 3
                if (fechaCaducidad.getText().endsWith("/") && fechaCaducidad.getText().length() != 3) {
                    fechaCaducidad.setText(fechaCaducidad.getText().substring(0, fechaCaducidad.getText().length() - 1));
                }
            });


            numeroTarjeta.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // Si el nuevo valor de la propiedad focused es false, es decir, el TextField ha perdido el foco
                    String cardType = getCardType(numeroTarjeta.getText());
                    if (cardType.equals("Unknown")) {
                        logoTarjetaImg.setImage(null);
                    }
                    System.out.println(cardType);
                }
            });
            nif.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() == 9) { // Un NIF/CIF válido tiene 9 caracteres
                    char controlLetter = ' ';
                    if (Character.isDigit(newValue.charAt(0))) {
                        // Asumimos que es un NIF
                        controlLetter = calculateDNILetter(newValue.substring(0, 8));
                    } else {
                        // Asumimos que es un CIF
                        controlLetter = calculateCIFLetter(newValue);
                    }
                    if (controlLetter != newValue.charAt(newValue.length() - 1)) {
                        nif.setStyle("-fx-border-color: red");
                        nifError = true;
                    } else {
                        nif.setStyle("-fx-border-color: none");
                        nifError = false;
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        updateStep();
    }

    /**
     * Este método establece el estado de la pantalla de la propiedad.
     * @param estadoPantallaPropiedad El estado de la pantalla de la propiedad.
     */
    public void setEstadoPantallaPropiedad(Stage estadoPantallaPropiedad) {
        this.estadoPantallaPropiedad = estadoPantallaPropiedad;
    }

    /**
     * Este método maneja el evento de clic en el botón "Siguiente".
     */
    @FXML
    private void nextStep() {

        switch (currentStep) {
            case 0:
                if (!validarCampos()) return;
                break;
            case 1:

                if (!pagado){
                    javafx.util.Duration fxDuration = javafx.util.Duration.millis(500);
                    FadeTransition fadeTransition = new FadeTransition(fxDuration, botonPagar);
                    fadeTransition.setFromValue(1.0);
                    fadeTransition.setToValue(0.2);
                    fadeTransition.setAutoReverse(true);
                    fadeTransition.setCycleCount(10); // Para 5 parpadeos
                    fadeTransition.play();
                    return;
                }

                break;
            case 2:
                if (guardarCheckBox.isSelected()) {
                    // Guardar la tarjeta en la base de datos
                    String numTarjeta = numeroTarjeta.getText().replaceAll("\\s", "");
                    tarjeta = new Tarjeta(numTarjeta, Principal.getUsuario(), fechaCaducidad.getText(), Integer.parseInt(cvv.getText()), direccionFacturacion.getText(), "insertarTarjeta");
                    BufferesUser.insertarTarjeta(tarjeta);

                }
                Stage stage = (Stage) mainPane.getScene().getWindow();

                stage.close();
                estadoPantallaPropiedad.close();
                break;
            default:
                // Acciones por defecto si currentStep no es ninguno de los anteriores
                break;
        }
        if (currentStep < 2) {
            currentStep++;
        }
        updateStep();
    }

    /**
     * Este método maneja el evento de clic en el botón "Anterior".
     */
    @FXML
    private void prevStep() {
        if (currentStep > 0) {
            currentStep--;
        }
        updateStep();
    }

    /**
     * Este método actualiza el paso actual en el proceso de facturación.
     */
    private void updateStep() {
        step1Pane.setVisible(currentStep == 0);
        step2Pane.setVisible(currentStep == 1);
        step3Pane.setVisible(currentStep == 2);

        stepLabel1.setStyle(currentStep == 0 ? "-fx-background-color: #92DCD8;" : "-fx-background-color: #379F99;");
        stepLabel2.setStyle(currentStep == 1 ? "-fx-background-color: #92DCD8;" : "-fx-background-color: #379F99;");
        stepLabel3.setStyle(currentStep == 2 ? "-fx-background-color: #92DCD8;" : "-fx-background-color: #379F99;");

        prevButton.setDisable(currentStep == 0);
        nextButton.setText(currentStep == 2 ? "Terminar" : "Siguiente");
    }

    /**
     * Este método establece el nombre completo del usuario.
     * @param nombreCompleto El nombre completo del usuario.
     */
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto.setText(nombreCompleto);
    }

    /**
     * Este método valida los campos de entrada.
     * @return true si todos los campos son válidos, false en caso contrario.
     */
    private boolean validarCampos() {
        if (nombreCompleto.getText().isEmpty()) {
            nombreCompleto.setStyle("-fx-border-color: red");
            return false;
        }else{
            nombreCompleto.setStyle("-fx-border-color: none");
        }
        if (nif.getText().isEmpty() || nifError) {
            nif.setStyle("-fx-border-color: red");
            return false;
        }else {
            nif.setStyle("-fx-border-color: none");
        }
        if (numeroTarjeta.getText().isEmpty() || !isValidCardNumber(numeroTarjeta.getText())) {
            numeroTarjeta.setStyle("-fx-border-color: red");
            return false;
        }else {
            numeroTarjeta.setStyle("-fx-border-color: none");
        }
        if (fechaCaducidad.getText().isEmpty() || fechaCaducidadError) {
            fechaCaducidad.setStyle("-fx-border-color: red");
            return false;
        }else {
            fechaCaducidad.setStyle("-fx-border-color: none");
        }
        if (cvv.getText().isEmpty()) {

            cvv.setStyle("-fx-border-color: red");
            return false;
        }else {
            cvv.setStyle("-fx-border-color: none");
        }
        if (direccionFacturacion.getText().isEmpty()) {
            direccionFacturacion.setStyle("-fx-border-color: red");
            return false;
        }else {
            direccionFacturacion.setStyle("-fx-border-color: none");
        }
        return true;
    }

    /**
     * Este método verifica si un número de tarjeta es válido.
     * @param cardNumber El número de tarjeta a verificar.
     * @return true si el número de tarjeta es válido, false en caso contrario.
     */
    private boolean isValidCardNumber(String cardNumber) {
        cardNumber = cardNumber.replaceAll("\\D", ""); // Remove non-digit characters
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    /**
     * Este método obtiene el tipo de tarjeta basado en el número de tarjeta.
     * @param cardNumber El número de tarjeta.
     * @return El tipo de tarjeta.
     */
    private String getCardType(String cardNumber) {
        cardNumber = cardNumber.replaceAll("\\s", "");
        if (cardNumber.length() < 6) {
            return "Unknown1";
        }
        String firstDigit = cardNumber.substring(0, 1);
        String firstTwoDigits = cardNumber.substring(0, 2);
        String firstSixDigits = cardNumber.substring(0, 6);
        String firstFourDigits = cardNumber.substring(0, 4);

        if (!firstSixDigits.matches("\\d+")) {
            return "Unknown2";
        }

        if (firstDigit.equals("4")) {
            logoTarjetaImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/visa.png"))));
            return "Visa";
        } else if (Integer.parseInt(firstTwoDigits) >= 51 && Integer.parseInt(firstTwoDigits) <= 55) {
            logoTarjetaImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/mastercard.png"))));
            return "MasterCard";
        } else if (firstTwoDigits.equals("34") || firstTwoDigits.equals("37")) {
            logoTarjetaImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/american_express.jpg"))));
            return "American Express";
        } else if (firstFourDigits.equals("6011") || (Integer.parseInt(firstSixDigits) >= 622126 && Integer.parseInt(firstSixDigits) <= 622925) || (Integer.parseInt(firstFourDigits) >= 644 && Integer.parseInt(firstFourDigits) <= 649) || firstDigit.equals("65")) {
            logoTarjetaImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/imgs/discover.png"))));
            return "Discover";
        } else {
            return "Unknown3";
        }
    }

    /**
     * Este método calcula la letra de control para un DNI.
     * @param dni El DNI para el que se calculará la letra de control.
     * @return La letra de control para el DNI.
     */
    public static char calculateDNILetter(String dni) {
        int dniNumber = Integer.parseInt(dni);
        int index = dniNumber % 23;
        return CONTROL_STRING.charAt(index);
    }

    /**
     * Este método calcula la letra de control para un CIF.
     * @param cif El CIF para el que se calculará la letra de control.
     * @return La letra de control para el CIF.
     */
    private char calculateCIFLetter(String cif) {
        // Implementa aquí la lógica para calcular la letra de control del CIF
        String digits = cif.substring(1, cif.length() - 1);
        int sumEven = 0;
        int sumOdd = 0;

        for (int i = 0; i < digits.length(); i++) {
            int digit = Character.getNumericValue(digits.charAt(i));
            if (i % 2 == 0) { // positions are 0-based, so even indices are odd positions
                digit *= 2;
                sumOdd += digit > 9 ? digit - 9 : digit;
            } else {
                sumEven += digit;
            }
        }

        int sumTotal = sumEven + sumOdd;
        int controlDigit = 10 - (sumTotal % 10);
        if (controlDigit == 10) {
            controlDigit = 0;
        }

        char lastChar = cif.charAt(cif.length() - 1);
        if (Character.isDigit(lastChar)) {
            return Character.forDigit(controlDigit, 10);
        } else {
            return "JABCDEFGHI".charAt(controlDigit);
        }
    }

    /**
     * Este método establece la fecha de check-in.
     * @param checkIn La fecha de check-in.
     */
    public static void setCheckIn(String checkIn) {
        Factura.checkIn = checkIn;
    }

    /**
     * Este método establece la fecha de check-out.
     * @param checkOut La fecha de check-out.
     */
    public static void setCheckOut(String checkOut) {
        Factura.checkOut = checkOut;
    }

    /**
     * Este método establece el número de personas.
     * @param numPersonas El número de personas.
     */
    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    /**
     * Este método establece la propiedad.
     * @param propiedad La propiedad.
     */
    public static void setPropiedad(Propiedad propiedad) {
        Factura.propiedad = propiedad;
    }

    /**
     * Este método calcula el número de horas entre el check-in y el check-out.
     * @param checkIn La fecha y hora de check-in.
     * @param checkOut La fecha y hora de check-out.
     * @return El número de horas entre el check-in y el check-out.
     */
    public long calcularHoras(String checkIn, String checkOut) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd 'a las' HH:mm");
        LocalDateTime inicio = LocalDateTime.parse(checkIn, formatter);
        LocalDateTime fin = LocalDateTime.parse(checkOut, formatter);

        Duration duracion = Duration.between(inicio, fin);

        return duracion.toHours();
    }

    /**
     * Este método maneja el evento de clic en el botón "Pagar".
     */
    public void pagar(){
        pagado = true;
        confirmacionLabel.setText("Detalles de la confirmación: Pagado");
        String inicio = checkIn.replace("a las ", "");
        String fin = checkOut.replace("a las ", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        tarjeta = new Tarjeta(numeroTarjeta.getText().replaceAll("\\s", ""), Principal.getUsuario(), fechaCaducidad.getText(), Integer.parseInt(cvv.getText()), direccionFacturacion.getText(), "insertarTarjeta");
        Set<Integer> existingIds = new HashSet<>();
        for (Alquiler alquiler : propiedad.getAlquileres()) {
            existingIds.add(alquiler.getId());
        }

        int id = 0;
        while (existingIds.contains(id)) {
            id++;
        }
        Alquiler alquila = new Alquiler(id, Principal.getUsuario(), propiedad, numPersonas, LocalDateTime.parse(inicio, formatter), LocalDateTime.parse(fin, formatter), (propiedad.getPrecioHora() * calcularHoras(checkIn, checkOut)) * 1.01f);
        alquila.setAccionServer("alquilar");
        existingIds.clear();
        BufferesUser.insertarAlquiler(alquila);
        for (Facturacion facturas : Principal.getUsuario().getFacturacion()) {
            existingIds.add(facturas.getId());
        }

        id = 0;
        while (existingIds.contains(id)) {
            id++;
        }

        Facturacion facturacion = new Facturacion(id,tarjeta,Principal.getUsuario(),propiedad, LocalDate.now(),nif.getText());
        try {
            new Jaspersoft(propiedad, Principal.getUsuario(), alquila, facturaStackPane, facturacion);
            BufferesUser.insertarFacturacion(facturacion);
        } catch (JRException e) {
            System.err.println("Error al generar el informe");
            e.printStackTrace();
        }

    }
}

