package LiveMicroscopy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This is a communication class with the CAM communication interface.
 * It manages the connection itself like establishing the connection and disconnecting properly.
 * It also holds the methods to send and receive commands to / from CAM interface.
 * Send and receive are realized concurrent in daemon-threads.
 * 
 * @author Thomas Irmer
 */
public class CAMConnection {

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Constants
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	// timeout for blocking receive functions
	private static final int RECV_TIMEOUT_MS = 500;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Connection fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private InetAddress host;
	private int port;
	private Socket clientSocket 		= null;
	private PrintWriter outToCAM 		= null;
	private BufferedReader inFromCAM 	= null;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Send-/receive buffer and threads
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private Thread sender;
	private Thread receiver;
	private volatile boolean sendRecvThreadsShouldRun;
	private BlockingQueue<String> sendBuffer	= null;
	private BlockingQueue<String> receiveBuffer = null;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Local fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static final Logger logger = Logger.getGlobal();

	/**
	 * Connects to host and creates input- and output-streams.
	 */
	public synchronized void connect(InetAddress host, int port) {
		try {
			clientSocket = new Socket(host, port);
			
			createStreams();
			createSendRecvThreads();

			logger.info("Connection established to " + host.getHostAddress() + ":" + port);
		} catch (IOException e) {
			logger.severe("Connection failed: " + e.getMessage());
		}
	}

	private void createSendRecvThreads() {
		// send-/receive threads
		sendBuffer 		= new LinkedBlockingQueue<String>();
		receiveBuffer 	= new LinkedBlockingQueue<String>();
		
		// loop condition for threads
		sendRecvThreadsShouldRun = true;
		
		sender = new Thread(new SenderThread());
		sender.setDaemon(true);
		sender.start();
		
		receiver = new Thread(new ReceiveThread());
		receiver.setDaemon(true);
		receiver.start();
	}

	private void createStreams() throws IOException, SocketException {
		// output
		OutputStream outputStream = clientSocket.getOutputStream();
		outToCAM = new PrintWriter(outputStream);
		
		// input
		clientSocket.setSoTimeout(RECV_TIMEOUT_MS);
		InputStream inputStream = clientSocket.getInputStream();
		inFromCAM = new BufferedReader(new InputStreamReader(inputStream));
	}

	/**
	 * Disconnects from client socket. Before closing the socket the error code
	 * '10054' will be send. This is needed by windows systems when closing JAVA
	 * socket. It will prompt windows to close and remove the JAVA based socket
	 * from the socket server list.
	 */
	public synchronized void disconnect() {
		if (!(clientSocket == null)) {
			try {
				stopSendRecvThreads();

				// last message (manually)
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

	private void stopSendRecvThreads() throws InterruptedException {
		// loop condition for threads
		sendRecvThreadsShouldRun = false;
		
		// kill threads
		receiver.interrupt();
		sender.interrupt();

		// wait for threads to die
		receiver.join();
		sender.join();
	}

	public synchronized boolean isConnected() {
		if (clientSocket != null)
			if (clientSocket.isConnected())
				return true;
		return false;

	}

	/**
	 * Inserts the given command to the send buffer.
	 * 
	 * @param command
	 *            The CAM command
	 */
	public synchronized void sendCAMCommand(String command) {
		try {
			sendBuffer.put(command);
		} catch (InterruptedException e) {
			logger.warning("Interrupted while sending command: " + command + "\n> Error: " + e.getMessage() + " <");
		}
	}

	/**
	 * Return the last command in the receive buffer.
	 * 
	 * @return last received CAM command
	 */
	public synchronized String receiveCAMCommand() {
		String camCommand = "";
		try {
			camCommand = receiveBuffer.take();
		} catch (InterruptedException e) {
			logger.warning("Interrupted while receiving command: " + "\n> Error: " + e.getMessage() + " <");
		}

		return camCommand;
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
					int delayTime = 50; // delay for CAM Commands --> necessary for multiple CAM Commands (due to CAM documentation)
					Thread.sleep(delayTime);
				} catch (InterruptedException e) {} // No need to handle this
			}
			
			logger.info("SenderThread terminated.");
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
			
			logger.info("ReceiveThread terminated.");
		}
	}
}