package org.mcupdater.gui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ConsoleHandler extends Handler
{
	private final TextArea console;
	private final SimpleDateFormat sdFormat = new SimpleDateFormat("[HH:mm:ss.SSS] ");

	public ConsoleHandler(ConsolePane consolePane) {
		this.console = consolePane.console;
	}

	@Override
	public void publish(LogRecord record) {
		if (this.isLoggable(record)){
			final Calendar recordDate = Calendar.getInstance();
			recordDate.setTimeInMillis(record.getMillis());
			//LineStyle a = LineStyle.NORMAL;
			//if (record.getLevel() == Level.INFO) { a = LineStyle.NORMAL; }
			//if (record.getLevel() == Level.WARNING) { a = LineStyle.WARNING; }
			//if (record.getLevel() == Level.SEVERE) { a = LineStyle.ERROR; }
			//final LineStyle style = a;
			final Throwable thrown = record.getThrown();
			try {
				final String msg = sdFormat.format(recordDate.getTime()) + record.getMessage() + (thrown != null ? " (stacktrace in " + record.getLoggerName() + " log)" : "");
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						console.appendText(msg + "\n");
					}
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
