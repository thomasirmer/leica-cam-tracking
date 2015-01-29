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
 * 
 * @author Thomas Irmer
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

	/**
	 * Connects to host and creates input- and output-streams.
	 */
	public void connect(){
		try {
			clientSocket = new Socket(host, port);
			
			OutputStream outputStream = clientSocket.getOutputStream();
			camOutput = new DataOutputStream(outputStream);
			InputStream inputStream = clientSocket.getInputStream();
			camInput = new DataInputStream(inputStream);
			
			logger.info("Connection established to " + host.getHostAddress() + ":" + port);
		} catch (IOException e) {
			logger.severe("Connection failed: " + e.getMessage());
		}		
	}

	/**
	 * Disconnects from client socket. Before closing the socket the error code '10054' will be send.
	 * This is needed by windows systems when closing JAVA socket. It will prompt windows to close and remove
	 * the JAVA based socket from the socket server list.
	 */
	public void disconnect() {
		if (!(clientSocket == null)) {
			try {
				logger.info("Sending 'ErrorCode = 10054' --> needed for Windows socket to close properly.");
				
				camOutput.writeUTF("ErrorCode = 10054");
				camOutput.flush();
				camInput.close();
				camOutput.close();
				clientSocket.close();
				
				logger.info("Connection closed.");
			} catch (IOException e) {
				logger.severe("Failed to close socket: " + e.getMessage());
			}
		} else {
			return;
		}
	}
	
	/*
	 * >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	 * TODO: Implement listener thread who checks input stream every n (milli-)seconds and
	 * writes received CAM commands to buffer.
	 * 
	 * TODO: Implement sender thread who waits for a blocking buffer and sends CAM commands to CAM interface.
	 * <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	 */
}