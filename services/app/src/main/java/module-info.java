module org.example.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires mysql.connector.j;
    requires java.sql;
    requires java.desktop;
    requires jmapviewer;
    requires javafx.swing;
    requires org.controlsfx.controls;
    requires jsch;
    requires org.apache.logging.log4j.core;
    requires org.apache.logging.log4j;
    requires jasperreports;
    requires org.jxmapviewer.jxmapviewer2;

    exports ProyectoFinal.Cliente;

    opens ProyectoFinal.Cliente to javafx.graphics, javafx.fxml;

    exports ProyectoFinal.Comun;

    opens ProyectoFinal.Comun to javafx.fxml;

    exports ProyectoFinal.Cliente.Controladores;

    opens ProyectoFinal.Cliente.Controladores to javafx.fxml, javafx.graphics;

    exports ProyectoFinal.Cliente.Celdas;

    opens ProyectoFinal.Cliente.Celdas to javafx.fxml, javafx.graphics;

    exports ProyectoFinal.Cliente.Librerias;

    opens ProyectoFinal.Cliente.Librerias to javafx.fxml, javafx.graphics;

    exports ProyectoFinal.Cliente.Chat;

    opens ProyectoFinal.Cliente.Chat to javafx.fxml, javafx.graphics;
}