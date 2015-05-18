package LiveMicroscopy;

import ij.ImagePlus;
import ij.gui.ImageWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class CAMControlWindow extends JFrame implements IMessageObserver {

	private static final long serialVersionUID = 1L;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Singleton construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static CAMControlWindow instance;

	public static synchronized CAMControlWindow getInstance() {
		if (instance == null)
			instance = new CAMControlWindow();
		return instance;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI elements / properties
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	private JTextField textFieldHostIp;
	private JTextField textFieldHostPort;
	private JLabel lblConnectionstatus;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// CAM Connection
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private CAMConnection camConnection = CAMConnection.getInstance();

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Observer pattern - IMessageObserver
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private void registerObserver() {
		CAMConnection.getInstance().registerMessageObserver(this);
	}

	@Override
	public synchronized void receivedCAMCommand(String camCommand) {
		// TODO: Analyse command
		// --> CAM message?
		// --> file path?
		System.out.println(camCommand);

		if (camCommand.contains("/alternativepath:")) { // received file path
			int idxBeginCommand = camCommand.indexOf("/alternativepath:");
			int idxBeginPath = camCommand.indexOf(":", idxBeginCommand) + 1;
			int idxEndPath = camCommand.indexOf("/", idxBeginPath);
			
			String path = camCommand.substring(idxBeginPath, idxEndPath).trim();
			try {
				imageQueue.put(new File(path));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void receivedLogMessage(String logMessage) {
		// TODO: log message
		System.out.println(logMessage);
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Imaging
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private BlockingQueue<File> imageQueue = new LinkedBlockingQueue<File>();;

	private void initImaging() {
		Thread imageLoader = new Thread(new ImageLoaderThread());
		imageLoader.setDaemon(true);
		imageLoader.start();
	}

	private class ImageLoaderThread implements Runnable {
		@Override
		public void run() {
			Thread.currentThread().setName("Image-Loader");

			File imageFile = null;
			ImagePlus imagePlus = null;
			ImageWindow imageWindow = null;

			while (true) {
				try {
					imageFile = imageQueue.take();
					imagePlus = new ImagePlus(imageFile.getAbsolutePath());
					if (imageWindow == null) {
						imageWindow = new ImageWindow(imagePlus);
					} else {
						imageWindow.setImage(imagePlus);
						imageWindow.validate();
					}
					imageWindow.setBounds(screenSize.width / 2 - screenSize.height / 4, screenSize.height / 4,
							screenSize.height / 2, screenSize.height / 2);
					imageWindow.getCanvas().fitToWindow();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private CAMControlWindow() {

		registerObserver();
		initImaging();

		getContentPane().setLayout(null);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "Connection"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelConnection = new JPanel();
		panelConnection.setBounds(10, 11, 264, 122);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblConnection = new JLabel("Connection");
		lblConnection.setBounds(10, 11, 54, 14);
		panelConnection.add(lblConnection);

		lblConnectionstatus = new JLabel("not connected \u25CF");
		lblConnectionstatus.setForeground(Color.RED);
		lblConnectionstatus.setHorizontalAlignment(SwingConstants.TRAILING);
		lblConnectionstatus.setBounds(88, 11, 166, 14);
		panelConnection.add(lblConnectionstatus);

		JLabel lblHostIp = new JLabel("Host IP");
		lblHostIp.setBounds(10, 36, 68, 14);
		panelConnection.add(lblHostIp);

		textFieldHostIp = new JTextField();
		textFieldHostIp.setText("127.0.0.1");
		textFieldHostIp.setBounds(88, 33, 166, 20);
		panelConnection.add(textFieldHostIp);
		textFieldHostIp.setColumns(10);

		JLabel lblHostPort = new JLabel("Host Port");
		lblHostPort.setBounds(10, 61, 68, 14);
		panelConnection.add(lblHostPort);

		textFieldHostPort = new JTextField();
		textFieldHostPort.setText("8895");
		textFieldHostPort.setBounds(88, 58, 166, 20);
		panelConnection.add(textFieldHostPort);
		textFieldHostPort.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Connect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				lblConnectionstatus.setForeground(new Color(196, 196, 0));
				lblConnectionstatus.setText("connecting... \u25CF");

				try {
					InetAddress hostAddress = InetAddress.getByName(textFieldHostIp.getText());
					int hostPort = Integer.valueOf(textFieldHostPort.getText());
					camConnection.connect(hostAddress, hostPort);
				} catch (UnknownHostException | NumberFormatException e1) {
					e1.printStackTrace();
				}

				if (camConnection.isConnected()) {
					lblConnectionstatus.setForeground(new Color(0, 128, 0));
					lblConnectionstatus.setText("connected \u25CF");

					textFieldHostIp.setEditable(false);
					textFieldHostPort.setEditable(false);
				} else {
					lblConnectionstatus.setForeground(Color.RED);
					lblConnectionstatus.setText("not connected \u25CF");
				}
			}
		});
		btnConnect.setBounds(88, 89, 72, 23);
		panelConnection.add(btnConnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Disconnect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				lblConnectionstatus.setForeground(new Color(196, 196, 0));
				lblConnectionstatus.setText("disconnecting... \u25CF");

				camConnection.disconnect();

				if (!camConnection.isConnected()) {
					lblConnectionstatus.setForeground(Color.RED);
					lblConnectionstatus.setText("disconnected \u25CF");

					textFieldHostIp.setEditable(true);
					textFieldHostPort.setEditable(true);
				} else {
					lblConnectionstatus.setForeground(new Color(0, 128, 0));
					lblConnectionstatus.setText("connected \u25CF");
				}
			}
		});
		btnDisconnect.setBounds(170, 89, 84, 23);
		panelConnection.add(btnDisconnect);
	}
}
