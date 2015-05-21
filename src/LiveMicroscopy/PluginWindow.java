package LiveMicroscopy;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.NewImage;
import ij.gui.Roi;
import ij.process.ColorProcessor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * Represents the GUI and the button actions of the ImageJ-plugin. This class is
 * realized as a singleton.
 * 
 * @author Thomas Irmer
 */
public class PluginWindow extends JFrame {

	private static final long serialVersionUID = 5811095093777366932L;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Singleton construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static PluginWindow instance = null;

	/**
	 * Singleton constructor.
	 * 
	 * @return Either the present instance of <i>PluginWindow</i> or a new one.
	 */
	public static synchronized PluginWindow getInstance() {
		if (instance == null)
			instance = new PluginWindow();
		return instance;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Fields for common use
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private JTextField textFieldHostAddress;
	private JTextField textFieldPort;
	private BlockingQueue<File> imageQueue = new LinkedBlockingQueue<File>();

	private CAMConnection camConnection = new CAMConnection();
	private Logger logger = Logger.getGlobal();
	private LogHandler logHandler;
	private JTextField textFieldXPos;
	private JTextField textFieldYPos;
	private JTextField textFieldZPos;
	
	private JTextArea textAreaPipeline;

	private JLabel lblUnitX;
	private JLabel lblUnitY;
	private JLabel lblUnitZ;

	private ImageWindow imageWindow;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI and action listener
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private PluginWindow() {
		getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 11));
		setFont(new Font("Tahoma", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(PluginWindow.class.getResource("/icon/microscope_2.ico")));
		setResizable(false);
		setTitle("Leica CAM Tracking");
		getContentPane().setLayout(null);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Connection",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelConnection.setBounds(10, 11, 190, 130);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblHostAddress = new JLabel("Host IP");
		lblHostAddress.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblHostAddress.setBounds(10, 21, 35, 14);
		panelConnection.add(lblHostAddress);

		textFieldHostAddress = new JTextField();
		textFieldHostAddress.setFont(new Font("Consolas", Font.PLAIN, 11));
		textFieldHostAddress.setText("127.0.0.1");
		textFieldHostAddress.setBounds(55, 15, 125, 28);
		panelConnection.add(textFieldHostAddress);
		textFieldHostAddress.setColumns(15);

		JLabel lblPort = new JLabel("Port");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblPort.setBounds(10, 60, 35, 14);
		panelConnection.add(lblPort);

		textFieldPort = new JTextField();
		textFieldPort.setFont(new Font("Consolas", Font.PLAIN, 11));
		textFieldPort.setBounds(55, 54, 125, 28);
		panelConnection.add(textFieldPort);
		textFieldPort.setText("8895");
		textFieldPort.setColumns(5);

		JScrollPane scrollPaneLogging = new JScrollPane();
		scrollPaneLogging.setBounds(10, 575, 874, 286);
		getContentPane().add(scrollPaneLogging);

		JEditorPane textAreaLogging = new JEditorPane();
		textAreaLogging.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scrollPaneLogging.setViewportView(textAreaLogging);
		// set to HTML text to display different colors based on log level
		textAreaLogging.setContentType("text/html");
		textAreaLogging.setText("<html>\n<head>\n</head>\n<body>\n</body>\n</html>");

		logHandler = new LogHandler(textAreaLogging);
		logger.addHandler(logHandler);

		JLabel lblLogView = new JLabel("Log View");
		lblLogView.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scrollPaneLogging.setColumnHeaderView(lblLogView);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Connect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

//		JButton btnConnect = new JButton("Connect");
//		btnConnect.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				try {
//					camConnection.connect(getHostName(), getPort());
//					if (camConnection.isConnected()) {
//						indicateEstablishedConnection(true);
//						String returnedMessage = camConnection.receiveCAMCommand();
//						createImagingThread();
//					}
//				} catch (UnknownHostException e) {
//					logger.warning("Invalid host address format: " + e.getMessage());
//				} catch (NumberFormatException e) {
//					logger.warning("Invalid port number format: " + e.getMessage());
//				}
//			}
//		});
//		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 11));
//		btnConnect.setBounds(10, 93, 75, 29);
//		panelConnection.add(btnConnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Disconnect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (camConnection != null) {
					camConnection.disconnect();
					indicateEstablishedConnection(false);
				}
			}
		});
		btnDisconnect.setBounds(93, 93, 87, 29);
		panelConnection.add(btnDisconnect);
		btnDisconnect.setFont(new Font("Tahoma", Font.PLAIN, 11));

		JPanel panelCAMCommand = new JPanel();
		panelCAMCommand.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelCAMCommand.setBounds(10, 152, 190, 378);
		getContentPane().add(panelCAMCommand);
		panelCAMCommand.setLayout(null);

		JLabel lblCamCommand = new JLabel("CAM Commands");
		lblCamCommand.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCamCommand.setBounds(10, 10, 170, 12);
		panelCAMCommand.add(lblCamCommand);

		JTextArea textAreaCamCommand = new JTextArea();
		textAreaCamCommand.setLineWrap(true);
		textAreaCamCommand.setFont(new Font("Consolas", Font.PLAIN, 11));
		textAreaCamCommand.setBounds(10, 33, 170, 65);
		panelCAMCommand.add(textAreaCamCommand);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Send CAM command"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

