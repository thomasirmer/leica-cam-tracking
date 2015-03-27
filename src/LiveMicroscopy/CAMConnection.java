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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This is a communication class with the CAM communication interface.
 * It manages the connection itself - establishing the connection and disconnecting properly.
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
	private static final int RECV_TIMEOUT_MS = 100;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Connection fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

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
			logger.info("Sent CAM command: " + command);
		} catch (InterruptedException e) {
			logger.warning("Interrupted while sending command: " + command + "\n> Error: " + e.getMessage() + " <");
		}
	}
	
	public static final int MOVE_ABSOLUTE = 0;
	public static final int MOVE_RELATIVE = 1;
	public static final int UNIT_METER = 0;
	public static final int UNIT_MICRONS = 1;
	
	/**
	 * 
	 * @param xPos
	 *            the new x-Position for the stage
	 * @param yPos
	 *            the new y-Position for the stage
	 * @param moveType
	 *            either absolute or relative (use the constants in this class:
	 *            MOVE_ABSOLUTE, MOVE_RELATIVE)
	 * @param unit
	 *            either meter or microns (use the constants in this class:
	 *            UNIT_METER, UNIT_MICRONS)
	 * @return
	 * @throws Exception
	 */
	public void moveStage(double xPos, double yPos, int type, int unit) throws Exception {
		String typeS;
		if (type == MOVE_ABSOLUTE)
			typeS = "absolute";
		else if (type == MOVE_RELATIVE)
			typeS = "relative";
		else
			throw new Exception("Wrong movement type. Possible values: MOVE_ABSOLUTE, MOVE_RELATIVE");

		String unitS;
		if (unit == UNIT_METER)
			unitS = "meter";
		else if (unit == UNIT_MICRONS)
			unitS = "microns";
		else
			throw new Exception("Wrong unit type. Possible values: UNIT_METER, UNIT_MICRONS");

		sendCAMCommand("/cli:" + Leica_CAM_Tracking.PLUGIN_NAME + " /app:matrix /sys:1 /cmd:setposition /typ:" + typeS
				+ " /dev:stage /unit:" + unitS + " /xpos:" + xPos + " /ypos:" + yPos);
	}
	
	public String getStageInfo() {
		sendCAMCommand("/cli:" + Leica_CAM_Tracking.PLUGIN_NAME + " /app:matrix /cmd:getinfo /dev:stage");
		return receiveCAMCommand();
	}

	public String getScanStatus() {
		sendCAMCommand("/cli:" + Leica_CAM_Tracking.PLUGIN_NAME + " /app:matrix /cmd:getinfo /dev:scanstatus");
		return receiveCAMCommand();
	}

	/**
	 * Return the last command in the receive buffer.
	 * 
	 * @return last received CAM command
	 */
	public String receiveCAMCommand() {
		String camCommand = "";
		try {
			camCommand = receiveBuffer.poll(150, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {}
		if (camCommand != null) {
			logger.info("Received CAM command: " + camCommand);
			return camCommand;
		}
		return "";
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
					int delayTime = 50; // delay for CAM Commands --> necessary for multiple CAM Commands (due to CAM documentation)
					Thread.sleep(delayTime);
				} catch (InterruptedException e) {} // No need to handle this
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
					String command = "";
					command = inFromCAM.readLine(); // realized with timeout at construction of inputStream
					if (!command.isEmpty()) {
						receiveBuffer.put(command);
						logger.info("Received CAM command --> inserted into receive buffer.");
					}
				} catch (IOException e) { // No log because IOException is just caused by readLine() timeout.
				} catch (InterruptedException e) {} // No need to handle this
			}
			
			logger.info(Thread.currentThread().getName() + " terminated.");
		}
	}
}