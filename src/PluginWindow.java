import ij.IJ;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

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
	private CAMConnection camConnection;
	private JTextField textFieldHostAddress;
	private JTextField textFieldPortNumber;

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

		JLabel lblLeicaCamConnector = new JLabel("Leica CAM Connector");
		lblLeicaCamConnector.setBounds(10, 11, 102, 14);
		getContentPane().add(lblLeicaCamConnector);

		// Button >>Connect<<
		// Establish connection to given host at given port.
		// Abort if any error occurs
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					InetAddress hostAddress = getHostName();
					int port = getPort();
					camConnection = new CAMConnection(hostAddress, port);
					camConnection.connect();
				} catch (UnknownHostException e1) {
					IJ.showMessage("Error", "Invalid host address format: " + e1.getMessage());
				} catch (NumberFormatException e2) {
					IJ.showMessage("Error", "Invalid port number format: " + e2.getMessage());
				}
			}
		});
		btnConnect.setBounds(10, 89, 89, 23);
		getContentPane().add(btnConnect);

		// Button >>Disconnect<<
		// Disconnect from host.
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				camConnection.disconnect();
			}
		});
		btnDisconnect.setBounds(109, 89, 89, 23);
		getContentPane().add(btnDisconnect);

		JLabel lblHostAddress = new JLabel("Host address");
		lblHostAddress.setBounds(10, 39, 63, 14);
		getContentPane().add(lblHostAddress);
		
		textFieldHostAddress = new JTextField();
		textFieldHostAddress.setBounds(83, 36, 115, 20);
		getContentPane().add(textFieldHostAddress);
		textFieldHostAddress.setColumns(10);

		JLabel lblPortNo = new JLabel("Port no.");
		lblPortNo.setBounds(10, 64, 46, 14);
		getContentPane().add(lblPortNo);

		textFieldPortNumber = new JTextField();
		textFieldPortNumber.setBounds(83, 61, 115, 20);
		getContentPane().add(textFieldPortNumber);
		textFieldPortNumber.setColumns(10);
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// getter functions for gui elements
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private InetAddress getHostName() throws UnknownHostException {
		InetAddress hostAddress = null;
		String hostText = textFieldHostAddress.getText();

		if (hostText.isEmpty()) {
			throw new UnknownHostException("[empty]");
		}

		hostAddress = InetAddress.getByName(hostText);
		return hostAddress;
	}

	private int getPort() throws NumberFormatException {
		int port = 0;
		String portTxt = textFieldPortNumber.getText();

		if (portTxt.isEmpty()) {
			throw new NumberFormatException("[empty]");
		}

		port = Integer.valueOf(portTxt);
		return port;
	}
}
