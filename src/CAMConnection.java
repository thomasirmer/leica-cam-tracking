import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import ij.IJ;

/**
 * 
 */

/**
 * @author Thomas Irmer
 *
 */
public class CAMConnection {

	private InetAddress host;
	private int port;

	private Socket client = null;

	public CAMConnection(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public void connect() {
		// Establish connection to given host and create streams
		try {
			client = new Socket(host, port);
			
			OutputStream outputStream = client.getOutputStream();
			DataOutputStream camOutput = new DataOutputStream(outputStream);
			InputStream inputStream = client.getInputStream();
			DataInputStream camInput = new DataInputStream(inputStream);
			
			IJ.showMessage("Connection to host " + host.getHostAddress() + ":" + port + " established."); // DEBUG
		} catch (IOException e) {
			IJ.showMessage("Error", "Failed to connect to " + host.getHostAddress() + ":" + port + "\n" + e.getMessage());
		}
	}

	public void disconnect() {
		// Disconnect if necessary
		if (client == null) {
			return;
		}

		try {
			client.close();
		} catch (IOException e) {
			IJ.showMessage("Error", "Failed to close socket" + "\n" + e.getMessage());
		}
	}
}