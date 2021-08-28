package org.mcupdater.gui.javafx.components;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler
{
    private final SimpleDateFormat sdFormat = new SimpleDateFormat("[HH:mm:ss.SSS] ");
    private final ConsolePane consolePane;

    public ConsoleHandler(ConsolePane consolePane) {
        this.consolePane = consolePane;
    }

    @Override
    public void publish(LogRecord record) {
        if (this.isLoggable(record)){
            final Calendar recordDate = Calendar.getInstance();
            recordDate.setTimeInMillis(record.getMillis());
            try {
                final Throwable thrown = record.getThrown();
                final String msg = sdFormat.format(recordDate.getTime()) + record.getMessage() + (thrown != null ? " (stacktrace in " + record.getLoggerName() + " log)" : "");
                Platform.runLater(() -> {
                    consolePane.appendEntry(msg, record.getLevel());
                    //console.appendText(msg + "\n");
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
}