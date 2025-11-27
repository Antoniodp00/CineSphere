module org.dam2.adp.cinesphere {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.sql;
    requires org.slf4j;
    requires org.apache.commons.csv;
    requires jbcrypt;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires java.logging;

    opens org.dam2.adp.cinesphere to javafx.fxml;
    opens org.dam2.adp.cinesphere.controller to javafx.fxml;
    opens org.dam2.adp.cinesphere.model to javafx.base;

    exports org.dam2.adp.cinesphere;
}
