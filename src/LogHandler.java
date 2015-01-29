import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

/**
 * This class presents the given log messages color formatted based on the log level in the given pane.
 * <br>
 * <br>info: green
 * <br>warning: orange
 * <br>severe: red
 * <br>other: black
 */
public class LogHandler extends java.util.logging.Handler {

	private JEditorPane textArea;

	public LogHandler(JEditorPane textArea) {
		this.textArea = textArea;
	}

	@Override
	public void publish(LogRecord record) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// get current text in text field and split by </body> tag to append log message to the end
				String[] oldLoggerText = textArea.getText().split("</body>");
				
				// change color based on log level
				String fontColor = "#000000"; // black
				if (record.getLevel() == Level.INFO)
					fontColor = "#006600"; // green
				else if (record.getLevel() == Level.WARNING)
					fontColor = "#FF9900"; // orange
				else if (record.getLevel() == Level.SEVERE)
					fontColor = "#990000"; // red
				
				// append log message to text field
				String newLogMessage = "<font face=\"Tahoma\" size=\"3\" color=\"" + fontColor + "\">" + "["
						+ record.getLevel() + "] " + record.getMessage() + "</font><br>";
				textArea.setText(oldLoggerText[0] + newLogMessage + "</body>" + oldLoggerText[1]);
			}
		});
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Unnecessary functions
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	@Override
	public void close() throws SecurityException {}
	
	@Override
	public void flush() {}
}
