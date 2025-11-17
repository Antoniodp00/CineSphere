module org.dam2.adp.cinesphere {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires org.slf4j;
    requires java.sql;
    requires commons.configuration;

    opens org.dam2.adp.cinesphere to javafx.fxml;
    exports org.dam2.adp.cinesphere;
}