//		JButton btnSendCommand = new JButton("Send command");
//		btnSendCommand.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				if (camConnection.isConnected()) {
//					String camCommand = textAreaCamCommand.getText();
//					if (CAMCommandParser.isValidCAMCommand(camCommand)) {
//						camConnection.sendCAMCommand(camCommand);
//						String returnedMessage = camConnection.receiveCAMCommand();
//					} else {
//						logger.severe("Wrong CAM Command structure.");
//					}
//				}
//			}
//		});
//		btnSendCommand.setFont(new Font("Tahoma", Font.PLAIN, 11));
//		btnSendCommand.setBounds(10, 109, 170, 23);
//		panelCAMCommand.add(btnSendCommand);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Get stage position"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

//		JButton btnGetStagePosition = new JButton("Get stage position");
//		btnGetStagePosition.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				if (camConnection.isConnected()) {
//					String returnedMessage = camConnection.getStageInfo();
//					Hashtable<String, String> camCommand = CAMCommandParser.parseStringToCAMCommand(returnedMessage);
//					setTextStagePosition(camCommand);
//				}
//			}
//		});
//		btnGetStagePosition.setFont(new Font("Tahoma", Font.PLAIN, 11));
//		btnGetStagePosition.setBounds(10, 143, 170, 23);
//		panelCAMCommand.add(btnGetStagePosition);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Get scan status"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

//		JButton btnGetScanStatus = new JButton("Get scan status");
//		btnGetScanStatus.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				if (camConnection.isConnected()) {
//					String returnedMessage = camConnection.getScanStatus();
//				}
//			}
//		});
//		btnGetScanStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
//		btnGetScanStatus.setBounds(10, 177, 170, 23);
//		panelCAMCommand.add(btnGetScanStatus);

		JLabel lblStagePosition = new JLabel("Stage Position");
		lblStagePosition.setBounds(10, 211, 68, 14);
		panelCAMCommand.add(lblStagePosition);

		JLabel lblX = new JLabel("x:");
		lblX.setBounds(10, 236, 10, 14);
		panelCAMCommand.add(lblX);

		JLabel lblY = new JLabel("y:");
		lblY.setBounds(10, 261, 10, 14);
		panelCAMCommand.add(lblY);

		JLabel lblZ = new JLabel("z:");
		lblZ.setBounds(10, 286, 10, 14);
		panelCAMCommand.add(lblZ);

		textFieldXPos = new JTextField();
		textFieldXPos.setText("test");
		textFieldXPos.setBounds(30, 233, 114, 20);
		panelCAMCommand.add(textFieldXPos);
		textFieldXPos.setColumns(10);

		textFieldYPos = new JTextField();
		textFieldYPos.setText("test");
		textFieldYPos.setBounds(30, 258, 114, 20);
		panelCAMCommand.add(textFieldYPos);
		textFieldYPos.setColumns(10);

		textFieldZPos = new JTextField();
		textFieldZPos.setText("test");
		textFieldZPos.setBounds(30, 283, 114, 20);
		panelCAMCommand.add(textFieldZPos);
		textFieldZPos.setColumns(10);

		lblUnitX = new JLabel("unit");
		lblUnitX.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitX.setBounds(154, 236, 26, 14);
		panelCAMCommand.add(lblUnitX);

		lblUnitY = new JLabel("unit");
		lblUnitY.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitY.setBounds(154, 261, 26, 14);
		panelCAMCommand.add(lblUnitY);

		lblUnitZ = new JLabel("unit");
		lblUnitZ.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUnitZ.setBounds(154, 286, 26, 14);
		panelCAMCommand.add(lblUnitZ);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Set position"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

