package LiveMicroscopy;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.measure.Calibration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultCaret;

public class GUIPluginMain extends JFrame implements IMessageObserver {

	private static final boolean __DEBUG_MODE__ = true;

	private static final long serialVersionUID = 1L;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Singleton construction
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static GUIPluginMain instance;

	/**
	 * Singleton constructor
	 * 
	 * @return Either the present instance of {@link GUIPluginMain} or a new
	 *         one.
	 */
	public static synchronized GUIPluginMain getInstance() {
		if (instance == null)
			instance = new GUIPluginMain();
		return instance;
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI elements / properties
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	private JLabel lblConnectionstatus;
	private JTextField textFieldHostIp;
	private JTextField textFieldHostPort;

	private JLabel lblPipelineStatus;
	private JList<String> listCAMPipeline;
	private JButton btnExecutePipeline;

	private JTextArea textAreaLog;

	private JTextField textFieldMediaPath;

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// CAM Connection
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private CAMConnection camConnection = CAMConnection.getInstance();

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Observer pattern - IMessageObserver
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * Registers this object as observer at {@link CAMConnection}.
	 */
	private void registerObserver() {
		CAMConnection.getInstance().registerMessageObserver(this);
	}

	/**
	 * Will be invoked by all observed objects when a new CAM message arrives
	 * from LAS X. This message handles the message and triggers all necessarry
	 * steps.
	 */
	public synchronized void receivedCAMCommand(String camCommand) {
		// TODO: Analyse command
		// --> CAM message?

		// received new image path
		if (camCommand.contains("/alternativepath:") || camCommand.contains("/relpath:")) {
			putFileIntoQueue(camCommand);
		} else { // something different

		}
	}

	/**
	 * Will be invoked by all observed objects when a new log message is
	 * available. The log message will be displayed in the log text area.
	 */
	public synchronized void receivedLogMessage(String logMessage) {
		if (!(logMessage.isEmpty() || logMessage == null)) {
			logMessage = logMessage.trim().replace("\n", "");
			textAreaLog.append(logMessage + "\n");
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Imaging
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private BlockingQueue<File> imageQueue = new LinkedBlockingQueue<File>();
	private ImageWindow imageWindow = null;
	private JTextField textFieldClientName;

	/**
	 * Starts the {@link ImageLoaderThread} as deamon thread.
	 */
	private void initImaging() {
		Thread imageLoader = new Thread(new ImageLoaderThread());
		imageLoader.setDaemon(true);
		imageLoader.start();
	}

	static int idx = 0;
	/**
	 * If CAM returned a new image path this method will extract the path from
	 * the CAM message, create a new {@link File} and put it into the image
	 * queue.
	 * 
	 * @param camCommand
	 *            The CAM command containing the image path.
	 */
	private void putFileIntoQueue(String camCommand) {

		if (__DEBUG_MODE__) {
			
			try { // put random image from test folder into image queue
				File folder = new File("res\\test-images");
				ArrayList<File> images = new ArrayList<File>(Arrays.asList(folder.listFiles()));

				//Random rand = new Random();
				//int index = rand.nextInt(images.size());
				
				File image = images.get(idx);
				idx++;

				imageQueue.put(image);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return;
		}

		int idxBeginCommand;
		int idxBeginPath;
		String path;

		if (camCommand.contains("/alternativepath:")) {

			idxBeginCommand = camCommand.indexOf("/alternativepath:");
			idxBeginPath = camCommand.indexOf(":", idxBeginCommand) + 1;

			path = camCommand.substring(idxBeginPath).trim();

		} else { // camCommand.contains("/relpath:")

			idxBeginCommand = camCommand.indexOf("/relpath:");
			idxBeginPath = camCommand.indexOf(":", idxBeginCommand) + 1;

			String mediaPath = textFieldMediaPath.getText();
			if (!(mediaPath.endsWith("\\"))) {
				mediaPath = mediaPath + "\\";
			}
			path = mediaPath + camCommand.substring(idxBeginPath).trim();
		}

		try {
			imageQueue.put(new File(path));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits at the image queue for new images. Whenever an image is available
	 * this thread loads it into an {@link ImagePlus} and displays it. It also
	 * triggers the {@link CellTracking} process.
	 */
	private class ImageLoaderThread implements Runnable {
		@Override
		public void run() {
			Thread.currentThread().setName("Image-Loader");

			File imageFile = null;
			ImagePlus imagePlus = null;
			CellTracking cellTracking = new CellTracking();

			while (true) {
				try {
					imageFile = imageQueue.take(); 
					imagePlus = new ImagePlus(imageFile.getAbsolutePath());

					normalizeImageProperties(imagePlus);
					cellTracking.track(imagePlus);

					if (imageWindow == null) {
						imageWindow = new ImageWindow(imagePlus);
					} else {
						imageWindow.setImage(imagePlus);
						imageWindow.validate();
						imageWindow.repaint();
					}
					
					int xPosWindow = Math.round(screenSize.width / 16);
					int yPosWindow = Math.round(screenSize.height / 8);
					int widthWindow = Math.round(screenSize.width * 0.625f);
					int heigthWindow = Math.round(screenSize.height * 0.75f);
					
					imageWindow.setBounds(xPosWindow, yPosWindow, widthWindow, heigthWindow);
					imageWindow.getCanvas().fitToWindow();
					imageWindow.setVisible(true);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Sets all image properties to pixel values instead of physical units.
	 * @param img image to normalize
	 */
	private void normalizeImageProperties(ImagePlus img) {
		Calibration imgCalibration = img.getCalibration();
		imgCalibration.pixelHeight = 1.0;
		imgCalibration.pixelWidth = 1.0;
		imgCalibration.setUnit("pixel");
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private GUIPluginMain() {

		getContentPane().setLayout(null);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Initilization
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		registerObserver();
		initImaging();

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "Connection"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelConnection = new JPanel();
		panelConnection.setBounds(10, 11, 192, 122);
		getContentPane().add(panelConnection);
		panelConnection.setLayout(null);

		JLabel lblConnection = new JLabel("Connection");
		lblConnection.setBounds(10, 11, 54, 14);
		panelConnection.add(lblConnection);

		lblConnectionstatus = new JLabel("not connected \u25CF");
		lblConnectionstatus.setForeground(Color.RED);
		lblConnectionstatus.setHorizontalAlignment(SwingConstants.TRAILING);
		lblConnectionstatus.setBounds(88, 11, 94, 14);
		panelConnection.add(lblConnectionstatus);

		JLabel lblHostIp = new JLabel("Host IP");
		lblHostIp.setBounds(10, 36, 68, 14);
		panelConnection.add(lblHostIp);

		textFieldHostIp = new JTextField();
		textFieldHostIp.setText("127.0.0.1");
		textFieldHostIp.setBounds(88, 33, 94, 20);
		panelConnection.add(textFieldHostIp);
		textFieldHostIp.setColumns(10);

		JLabel lblHostPort = new JLabel("Host Port");
		lblHostPort.setBounds(10, 61, 68, 14);
		panelConnection.add(lblHostPort);

		textFieldHostPort = new JTextField();
		textFieldHostPort.setText("8895");
		textFieldHostPort.setBounds(88, 58, 94, 20);
		panelConnection.add(textFieldHostPort);
		textFieldHostPort.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Connect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

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

					lblPipelineStatus.setForeground(new Color(0, 128, 0));
					lblPipelineStatus.setText("ready \u25CF");

					btnExecutePipeline.setEnabled(true);
				} else {
					lblConnectionstatus.setForeground(Color.RED);
					lblConnectionstatus.setText("not connected \u25CF");

					lblPipelineStatus.setForeground(Color.RED);
					lblPipelineStatus.setText("not connected \u25CF");

					btnExecutePipeline.setEnabled(false);
				}
			}
		});
		btnConnect.setBounds(10, 89, 72, 23);
		panelConnection.add(btnConnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Disconnect"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				camConnection.disconnect();

				if (!camConnection.isConnected()) {
					lblConnectionstatus.setForeground(Color.RED);
					lblConnectionstatus.setText("disconnected \u25CF");

					textFieldHostIp.setEditable(true);
					textFieldHostPort.setEditable(true);

					lblPipelineStatus.setForeground(Color.RED);
					lblPipelineStatus.setText("disconnected \u25CF");

					btnExecutePipeline.setEnabled(false);
				} else {
					lblConnectionstatus.setForeground(new Color(0, 128, 0));
					lblConnectionstatus.setText("connected \u25CF");

					lblPipelineStatus.setForeground(new Color(0, 128, 0));
					lblPipelineStatus.setText("ready \u25CF");

					btnExecutePipeline.setEnabled(true);
				}
			}
		});
		btnDisconnect.setBounds(98, 89, 84, 23);
		panelConnection.add(btnDisconnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "CAM Pipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelCAMPipeline = new JPanel();
		panelCAMPipeline.setBounds(10, 144, 464, 242);
		getContentPane().add(panelCAMPipeline);
		panelCAMPipeline.setLayout(null);

		JLabel lblCamCommandPipeline = new JLabel("CAM Command Pipeline");
		lblCamCommandPipeline.setBounds(10, 11, 111, 14);
		panelCAMPipeline.add(lblCamCommandPipeline);

		lblPipelineStatus = new JLabel("not connected \u25CF");
		lblPipelineStatus.setForeground(Color.RED);
		lblPipelineStatus.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPipelineStatus.setBounds(375, 11, 79, 14);
		panelCAMPipeline.add(lblPipelineStatus);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// List "CAM Pipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JScrollPane scrollPanePipeline = new JScrollPane();
		scrollPanePipeline.setBounds(10, 36, 444, 127);
		panelCAMPipeline.add(scrollPanePipeline);

		listCAMPipeline = new JList<String>();
		listCAMPipeline.setModel(new DefaultListModel<String>());
		listCAMPipeline.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollPanePipeline.setViewportView(listCAMPipeline);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "AddLine"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton buttonAddLine = new JButton("+");
		buttonAddLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				BlockingQueue<String> newLineQueue = new SynchronousQueue<String>();

				GUIAddCAMCommand window = new GUIAddCAMCommand(newLineQueue, textFieldClientName.getText());
				window.setBounds(getX() - 450 / 2, getY() + 200, 450, 430);
				window.setVisible(true);

				// wait for ok from dialog
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							String newLine = newLineQueue.take();
							// TODO: Validate newLine
							if (!(newLine.isEmpty() || newLine == null)) {
								DefaultListModel<String> model = (DefaultListModel<String>) listCAMPipeline.getModel();
								model.addElement(newLine);
							}
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}).start();
			}
		});
		buttonAddLine.setBounds(10, 174, 42, 23);
		panelCAMPipeline.add(buttonAddLine);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "RemoveLine"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnRemoveLine = new JButton("-");
		btnRemoveLine.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> model = (DefaultListModel<String>) listCAMPipeline.getModel();

				for (String line : listCAMPipeline.getSelectedValuesList()) {
					model.removeElement(line);
				}
			}
		});
		btnRemoveLine.setBounds(62, 174, 42, 23);
		panelCAMPipeline.add(btnRemoveLine);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Line up"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnUp = new JButton("\u21e7");
		btnUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int nIndices = listCAMPipeline.getSelectedIndices().length;
				int index = listCAMPipeline.getSelectedIndex();

				if (nIndices == 1 && index > 0) {
					swapListElements(listCAMPipeline, index, index - 1);
					listCAMPipeline.setSelectedIndex(index - 1);
				}
			}
		});
		btnUp.setBounds(10, 208, 42, 23);
		panelCAMPipeline.add(btnUp);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Line down"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDown = new JButton("\u21E9");
		btnDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				DefaultListModel<String> model = (DefaultListModel<String>) listCAMPipeline.getModel();

				int nIndices = listCAMPipeline.getSelectedIndices().length;
				int index = listCAMPipeline.getSelectedIndex();
				int nElements = model.size();

				if (nIndices == 1 && index < nElements - 1) {
					swapListElements(listCAMPipeline, index, index + 1);
					listCAMPipeline.setSelectedIndex(index + 1);
				}
			}
		});
		btnDown.setBounds(62, 208, 42, 23);
		panelCAMPipeline.add(btnDown);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "SavePipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnSavePipeline = new JButton("Save");
		btnSavePipeline.setEnabled(false);
		btnSavePipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnSavePipeline.setBounds(398, 174, 56, 23);
		panelCAMPipeline.add(btnSavePipeline);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "LoadPipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnLoadPipeline = new JButton("Load");
		btnLoadPipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle("Select a text file with that describes your pipeline");

				int returnVal = fileChooser.showOpenDialog(null);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File pipelineFile = fileChooser.getSelectedFile();
					// TODO: Check if text file
					try {
						BufferedReader fileReader = new BufferedReader(new FileReader(pipelineFile));
						String line;
						DefaultListModel<String> model = (DefaultListModel<String>) listCAMPipeline.getModel();
						while ((line = fileReader.readLine()) != null) {
							model.addElement(line);
						}
						fileReader.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnLoadPipeline.setBounds(332, 174, 56, 23);
		panelCAMPipeline.add(btnLoadPipeline);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "ExecutePipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		btnExecutePipeline = new JButton("Execute");
		btnExecutePipeline.setEnabled(false);
		btnExecutePipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				DefaultListModel<String> model = (DefaultListModel<String>) listCAMPipeline.getModel();

				for (int i = 0; i < model.getSize(); i++) {
					String command = model.getElementAt(i);
					camConnection.sendCAMCommand(command);
				}
			}
		});
		btnExecutePipeline.setBounds(332, 208, 122, 23);
		panelCAMPipeline.add(btnExecutePipeline);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "CAM Comunication Log"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelCAMLog = new JPanel();
		panelCAMLog.setBounds(10, 397, 464, 154);
		getContentPane().add(panelCAMLog);
		panelCAMLog.setLayout(null);

		JLabel lblCamCommunicationLog = new JLabel("CAM Communication Log");
		lblCamCommunicationLog.setBounds(10, 11, 117, 14);
		panelCAMLog.add(lblCamCommunicationLog);

		JScrollPane scrollPaneLog = new JScrollPane();
		scrollPaneLog.setBounds(10, 36, 444, 107);
		panelCAMLog.add(scrollPaneLog);

		textAreaLog = new JTextArea();
		textAreaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textAreaLog.setEditable(false);
		DefaultCaret caret = (DefaultCaret) textAreaLog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollPaneLog.setViewportView(textAreaLog);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Clear Log"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnClearLog = new JButton("Clear Log");
		btnClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textAreaLog.setText("");
			}
		});
		btnClearLog.setBounds(365, 7, 89, 23);
		panelCAMLog.add(btnClearLog);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "Settings"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelSettings = new JPanel();
		panelSettings.setBounds(212, 11, 262, 122);
		getContentPane().add(panelSettings);
		panelSettings.setLayout(null);

		JLabel lblSettings = new JLabel("Settings");
		lblSettings.setBounds(10, 11, 46, 14);
		panelSettings.add(lblSettings);

		JLabel lblMediaPath = new JLabel("Media Path");
		lblMediaPath.setBounds(10, 36, 56, 14);
		panelSettings.add(lblMediaPath);

		textFieldMediaPath = new JTextField();
		textFieldMediaPath.setText("C:\\MatrixScreenerImages");
		textFieldMediaPath.setBounds(73, 33, 137, 20);
		panelSettings.add(textFieldMediaPath);
		textFieldMediaPath.setColumns(10);

		JButton btnSelectMediaPath = new JButton("...");
		btnSelectMediaPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfChooser = new JFileChooser();
				jfChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfChooser.setDialogTitle("Select Matrix Screener Media Path...");

				int retValue = jfChooser.showOpenDialog(null);

				if (retValue == JFileChooser.APPROVE_OPTION) {
					String mediaPath = jfChooser.getSelectedFile().getAbsolutePath();
					textFieldMediaPath.setText(mediaPath);
				}
			}
		});
		btnSelectMediaPath.setBounds(220, 32, 32, 22);
		panelSettings.add(btnSelectMediaPath);

		JLabel lblClientName = new JLabel("Client name");
		lblClientName.setBounds(10, 61, 56, 14);
		panelSettings.add(lblClientName);

		textFieldClientName = new JTextField();
		textFieldClientName.setBounds(73, 58, 179, 20);
		textFieldClientName.setText(Leica_CAM_Tracking.PLUGIN_NAME);
		panelSettings.add(textFieldClientName);
		textFieldClientName.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// __DEBUG_MODE__
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		if (__DEBUG_MODE__) {
			try {
				BufferedReader fileReader = new BufferedReader(new FileReader("./res/doc/CAMCommandPipeline.txt"));
				String line;
				DefaultListModel<String> model = (DefaultListModel<String>) listCAMPipeline.getModel();
				while ((line = fileReader.readLine()) != null) {
					model.addElement(line);
				}
				fileReader.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Helper methods
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private <T> void swapListElements(JList<T> list, int index1, int index2) {
		DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();

		T element1 = model.get(index1);
		T element2 = model.get(index2);

		model.set(index2, element1);
		model.set(index1, element2);
	}
}
