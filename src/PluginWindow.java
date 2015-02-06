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

/**
 *
 */
public class PluginWindow extends JFrame {

	private static final long serialVersionUID = 5811095093777366932L;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Singleton construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static PluginWindow instance = null;
	/**
	 * Singleton constructor.
	 * @return Either the present instance of <i>PluginWindow</i> or a new one.
	 */
	public static PluginWindow getInstance() {
		if (instance == null)
			return new PluginWindow();
		else
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
	private final static Logger logger = Logger.getGlobal();
	private JTextField textFieldImagePath;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI and action listener
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private PluginWindow() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(PluginWindow.class.getResource("/icon/microscope_2.ico")));
		setResizable(false);
		setTitle("Leica CAM Tracking");
		getContentPane().setLayout(null);

		JLabel lblLeicaCamInterface = new JLabel("Leica CAM Interface");
		lblLeicaCamInterface.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblLeicaCamInterface.setBounds(10, 11, 210, 14);
		getContentPane().add(lblLeicaCamInterface);

		JPanel panelConnection = new JPanel();
		panelConnection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelConnection.setBounds(10, 36, 230, 128);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblHostAddress = new JLabel("Host address");
		lblHostAddress.setBounds(10, 18, 83, 14);
		panelConnection.add(lblHostAddress);

		textFieldHostAddress = new JTextField();
		textFieldHostAddress.setText("127.0.0.1");
		textFieldHostAddress.setBounds(103, 11, 115, 28);
		panelConnection.add(textFieldHostAddress);
		textFieldHostAddress.setColumns(15);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(10, 57, 83, 14);
		panelConnection.add(lblPort);

		textFieldPort = new JTextField();
		textFieldPort.setBounds(103, 50, 115, 28);
		panelConnection.add(textFieldPort);
		textFieldPort.setText("8895");
		textFieldPort.setEditable(false);
		textFieldPort.setColumns(5);
		
		JScrollPane scrollPaneLogging = new JScrollPane();
		scrollPaneLogging.setBounds(10, 331, 764, 220);
		getContentPane().add(scrollPaneLogging);
		
		JEditorPane textAreaLogging = new JEditorPane();
		scrollPaneLogging.setViewportView(textAreaLogging);
		textAreaLogging.setContentType("text/html"); // set to HTML text to display different colors based on log level
		textAreaLogging.setText("<html>\n<head>\n</head>\n<body>\n</body>\n</html>");

		logger.addHandler(new LogHandler(textAreaLogging));
		
		JLabel lblLogView = new JLabel("Log View");
		scrollPaneLogging.setColumnHeaderView(lblLogView);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Connect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(10, 89, 89, 29);
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
		btnDisconnect.setBounds(116, 89, 102, 29);
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
		panelImageView.setBounds(250, 36, 524, 284);
		getContentPane().add(panelImageView);
		panelImageView.setLayout(new BorderLayout(0, 0));
		
		JPanel panelPathSelection = new JPanel();
		panelPathSelection.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelPathSelection.setBounds(10, 175, 230, 145);
		getContentPane().add(panelPathSelection);
		panelPathSelection.setLayout(null);
		
		textFieldImagePath = new JTextField();
		textFieldImagePath.setEditable(false);
		textFieldImagePath.setBounds(10, 11, 155, 20);
		panelPathSelection.add(textFieldImagePath);
		textFieldImagePath.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Select path"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		// TODO: Möglicherweise nicht notwendig
		JButton buttonSelectPath = new JButton("...");
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
						} catch (InterruptedException e1) {}
					}
					
					Thread imagePresenter = new Thread(new ImagePresenterThread());
					imagePresenter.start();
					// END: DEBUG
				}
			}
		});
		buttonSelectPath.setBounds(175, 10, 45, 23);
		panelPathSelection.add(buttonSelectPath);
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
	// Getter functions for GUI elements
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	private class ImagePresenterThread implements Runnable {

		// TODO: Dieser Thread soll an einer BlockingQueue darauf warten, dass neue Bilder angeboten werden.
		// Die Bilder werden dann in eine Queue geschoben, wenn ein CAM Command empfangen wird, dass den Pfad zu diesem Bild mitliefert.
		// Immer wenn ein neues Bild angeboten wird, aktualisiert dieser Thread die ImageView.
		
		@Override
		public void run() {
			try {
				while (true) { // TODO: Sollte durch sinnvolle Variable ersetzt werden
					File file = imageQueue.take();
					BufferedImage image = ImageIO.read(file);
					JLabel imageView = new JLabel(new ImageIcon(image));
					panelImageView.paintComponents(panelImageView.getGraphics());
					panelImageView.getGraphics().drawImage(image, 0, 0, null);
					Thread.sleep(2500);
				}
			} catch (InterruptedException | IOException e) {}
		}
		
	}
}
