package ProyectoFinal.Cliente.Librerias;

import ProyectoFinal.Comun.Alquiler;
import ProyectoFinal.Comun.Facturacion;
import ProyectoFinal.Comun.Propiedad;
import ProyectoFinal.Comun.Usuario;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.StackPane;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;

import javax.swing.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class Jaspersoft {
    private static final String USERHOME = System.getProperty("user.home");

    public Jaspersoft(Propiedad propiedad, Usuario usuario, Alquiler alquila, StackPane pantalla, Facturacion facturacion) throws JRException {
        System.out.println("Generando informe..." + propiedad.getNombre() + " " + usuario.getNombre() + " " + alquila.getNumPersonas() + " " + alquila.getTotalCoste());
        // Crear datos de ejemplo
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> property = new HashMap<>();
        property.put("nombre", propiedad.getNombre());
        property.put("tipo", propiedad.getTipo());
        property.put("comunidad", propiedad.getComunidad());
        property.put("provincia", propiedad.getProvincia());
        property.put("localidad", propiedad.getLocalidad());
        property.put("direccion", propiedad.getDireccion());
        property.put("codigoPostal", propiedad.getCodigoPostal());
        property.put("precioHora", propiedad.getPrecioHora());
        property.put("descripcion", propiedad.getDescripcion());
        property.put("usuarionombre", usuario.getNombre());
        property.put("usuarioemail", usuario.getEmail());
        property.put("usuariotelefono", usuario.getNumTelefono());
        property.put("numPersonas", alquila.getNumPersonas());
        property.put("checkin", alquila.getCheckin());
        property.put("checkout", alquila.getCheckout());
        property.put("totalCoste", alquila.getTotalCoste());
        property.put("fecha", LocalDate.now());
        property.put("dni", facturacion.getNif());

        data.add(property);

        // Cargar el informe JRXML
        JasperReport report = JasperCompileManager.compileReport(Objects.requireNonNull(getClass().getResourceAsStream("/ProyectoFinal/jaspersoft/HumHousePlantilla.jrxml")));

        // Parámetros
        Map<String, Object> parameters = new HashMap<>();
        String logoPath =  USERHOME + "/ProyectoFinal/imgs/logo.png";
        parameters.put("logo", logoPath);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);
        parameters.put("data", dataSource);

        // Llenar el informe
        JasperPrint print = JasperFillManager.fillReport(report, parameters, dataSource);

        JRViewer viewer = new JRViewer(print);

        // Crear un SwingNode para incrustar el JRViewer en JavaFX
        SwingNode swingNode = new SwingNode();
        SwingUtilities.invokeLater(() -> swingNode.setContent(viewer));

        // Añadir el SwingNode a un StackPane de JavaFX
        pantalla.getChildren().add(swingNode);
    }
}
