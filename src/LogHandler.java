import java.util.logging.LogRecord;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class LogHandler extends java.util.logging.Handler {

	private JTextArea textArea;
	
	public LogHandler(JTextArea textArea) {
		this.textArea = textArea;
	}
	
	@Override
	public void close() throws SecurityException {}

	@Override
	public void flush() {}

	@Override
	public void publish(LogRecord record) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				textArea.setText(textArea.getText() + "[" + record.getLevel() + "] " + record.getMessage() + "\n");
			}
		});
	}

}
