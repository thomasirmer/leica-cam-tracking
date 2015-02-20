import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

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
		setIconImage(Toolkit.getDefaultToolkit().getImage(PluginWindow.class.getResource("/icon/microscope_2.ico")));
		setResizable(false);
		setTitle("Leica CAM Tracking");
		getContentPane().setLayout(null);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelConnection.setBounds(10, 11, 190, 125);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblHostAddress = new JLabel("Host IP");
		lblHostAddress.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblHostAddress.setBounds(10, 18, 35, 14);
		panelConnection.add(lblHostAddress);

		textFieldHostAddress = new JTextField();
		textFieldHostAddress.setFont(new Font("Consolas", Font.PLAIN, 11));
		textFieldHostAddress.setText("127.0.0.1");
		textFieldHostAddress.setBounds(55, 12, 125, 28);
		panelConnection.add(textFieldHostAddress);
		textFieldHostAddress.setColumns(15);

		JLabel lblPort = new JLabel("Port");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblPort.setBounds(10, 57, 35, 14);
		panelConnection.add(lblPort);

		textFieldPort = new JTextField();
		textFieldPort.setFont(new Font("Consolas", Font.PLAIN, 11));
		textFieldPort.setBounds(55, 51, 125, 28);
		panelConnection.add(textFieldPort);
		textFieldPort.setText("8895");
		textFieldPort.setEditable(false);
		textFieldPort.setColumns(5);

		JScrollPane scrollPaneLogging = new JScrollPane();
		scrollPaneLogging.setBounds(210, 701, 1054, 220);
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
		btnConnect.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnConnect.setBounds(10, 89, 75, 29);
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
		panelConnection.add(btnConnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Disconnect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(93, 89, 87, 29);
		panelConnection.add(btnDisconnect);
		btnDisconnect.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (camConnection != null) {
					camConnection.disconnect();
				}
			}
		});

		panelImageView = new JPanel();
		panelImageView.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelImageView.setBounds(210, 11, 1054, 679);
		getContentPane().add(panelImageView);
		panelImageView.setLayout(null);

		JPanel panelPathSelection = new JPanel();
		panelPathSelection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelPathSelection.setBounds(10, 147, 190, 97);
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
		buttonSelectPath.setFont(new Font("Tahoma", Font.PLAIN, 11));
		buttonSelectPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
							if (file.getAbsolutePath().endsWith("jpg"))
								imageQueue.put(file);
						} catch (InterruptedException e1) {
						}
					}

					Thread imagePresenter = new Thread(new ImagePresenterThread());
					imagePresenter.setDaemon(true);
					imagePresenter.start();
					// END: DEBUG
				}
			}
		});
		buttonSelectPath.setBounds(10, 65, 71, 23);
		panelPathSelection.add(buttonSelectPath);

		JLabel lblImagePath = new JLabel("Image path");
		lblImagePath.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblImagePath.setBounds(10, 11, 170, 12);
		panelPathSelection.add(lblImagePath);

		JPanel panelCAMCommand = new JPanel();
		panelCAMCommand.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelCAMCommand.setBounds(10, 255, 190, 666);
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
			public void actionPerformed(ActionEvent e) {
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
			public void actionPerformed(ActionEvent arg0) {
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
			public void actionPerformed(ActionEvent e) {
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

	private class ImagePresenterThread implements Runnable {

		// TODO: Dieser Thread soll an einer BlockingQueue darauf warten, dass
		// neue Bilder angeboten werden.
		// Die Bilder werden dann in eine Queue geschoben, wenn ein CAM Command
		// empfangen wird, dass den Pfad zu diesem Bild mitliefert.
		// Immer wenn ein neues Bild angeboten wird, aktualisiert dieser Thread
		// die ImageView.

		@Override
		public void run() {
			try {
				while (true) {
					File file = imageQueue.take();
					BufferedImage image = ImageIO.read(file);
					panelImageView.paintComponents(panelImageView.getGraphics());
					panelImageView.getGraphics().drawImage(image, 0, 0, null);
					Thread.sleep(2500);
				}
			} catch (InterruptedException | IOException e) {
			}
		}

	}
}
