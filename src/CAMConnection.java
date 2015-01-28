import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

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

	private Socket clientSocket = null;
	private DataOutputStream camOutput;
	private DataInputStream camInput;
	
	private static final Logger logger = Logger.getGlobal();

	public CAMConnection(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public void connect() throws IOException {
		// Establish connection to given host and create streams
		clientSocket = new Socket(host, port);

		OutputStream outputStream = clientSocket.getOutputStream();
		camOutput = new DataOutputStream(outputStream);
		InputStream inputStream = clientSocket.getInputStream();
		camInput = new DataInputStream(inputStream);
		
		logger.info("Connection established to " + host.getHostAddress() + ":" + port);
	}

	public void disconnect() {
		if (!(clientSocket == null)) { // Disconnect only if necessary
			try {
				logger.info("Sending 'ErrorCode = 10054' --> needed for Windows socket to close properly.");
				camOutput.writeUTF("ErrorCode = 10054"); // needed for windows socket to close connection to java socket
				camOutput.flush();
				
				camInput.close();
				camOutput.close();
				
				clientSocket.close();
				logger.info("Connection closed.");
			} catch (IOException e) {
				logger.severe("Failed to close socket: " + e.getMessage());
			}
		} else return;
	}
}