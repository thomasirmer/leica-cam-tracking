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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * This is a communication class with the CAM communication interface. It
 * manages the connection itself - establishing the connection and disconnecting
 * properly. It also holds the methods to send and receive commands to / from
 * CAM interface. Send and receive are realized concurrent in daemon-threads.
 * 
 * @author Thomas Irmer
 */
public class CAMConnection {

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Singleton construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static CAMConnection instance = null;

	public static synchronized CAMConnection getInstance() {
		if (instance == null)
			instance = new CAMConnection();
		return instance;
	}

	// ------------------------------------------------------------------------

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Constants
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	// timeout for blocking receive functions
	private static final int RECV_TIMEOUT_MS = 100;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Connection fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private Socket clientSocket = null;
	private PrintWriter outToCAM = null;
	private BufferedReader inFromCAM = null;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Send-/receive buffer and threads
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private Thread sender;
	private Thread receiver;
	private volatile boolean sendRecvThreadsShouldRun;
	private BlockingQueue<String> sendBuffer = null;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Local fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static final Logger logger = Logger.getGlobal();

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Observer pattern
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	List<IMessageObserver> messageObservers = new ArrayList<IMessageObserver>();

	public void registerMessageObserver(IMessageObserver observer) {
		messageObservers.add(observer);
	}

	public void unregisterMessageObserver(IMessageObserver observer) {
		messageObservers.remove(observer);
	}

	private void informCAMObserver(String message) {
		for (IMessageObserver observer : messageObservers) {
			observer.receivedCAMCommand(message);
		}
	}
	
	private void informLogObserver(String logMessage) {
		for (IMessageObserver observer : messageObservers) {
			observer.receivedLogMessage(logMessage);
		}
	}

	/**
	 * Connects to host and creates input- and output-streams.
	 */
	public void connect(InetAddress host, int port) {
		try {
			logger.info("Connecting to " + host + " at port " + port + "...");

			clientSocket = new Socket(host, port);
			clientSocket.setSoTimeout(RECV_TIMEOUT_MS);

			createStreams();
			createSendRecvThreads();

			logger.info("...done!");
		} catch (IOException e) {
			logger.severe("...failed! " + e.getMessage());
		}
	}

	private void createSendRecvThreads() {
		// send-/receive threads
		sendBuffer = new LinkedBlockingQueue<String>();

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
		InputStream inputStream = clientSocket.getInputStream();
		inFromCAM = new BufferedReader(new InputStreamReader(inputStream));
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
				logger.warning("Disconnect interrupted while waiting for send-/receive threads to finish: " + e.getMessage());
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

	public boolean isConnected() {
		if (clientSocket != null)
			if (clientSocket.isConnected())
				return true;
		return false;

	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// CAM Commands
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * Inserts the given command to the send buffer.
	 * 
	 * @param command
	 *            The CAM command
	 */
	public void sendCAMCommand(String command) {
		try {
			sendBuffer.put(command);
			logger.info("Plugin >>> " + command);
		} catch (InterruptedException e) {
			logger.warning("Interrupted while sending command: " + command + "\n> Error: " + e.getMessage() + " <");
		}
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
			Thread.currentThread().setName("CAM Send Thread");
			logger.info("Starting " + Thread.currentThread().getName() + "...");

			while (sendRecvThreadsShouldRun) {
				try {
					String command = sendBuffer.take();
					outToCAM.println(command);
					outToCAM.flush();
					// delay for CAM Commands --> necessary for multiple CAM Commands (due to CAM documentation)
					int delayTime = 50; 
					Thread.sleep(delayTime);
				} catch (InterruptedException e) {
					// No need to handle this
				} 
			}

			logger.info(Thread.currentThread().getName() + " terminated.");
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
			Thread.currentThread().setName("CAM Receive Thread");
			logger.info("Starting " + Thread.currentThread().getName() + "...");

			while (sendRecvThreadsShouldRun) {
				try {
					// realized with timeout at construction of inputStream
					String command = "";
					command = inFromCAM.readLine(); 
					
					if (!command.isEmpty()) {
						logger.info("LAS X >>> " + command);
						informCAMObserver(command);
					}
				} catch (IOException e) {
					// No log because IOException is just caused by readLine() timeout.
				}
			}

			logger.info(Thread.currentThread().getName() + " terminated.");
		}
	}
}