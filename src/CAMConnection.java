import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * 
 */
public class CAMConnection {

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Constants
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static final int RECV_TIMEOUT_MS = 125;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Connection fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private InetAddress host;
	private int port;
	private Socket clientSocket = null;
	private PrintWriter outToCAM;
	private BufferedReader inFromCAM;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Concurrent send-/receive buffer and threads
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private Thread sender;
	private Thread receiver;
	private volatile boolean sendRecvThreadsShouldRun;
	private BlockingQueue<String> sendBuffer;
	private BlockingQueue<String> receiveBuffer;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// other
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static final Logger logger = Logger.getGlobal();

	public CAMConnection(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Connects to host and creates input- and output-streams.
	 */
	public void connect() {
		try {
			clientSocket = new Socket(host, port);

			// output
			OutputStream outputStream = clientSocket.getOutputStream();
			outToCAM = new PrintWriter(outputStream);
			
			// input
			clientSocket.setSoTimeout(RECV_TIMEOUT_MS);
			InputStream inputStream = clientSocket.getInputStream();
			inFromCAM = new BufferedReader(new InputStreamReader(inputStream));
			
			// send-/receive threads
			sendBuffer = new LinkedBlockingQueue<String>();
			receiveBuffer = new LinkedBlockingQueue<String>();
			sendRecvThreadsShouldRun = true;
			sender = new Thread(new SenderThread());
			sender.setDaemon(true);
			sender.start();
			receiver = new Thread(new ReceiveThread());
			receiver.setDaemon(true);
			receiver.start();

			logger.info("Connection established to " + host.getHostAddress() + ":" + port);
		} catch (IOException e) {
			logger.severe("Connection failed: " + e.getMessage());
		}
	}

	/**
	 * Disconnects from client socket. Before closing the socket the error code
	 * '10054' will be send. This is needed by windows systems when closing JAVA
	 * socket. It will prompt windows to close and remove the JAVA based socket
	 * from the socket server list.
	 */
	public void disconnect() {
		if (!(clientSocket == null)) {
			try {
				sendRecvThreadsShouldRun = false;
				
				receiver.interrupt();
				sender.interrupt();

				receiver.join();
				sender.join();

				logger.info("Sending 'ErrorCode = 10054' --> needed for Windows socket to close properly.");
				outToCAM.println("ErrorCode = 10054");
				outToCAM.flush();

				clientSocket.close();
				clientSocket = null;

				logger.info("Connection closed.");
			} catch (IOException e) {
				logger.severe("Failed to close socket: " + e.getMessage());
			} catch (InterruptedException e) {
				logger.warning("Disconnect interrupted while waiting for send-/receive threads to finish: "
						+ e.getMessage());
			}
		} else {
			return;
		}
	}

	/**
	 * Inserts the given command to the send buffer.
	 * 
	 * @param command
	 *            The CAM command
	 */
	public void sendCAMCommand(String command) {
		try {
			sendBuffer.put(command);
		} catch (InterruptedException e) {
			logger.warning("Interrupted while sending command: " + command + ">Error: " + e.getMessage() + "<");
		}
	}

	/**
	 * Return the last command in the receive buffer.
	 * 
	 * @return last received CAM command
	 */
	public String receiveCAMCommand() {
		String command = "";

		try {
			command = receiveBuffer.take();
		} catch (InterruptedException e) {
			logger.warning("Interrupted while receiving command: " + e.getMessage());
		}

		return command;
	}
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// SenderThread
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * Wait for the send buffer. If any command is put into the send buffer this
	 * thread sends it to the output stream.
	 */
	private class SenderThread implements Runnable {

		@Override
		public void run() {
			logger.info("Starting sender thread.");

			while (sendRecvThreadsShouldRun) {
				try {
					String command = sendBuffer.take();
					outToCAM.println(command);
					outToCAM.flush();
					logger.info("Sent CAM command: " + command);
				} catch (InterruptedException e) {} // No need to handle this
			}
			
			logger.info("Terminating SenderThread.");
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// ReceiveThread
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * Check input stream every RECV_THREAD_SLEEP_TIME milliseconds and put
	 * received commands into receive buffer.
	 */
	private class ReceiveThread implements Runnable {

		@Override
		public void run() {
			logger.info("Starting receiver thread.");

			while (sendRecvThreadsShouldRun) {
				try {
					String command = "";
					command = inFromCAM.readLine(); // realized with timeout at construction of inputStream
					if (!command.isEmpty()) {
						receiveBuffer.put(command);
						logger.info("Received CAM command: " + command);
					}
				} catch (IOException e) { // No log because IOException is just caused by readLine() timeout.
				} catch (InterruptedException e) {} // No need to handle this
			}
			
			logger.info("Terminating ReceiveThread.");
		}
	}
}