import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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

	private JPanel panelImageView;
	private BlockingQueue<File> imageQueue;

	private CAMConnection camConnection;
	private Logger logger = Logger.getGlobal();
	private LogHandler logHandler;
	private JTextField textFieldImagePath;

	private JLabel lblXposition;
	private JLabel lblYposition;
	private JLabel lblZposition;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI and action listener
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private PluginWindow() {
		getContentPane().setFont(new Font("Tahoma", Font.PLAIN, 11));
		setFont(new Font("Tahoma", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(PluginWindow.class.getResource("/icon/microscope_3.ico")));
		setResizable(false);
		setTitle("Leica CAM Tracking");
		getContentPane().setLayout(null);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Connection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
		textFieldPort.setEditable(false);
		textFieldPort.setColumns(5);

		JScrollPane scrollPaneLogging = new JScrollPane();
		scrollPaneLogging.setBounds(210, 701, 1054, 199);
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

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					camConnection = new CAMConnection(getHostName(), getPort());
					camConnection.connect();
					if (camConnection.isConnected()) {
						camConnection.sendCAMCommand("Hallo CAM");
					}
				} catch (UnknownHostException e) {
					logger.warning("Invalid host address format: " + e.getMessage());
				} catch (NumberFormatException e) {
					logger.warning("Invalid port number format: " + e.getMessage());
				}
			}
		});
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnConnect.setBounds(10, 93, 75, 29);
		panelConnection.add(btnConnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Disconnect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (camConnection != null) {
					camConnection.disconnect();
				}
			}
		});
		btnDisconnect.setBounds(93, 93, 87, 29);
		panelConnection.add(btnDisconnect);
		btnDisconnect.setFont(new Font("Tahoma", Font.PLAIN, 11));

		panelImageView = new JPanel();
		panelImageView.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Image View", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelImageView.setBounds(210, 11, 1054, 679);
		getContentPane().add(panelImageView);
		panelImageView.setLayout(null);

		JPanel panelPathSelection = new JPanel();
		panelPathSelection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelPathSelection.setBounds(10, 278, 190, 97);
		getContentPane().add(panelPathSelection);
		panelPathSelection.setLayout(null);

		textFieldImagePath = new JTextField();
		textFieldImagePath.setFont(new Font("Tahoma", Font.PLAIN, 11));
		textFieldImagePath.setEditable(false);
		textFieldImagePath.setBounds(10, 34, 170, 20);
		panelPathSelection.add(textFieldImagePath);
		textFieldImagePath.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Select path"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		// TODO: Möglicherweise nicht notwendig, weil Pfad per CAM Command
		// übermittelt wird.
		JButton buttonSelectPath = new JButton("Choose");
		buttonSelectPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Choose the path where the microscope will save its images...");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnValue = chooser.showDialog(PluginWindow.instance, "Select");

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File directory = chooser.getSelectedFile();
					textFieldImagePath.setText(directory.getAbsolutePath());

					// TODO: DEBUG
					imageQueue = new LinkedBlockingQueue<File>();
					File[] files = directory.listFiles();
					for (File file : files) {
						try {
							if (file.getAbsolutePath().endsWith("tif") || file.getAbsolutePath().endsWith("png") || file.getAbsolutePath().endsWith("jpg"))
								imageQueue.put(file);
						} catch (InterruptedException e1) {
						}
					}

					Thread imagePresenter = new Thread(new ImageLoaderThread());
					imagePresenter.setDaemon(true);
					imagePresenter.start();
					// END: DEBUG
				}
			}
		});
		buttonSelectPath.setFont(new Font("Tahoma", Font.PLAIN, 11));
		buttonSelectPath.setBounds(10, 65, 71, 23);
		panelPathSelection.add(buttonSelectPath);

		JLabel lblImagePath = new JLabel("Image path (maybe unnecessary)");
		lblImagePath.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblImagePath.setBounds(10, 11, 170, 12);
		panelPathSelection.add(lblImagePath);

		JPanel panelCAMCommand = new JPanel();
		panelCAMCommand.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelCAMCommand.setBounds(10, 386, 190, 514);
		getContentPane().add(panelCAMCommand);
		panelCAMCommand.setLayout(null);

		JLabel lblCamCommand = new JLabel("CAM Commands");
		lblCamCommand.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblCamCommand.setBounds(10, 10, 170, 12);
		panelCAMCommand.add(lblCamCommand);

		JTextArea textAreaCamCommand = new JTextArea();
		textAreaCamCommand.setLineWrap(true);
		textAreaCamCommand.setFont(new Font("Consolas", Font.PLAIN, 11));
		textAreaCamCommand.setBounds(10, 33, 170, 129);
		panelCAMCommand.add(textAreaCamCommand);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Send CAM command"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnSendCommand = new JButton("Send command");
		btnSendCommand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (camConnection.isConnected()) {
					String camCommand = textAreaCamCommand.getText();
					if (CAMCommandParser.isValidCAMCommand(camCommand))
						camConnection.sendCAMCommand(camCommand);
					else
						logger.severe("Wrong CAM Command structure.");
				}
			}
		});
		btnSendCommand.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnSendCommand.setBounds(10, 173, 123, 23);
		panelCAMCommand.add(btnSendCommand);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Get stage position"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnGetStagePosition = new JButton("Get stage position");
		btnGetStagePosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (camConnection.isConnected()) {
					camConnection.sendCAMCommand(CAMCommandParser.getCommandStageInfo());
					String returnedMessage = camConnection.receiveCAMCommand();
					Hashtable<String,String> camCommand = CAMCommandParser.parseStringToCAMCommand(returnedMessage);
					setTextStagePosition(camCommand);
				}
			}
		});
		btnGetStagePosition.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnGetStagePosition.setBounds(10, 207, 123, 23);
		panelCAMCommand.add(btnGetStagePosition);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Get scan status"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnGetScanStatus = new JButton("Get scan status");
		btnGetScanStatus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (camConnection.isConnected()) {
					camConnection.sendCAMCommand(CAMCommandParser.getCommandScanStatus());
				}
			}
		});
		btnGetScanStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnGetScanStatus.setBounds(10, 241, 123, 23);
		panelCAMCommand.add(btnGetScanStatus);
		
		JLabel lblStagePosition = new JLabel("Stage Position");
		lblStagePosition.setBounds(10, 275, 68, 14);
		panelCAMCommand.add(lblStagePosition);
		
		JLabel lblX = new JLabel("x:");
		lblX.setBounds(10, 300, 10, 14);
		panelCAMCommand.add(lblX);
		
		JLabel lblY = new JLabel("y:");
		lblY.setBounds(10, 325, 10, 14);
		panelCAMCommand.add(lblY);
		
		JLabel lblZ = new JLabel("z:");
		lblZ.setBounds(10, 350, 10, 14);
		panelCAMCommand.add(lblZ);
		
		lblXposition = new JLabel("xPosition");
		lblXposition.setBounds(30, 300, 103, 14);
		panelCAMCommand.add(lblXposition);
		
		lblYposition = new JLabel("yPosition");
		lblYposition.setBounds(30, 325, 103, 14);
		panelCAMCommand.add(lblYposition);
		
		lblZposition = new JLabel("zPosition");
		lblZposition.setBounds(30, 350, 103, 14);
		panelCAMCommand.add(lblZposition);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Screening Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(10, 152, 190, 115);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblResolution = new JLabel("Resolution");
		lblResolution.setBounds(10, 26, 50, 14);
		panel.add(lblResolution);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(85, 24, 95, 20);
		panel.add(comboBox);
		
		JLabel lblImagesSec = new JLabel("Images / sec.");
		lblImagesSec.setBounds(10, 58, 65, 14);
		panel.add(lblImagesSec);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setBounds(85, 54, 95, 20);
		panel.add(comboBox_1);
		
		JButton btnStartTracking = new JButton("Start Tracking");
		btnStartTracking.setBounds(10, 83, 170, 23);
		panel.add(btnStartTracking);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnConnection = new JMenu("Connection");
		menuBar.add(mnConnection);
		
		JMenuItem mntmSaveSettings = new JMenuItem("Save Settings...");
		mnConnection.add(mntmSaveSettings);
		
		JMenuItem mntmLoadSettings = new JMenuItem("Load Settings...");
		mnConnection.add(mntmLoadSettings);
	}
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Setter functions for GUI elements
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	private void setTextStagePosition(Hashtable<String, String> camCommand) {
		if (camCommand.get("app").equals("matrix") &&
				camCommand.get("dev").equals("stage") &&
				camCommand.get("info_for").equals(Leica_CAM_Tracking.PLUGIN_NAME)) {
			lblXposition.setText(camCommand.get("xpos") + " " + camCommand.get("unit"));
			lblYposition.setText(camCommand.get("ypos") + " " + camCommand.get("unit"));
			lblZposition.setText(camCommand.get("zpos") + " " + camCommand.get("unit"));
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

	private class ImageLoaderThread implements Runnable {

		// TODO: Dieser Thread soll an einer BlockingQueue darauf warten, dass
		// neue Bilder angeboten werden.
		// Die Bilder werden dann in eine Queue geschoben, wenn ein CAM Command
		// empfangen wird, dass den Pfad zu diesem Bild mitliefert.
		// Immer wenn ein neues Bild angeboten wird, aktualisiert dieser Thread
		// die ImageView.

		@Override
		public void run() {
			try {
				CellTracking cellTracking = new CellTracking();
				
				while (true) {
					File file = imageQueue.take();
					BufferedImage image = ImageIO.read(file);
					panelImageView.paintComponents(panelImageView.getGraphics());
					panelImageView.getGraphics().drawImage(image, 0, 0, null);
					
					// TODO: Cell tracking and stage movement calculation comes here!
					cellTracking.track(image); // or something ^^
					
					// END _TODO
					
					Thread.sleep(2500); // DEBUG
				}
			} catch (InterruptedException | IOException e) {
			}
		}

	}
}
