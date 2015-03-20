package LiveMicroscopy;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JEditorPane;
import javax.swing.SwingWorker;

/**
 * This class presents the given log messages color formatted based on the log level in the given pane.
 * <br>
 * <br>info: green
 * <br>warning: orange
 * <br>severe: red
 * <br>other: black
 * 
 * @author Thomas Irmer
 */
public class LogHandler extends java.util.logging.Handler {

	private JEditorPane textArea;
	
	private BlockingQueue<LogRecord> logQueue;
	private LogWorker logDisplayer;

	public LogHandler(JEditorPane textArea) {
		this.textArea = textArea;
		logQueue = new LinkedBlockingQueue<LogRecord>();
		logDisplayer = new LogWorker();
		logDisplayer.execute();
	}

	@Override
	public synchronized void publish(LogRecord record) {
		try {
			logQueue.put(record);
		} catch (InterruptedException e) {}
	}
	
	private class LogWorker extends SwingWorker<Object, Object> {
		@Override
		protected Object doInBackground() throws Exception {
			while (true) {
				LogRecord record = logQueue.take();

				// change color based on log level
				String fontColor = "#000000"; // black
				if (record.getLevel() == Level.INFO)
					fontColor = "#006600"; // green
				else if (record.getLevel() == Level.WARNING)
					fontColor = "#FF9900"; // orange
				else if (record.getLevel() == Level.SEVERE)
					fontColor = "#990000"; // red

				// append log message to text field
				String newLogMessage = "<font face=\"Ubuntu\" size=\"3\" color=\"" + fontColor + "\">" + "["
						+ record.getLevel() + "] " + record.getMessage() + "</font><br>";
				publish(newLogMessage);
			}
		}
		
		@Override
		protected void process(List<Object> chunks) {
			for (Object newLogMessage : chunks) {
				String[] oldLoggerText = textArea.getText().split("</body>");
				textArea.setText(oldLoggerText[0] + (String)newLogMessage + "</body>" + oldLoggerText[1]);
			}
			
		}
		
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// other functions
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	@Override
	public synchronized void close() throws SecurityException {}
	
	@Override
	public synchronized void flush() {}
}
