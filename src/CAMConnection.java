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
	private DataOutputStream camOutput;
	private DataInputStream camInput;

	public CAMConnection(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public void connect() throws IOException {
		// Establish connection to given host and create streams
		client = new Socket(host, port);

		OutputStream outputStream = client.getOutputStream();
		camOutput = new DataOutputStream(outputStream);
		InputStream inputStream = client.getInputStream();
		camInput = new DataInputStream(inputStream);
	}

	public void disconnect() {
		if (!(client == null)) { // Disconnect only if necessary
			try {
				camOutput.writeUTF("[CLIENT] Goodbye!");
				client.close();
			} catch (IOException e) {
				IJ.showMessage("Error", "Failed to close socket" + "\n" + e.getMessage());
			}
		} else return;
	}
}