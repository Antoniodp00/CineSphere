package org.dam2.adp.cinesphere.util;

import javafx.scene.control.Alert;

public class AlertUtils {

    public static void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.show();
    }

    public static void error(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setContentText(msg);
        a.show();
    }
}

