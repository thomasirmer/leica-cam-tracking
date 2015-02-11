import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextArea;

/**
 * Represents the GUI and the button actions of the ImageJ-plugin. This class is realized as a singleton.
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
	private CAMCommandParser camCommandParser;
	private Logger logger = Logger.getGlobal();
	private LogHandler logHandler;
	private JTextField textFieldImagePath;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI and action listener
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private PluginWindow() {
		camCommandParser = new CAMCommandParser();

		getContentPane().setFont(new Font("Ubuntu", Font.PLAIN, 12));
		setFont(new Font("Ubuntu", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(PluginWindow.class.getResource("/icon/microscope_2.ico")));
		setResizable(false);
		setTitle("Leica CAM Tracking");
		getContentPane().setLayout(null);

		JLabel lblLeicaCamInterface = new JLabel("Leica CAM Interface");
		lblLeicaCamInterface.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		lblLeicaCamInterface.setBounds(10, 11, 190, 14);
		getContentPane().add(lblLeicaCamInterface);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelConnection.setBounds(10, 36, 190, 128);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblHostAddress = new JLabel("Host IP");
		lblHostAddress.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		lblHostAddress.setBounds(10, 18, 33, 14);
		panelConnection.add(lblHostAddress);

		textFieldHostAddress = new JTextField();
		textFieldHostAddress.setFont(new Font("Ubuntu Mono", Font.PLAIN, 10));
		textFieldHostAddress.setText("127.0.0.1");
		textFieldHostAddress.setBounds(53, 12, 125, 28);
		panelConnection.add(textFieldHostAddress);
		textFieldHostAddress.setColumns(15);

		JLabel lblPort = new JLabel("Port");
		lblPort.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		lblPort.setBounds(10, 57, 33, 14);
		panelConnection.add(lblPort);

		textFieldPort = new JTextField();
		textFieldPort.setFont(new Font("Ubuntu Mono", Font.PLAIN, 10));
		textFieldPort.setBounds(53, 51, 125, 28);
		panelConnection.add(textFieldPort);
		textFieldPort.setText("8895");
		textFieldPort.setEditable(false);
		textFieldPort.setColumns(5);

		JScrollPane scrollPaneLogging = new JScrollPane();
		scrollPaneLogging.setBounds(10, 541, 774, 220);
		getContentPane().add(scrollPaneLogging);

		JEditorPane textAreaLogging = new JEditorPane();
		textAreaLogging.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		scrollPaneLogging.setViewportView(textAreaLogging);
		// set to HTML text to display different colors based on log level
		textAreaLogging.setContentType("text/html");
		textAreaLogging.setText("<html>\n<head>\n</head>\n<body>\n</body>\n</html>");

		logHandler = new LogHandler(textAreaLogging);
		logger.addHandler(logHandler);

		JLabel lblLogView = new JLabel("Log View");
		lblLogView.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		scrollPaneLogging.setColumnHeaderView(lblLogView);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Connect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnConnect = new JButton("Connect");
		btnConnect.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		btnConnect.setBounds(10, 89, 73, 29);
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
		btnDisconnect.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		btnDisconnect.setBounds(93, 89, 85, 29);
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (camConnection != null) {
					camConnection.disconnect();
				}
			}
		});
		panelConnection.add(btnDisconnect);

		panelImageView = new JPanel();
		panelImageView.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelImageView.setBounds(210, 36, 574, 494);
		getContentPane().add(panelImageView);
		panelImageView.setLayout(new BorderLayout(0, 0));

		JPanel panelPathSelection = new JPanel();
		panelPathSelection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelPathSelection.setBounds(10, 433, 190, 97);
		getContentPane().add(panelPathSelection);
		panelPathSelection.setLayout(null);

		textFieldImagePath = new JTextField();
		textFieldImagePath.setFont(new Font("Ubuntu", Font.PLAIN, 10));
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
		buttonSelectPath.setFont(new Font("Ubuntu", Font.PLAIN, 10));
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
		buttonSelectPath.setBounds(10, 65, 87, 23);
		panelPathSelection.add(buttonSelectPath);

		JLabel lblImagePath = new JLabel("Image path");
		lblImagePath.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		lblImagePath.setBounds(10, 11, 170, 12);
		panelPathSelection.add(lblImagePath);

		JPanel panelCAMCommand = new JPanel();
		panelCAMCommand.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelCAMCommand.setBounds(10, 175, 190, 247);
		getContentPane().add(panelCAMCommand);
		panelCAMCommand.setLayout(null);

		JLabel lblCamCommand = new JLabel("CAM Command");
		lblCamCommand.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		lblCamCommand.setBounds(10, 10, 170, 12);
		panelCAMCommand.add(lblCamCommand);

		JTextArea textAreaCamCommand = new JTextArea();
		textAreaCamCommand.setLineWrap(true);
		textAreaCamCommand.setFont(new Font("Ubuntu Mono", Font.PLAIN, 10));
		textAreaCamCommand.setBounds(10, 33, 170, 169);
		panelCAMCommand.add(textAreaCamCommand);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Send CAM command"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnSendCommand = new JButton("Send command");
		btnSendCommand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (camConnection.isConnected()) {
					String camCommand = textAreaCamCommand.getText();
					if (camCommandParser.isValidCAMCommand(camCommand))
						camConnection.sendCAMCommand(camCommand);
					else
						logger.severe("Wrong CAM Command structure.");
				}
			}
		});
		btnSendCommand.setFont(new Font("Ubuntu", Font.PLAIN, 10));
		btnSendCommand.setBounds(10, 213, 105, 23);
		panelCAMCommand.add(btnSendCommand);
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
				while (true) { // TODO: Sollte durch sinnvolle Variable ersetzt
								// werden
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
