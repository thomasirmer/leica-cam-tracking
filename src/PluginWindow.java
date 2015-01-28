import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import org.eclipse.wb.swing.FocusTraversalOnArray;

/**
 * 
 */

/**
 * @author Thomas Irmer
 *
 */
public class PluginWindow extends JFrame {

	private static final long serialVersionUID = 5811095093777366932L;

	private static PluginWindow instance = null;
	private JTextField textFieldHostAddress;
	private JTextField textFieldPort;
	private JTextArea textAreaLogger;

	private CAMConnection camConnection;

	private final static Logger logger = Logger.getGlobal();

	// Replaces the constructor to provide a singleton interface.
	// This method returns either the existing instance or creates it.
	public static PluginWindow getInstance() {
		if (instance == null)
			return new PluginWindow();
		else
			return instance;
	}

	// Constructor is set to private because this class is a singleton.
	// To get the instance of this class call getInstance().
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
		textFieldHostAddress.setBounds(103, 11, 115, 28);
		panelConnection.add(textFieldHostAddress);
		textFieldHostAddress.setColumns(10);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(10, 57, 83, 14);
		panelConnection.add(lblPort);

		textFieldPort = new JTextField();
		textFieldPort.setBounds(103, 50, 115, 28);
		panelConnection.add(textFieldPort);
		textFieldPort.setText("8895");
		textFieldPort.setEditable(false);
		textFieldPort.setColumns(10);
		
		JScrollPane scrollPaneLogger = new JScrollPane();
		scrollPaneLogger.setBounds(10, 331, 764, 220);
		getContentPane().add(scrollPaneLogger);

		textAreaLogger = new JTextArea();
		scrollPaneLogger.setViewportView(textAreaLogger);

		// Button >>Connect<<
		// Establish connection to given host at given port.
		// Abort if any error occurs
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(10, 89, 89, 29);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					camConnection = new CAMConnection(getHostName(), getPort());
					camConnection.connect();
				} catch (UnknownHostException e1) {
					logger.warning("Invalid host address format: " + e1.getMessage());
				} catch (NumberFormatException e2) {
					logger.warning("Invalid port number format: " + e2.getMessage());
				} catch (IOException e3) {
					logger.warning("Connection failed: " + e3.getMessage());
				}
			}
		});
		panelConnection.add(btnConnect);

		// Button >>Disconnect<<
		// Disconnect from host.
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
		panelConnection.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[] { textFieldHostAddress, btnConnect, btnDisconnect }));	
		
		// Set up logger
		LogHandler logHandler = new LogHandler(textAreaLogger);
		logger.addHandler(logHandler);
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Getter functions for gui elements
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