//		JButton btnSetPosition = new JButton("Set position");
//		btnSetPosition.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				if (camConnection.isConnected()) {
//					try {
//						camConnection.moveStage(Double.parseDouble(textFieldXPos.getText()),
//								Double.parseDouble(textFieldYPos.getText()), CAMConnection.MOVE_ABSOLUTE,
//								CAMConnection.UNIT_METER);
//					} catch (NumberFormatException e) {
//						logger.severe(e.getMessage());
//					} catch (Exception e) {
//						logger.severe(e.getMessage());
//					}
//					String returnedMessage = camConnection.receiveCAMCommand();
//					Hashtable<String, String> camCommand = CAMCommandParser.parseStringToCAMCommand(returnedMessage);
//					setTextStagePosition(camCommand);
//				}
//			}
//		});
//		btnSetPosition.setBounds(10, 311, 170, 23);
//		panelCAMCommand.add(btnSetPosition);

		JLabel lblScanStatus = new JLabel("Scan status");
		lblScanStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblScanStatus.setBounds(124, 211, 56, 14);
		panelCAMCommand.add(lblScanStatus);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Execute Pipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnExecutePipeline = new JButton("Execute Pipeline");
		btnExecutePipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String camCommands = textAreaPipeline.getText();
				String[] splittedCamCommands = camCommands.split("\n");
				
				for (String command : splittedCamCommands) {
					if (!command.isEmpty() && !command.startsWith("#")) {
						if (CAMCommandParser.isValidCAMCommand(command)) {
							camConnection.sendCAMCommand(command);
						} else {
							logger.warning("Invalid CAM Command");
						}
					}
				}
			}
		});
		btnExecutePipeline.setBounds(10, 345, 170, 23);
		panelCAMCommand.add(btnExecutePipeline);

		JScrollPane scrollPanePipeline = new JScrollPane();
		scrollPanePipeline.setBounds(210, 309, 674, 221);
		getContentPane().add(scrollPanePipeline);

		textAreaPipeline = new JTextArea();
		scrollPanePipeline.setViewportView(textAreaPipeline);

		JLabel lblCamCommandPipeline = new JLabel("CAM Command Pipeline");
		scrollPanePipeline.setColumnHeaderView(lblCamCommandPipeline);
		
		JButton btnClearLog = new JButton("Clear Log");
		btnClearLog.setBounds(10, 541, 89, 23);
		getContentPane().add(btnClearLog);
		
		JPanel panel = new JPanel();
		panel.setBounds(210, 11, 287, 287);
		getContentPane().add(panel);
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textAreaLogging.setText("");
			}
		});
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Setter functions for GUI elements
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private void setTextStagePosition(Hashtable<String, String> camCommand) {
		if (camCommand.containsKey("app") && camCommand.containsKey("dev") && camCommand.containsKey("info_for")) {
			if (camCommand.get("app").equals("matrix") && camCommand.get("dev").equals("stage")
					&& camCommand.get("info_for").equals(Leica_CAM_Tracking.PLUGIN_NAME)) {
				textFieldXPos.setText(camCommand.get("xpos"));
				textFieldYPos.setText(camCommand.get("ypos"));
				textFieldZPos.setText(camCommand.get("zpos"));
				lblUnitX.setText(camCommand.get("unit"));
				lblUnitY.setText(camCommand.get("unit"));
				lblUnitZ.setText(camCommand.get("unit"));
			}
		}
	}

	private void indicateEstablishedConnection(boolean connected) {
		if (connected) {
			textFieldHostAddress.setEnabled(false);
			textFieldHostAddress.setBackground(new Color(102, 255, 000));
			textFieldPort.setEnabled(false);
			textFieldPort.setBackground(new Color(102, 255, 000));
		} else {
			textFieldHostAddress.setEnabled(true);
			textFieldHostAddress.setBackground(Color.WHITE);
			textFieldPort.setEnabled(true);
			textFieldPort.setBackground(Color.WHITE);
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Getter functions for GUI elements
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

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Image-Presenter-Thread
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private void createImagingThread() {
		Thread imagePresenter = new Thread(new ImageLoaderThread());
		imagePresenter.setDaemon(true);
		imagePresenter.start();
	}
	
	private class ImageLoaderThread implements Runnable {

		// TODO: Dieser Thread soll an einer BlockingQueue darauf warten, dass
		// neue Bilder angeboten werden.
		// Die Bilder werden dann in eine Queue geschoben, wenn ein CAM Command
		// empfangen wird, dass den Pfad zu diesem Bild mitliefert.
		// Immer wenn ein neues Bild angeboten wird, aktualisiert dieser Thread
		// die ImageView.

		@Override
		public void run() {
			Thread.currentThread().setName("Image Loader Thread");

			try {
				//CellTracking cellTracking = new CellTracking();

				File 		imageFile			= null;
				ImagePlus 	imageFromMicroscope = null;
				ImageCanvas imageCanvas			= null;
				ImageWindow imageWindow			= null;
				
				while (true) {				
					imageFile 			= imageQueue.take();					
					imageFromMicroscope = new ImagePlus(imageFile.getAbsolutePath());
					
					if (imageWindow == null || !imageWindow.isVisible()) {
						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
						int screenWidth = screenSize.width;
						int screenHeight = screenSize.height;
						
						imageWindow = new ImageWindow(imageFromMicroscope);
						imageWindow.setBounds(210, 11, 287, 287);
//						instance.getContentPane().add(new JImageWindow(imageWindow));
						//imageWindow.setBounds(screenWidth / 2 - 600, screenHeight / 2 - 400, 1200, 800);
						imageWindow.getCanvas().fitToWindow();
					} else {
						imageWindow.setImage(imageFromMicroscope);
					}		
					
					imageWindow.setVisible(true);
				}
			} catch (InterruptedException e) {
			}
		}

	}
}
