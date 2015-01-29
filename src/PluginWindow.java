import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

/**
 *
 */
public class PluginWindow extends JFrame {

	private static final long serialVersionUID = 5811095093777366932L;

	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Singleton construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static PluginWindow instance = null;
	/**
	 * Singleton constructor.
	 * @return Either the present instance of <i>PluginWindow</i> or a new one.
	 */
	public static PluginWindow getInstance() {
		if (instance == null)
			return new PluginWindow();
		else
			return instance;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Fields for common use
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	private JTextField textFieldHostAddress;
	private JTextField textFieldPort;

	private CAMConnection camConnection;
	private final static Logger logger = Logger.getGlobal();

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI and action listener
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private PluginWindow() {
		getContentPane().setLayout(null);

		JLabel lblLeicaCamInterface = new JLabel("Leica CAM Interface");
		lblLeicaCamInterface.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblLeicaCamInterface.setBounds(10, 11, 210, 14);
		getContentPane().add(lblLeicaCamInterface);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelConnection.setBounds(10, 36, 230, 128);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblHostAddress = new JLabel("Host address");
		lblHostAddress.setBounds(10, 18, 83, 14);
		panelConnection.add(lblHostAddress);

		textFieldHostAddress = new JTextField();
		textFieldHostAddress.setText("127.0.0.1");
		textFieldHostAddress.setBounds(103, 11, 115, 28);
		panelConnection.add(textFieldHostAddress);
		textFieldHostAddress.setColumns(15);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(10, 57, 83, 14);
		panelConnection.add(lblPort);

		textFieldPort = new JTextField();
		textFieldPort.setBounds(103, 50, 115, 28);
		panelConnection.add(textFieldPort);
		textFieldPort.setText("8895");
		textFieldPort.setEditable(false);
		textFieldPort.setColumns(5);
		
		JScrollPane scrollPaneLogger = new JScrollPane();
		scrollPaneLogger.setBounds(10, 331, 764, 220);
		getContentPane().add(scrollPaneLogger);
		
		JEditorPane textAreaLogger = new JEditorPane();
		scrollPaneLogger.setViewportView(textAreaLogger);
		textAreaLogger.setContentType("text/html"); // set to HTML text to display different colors based on log level
		textAreaLogger.setText("<html>\n<head>\n</head>\n<body>\n</body>\n</html>");

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Connect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(10, 89, 89, 29);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					camConnection = new CAMConnection(getHostName(), getPort());
					camConnection.connect();
					camConnection.sendCAMCommand("Hallo CAM");
				} catch (UnknownHostException e) {
					logger.warning("Invalid host address format: " + e.getMessage());
				} catch (NumberFormatException e) {
					logger.warning("Invalid port number format: " + e.getMessage());
				}
			}
		});
		panelConnection.add(btnConnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Disconnect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(116, 89, 102, 29);
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (camConnection != null) {
					camConnection.disconnect();
				}
			}
		});
		panelConnection.add(btnDisconnect);
		logger.addHandler(new LogHandler(textAreaLogger));
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Getter functions for GUI elements
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private InetAddress getHostName() throws UnknownHostException {
		InetAddress hostAddress = null;
		hostAddress = InetAddress.getByName(textFieldHostAddress.getText());
		return hostAddress;
	}

	private int getPort() throws NumberFormatException {
		int port = 0;
		port = Integer.valueOf(textFieldPort.getText());
		return port;
	}
}
