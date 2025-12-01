package org.dam2.adp.cinesphere.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Un formateador de logs personalizado para java.util.logging que añade colores
 * a la salida de la consola según el nivel del log.
 * - INFO: Blanco
 * - WARNING: Naranja
 * - SEVERE: Rojo
 */
public class CustomFormatter extends Formatter {

    // Códigos de color ANSI
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();


        if (record.getLevel() == Level.SEVERE) {
            sb.append(ANSI_RED);
        } else if (record.getLevel() == Level.WARNING) {
            sb.append(ANSI_YELLOW);
        } else {
            sb.append(ANSI_WHITE);
        }


        sb.append(dateFormat.format(new Date(record.getMillis())));
        sb.append(" ");


        sb.append(String.format("[%s] ", record.getLevel().getName()));


        if (record.getSourceClassName() != null) {
            sb.append(record.getSourceClassName());
            if (record.getSourceMethodName() != null) {
                sb.append(".").append(record.getSourceMethodName());
            }
        }
        sb.append(" - ");


        sb.append(formatMessage(record));
        sb.append(System.lineSeparator());


        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        }


        sb.append(ANSI_RESET);

        return sb.toString();
    }
}
