package LiveMicroscopy;

import ij.ImagePlus;
import ij.gui.ImageWindow;

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
	
	private JTextArea textAreaLog;
	
	private JLabel lblConnectionstatus;
	private JLabel lblPipelineStatus;
	
	private JButton btnExecutePipeline;

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
			putFileIntoQueue(camCommand);
		}
	}

	@Override
	public synchronized void receivedLogMessage(String logMessage) {
		if (! (logMessage.isEmpty() || logMessage == null)) {
			logMessage = logMessage.trim().replace("\n", "");
			textAreaLog.append(logMessage + "\n");
		}
	}

	private void putFileIntoQueue(String camCommand) {
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

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Imaging
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private BlockingQueue<File> imageQueue = new LinkedBlockingQueue<File>();

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
						imageWindow.repaint();
					}
					imageWindow.setBounds(screenSize.width / 2 - screenSize.height / 4, screenSize.height / 4,
							screenSize.height / 2, screenSize.height / 2);
					imageWindow.getCanvas().fitToWindow();
					imageWindow.setVisible(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI
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
		btnConnect.setBounds(88, 89, 72, 23);
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
		btnDisconnect.setBounds(170, 89, 84, 23);
		panelConnection.add(btnDisconnect);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "CAM Pipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelCAMPipeline = new JPanel();
		panelCAMPipeline.setBounds(10, 144, 264, 242);
		getContentPane().add(panelCAMPipeline);
		panelCAMPipeline.setLayout(null);

		JLabel lblCamCommandPipeline = new JLabel("CAM Command Pipeline");
		lblCamCommandPipeline.setBounds(10, 11, 111, 14);
		panelCAMPipeline.add(lblCamCommandPipeline);

		lblPipelineStatus = new JLabel("not connected \u25CF");
		lblPipelineStatus.setForeground(Color.RED);
		lblPipelineStatus.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPipelineStatus.setBounds(175, 11, 79, 14);
		panelCAMPipeline.add(lblPipelineStatus);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// TextArea "CAM Pipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JScrollPane scrollPanePipeline = new JScrollPane();
		scrollPanePipeline.setBounds(10, 36, 244, 128);
		panelCAMPipeline.add(scrollPanePipeline);
		
		JList<String> listCAMPipeline = new JList<String>();
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

				AddNewLineGUI window = new AddNewLineGUI(newLineQueue);
				window.setBounds(getX() - 450 / 2, getY() + 200, 450, 127);
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
		buttonAddLine.setBounds(10, 175, 42, 23);
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
		btnRemoveLine.setBounds(62, 175, 42, 23);
		panelCAMPipeline.add(btnRemoveLine);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "SavePipeline"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnSavePipeline = new JButton("Save");
		btnSavePipeline.setEnabled(false);
		btnSavePipeline.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnSavePipeline.setBounds(198, 175, 56, 23);
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
		btnLoadPipeline.setBounds(132, 175, 56, 23);
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
		btnExecutePipeline.setBounds(132, 209, 122, 23);
		panelCAMPipeline.add(btnExecutePipeline);
		
		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "CAM Comunication Log"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
		JPanel panelCAMLog = new JPanel();
		panelCAMLog.setBounds(10, 397, 264, 154);
		getContentPane().add(panelCAMLog);
		panelCAMLog.setLayout(null);
		
		JLabel lblCamCommunicationLog = new JLabel("CAM Communication Log");
		lblCamCommunicationLog.setBounds(10, 11, 117, 14);
		panelCAMLog.add(lblCamCommunicationLog);
		
		JScrollPane scrollPaneLog = new JScrollPane();
		scrollPaneLog.setBounds(10, 36, 244, 107);
		panelCAMLog.add(scrollPaneLog);
		
		textAreaLog = new JTextArea();
		textAreaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
		textAreaLog.setEditable(false);
		scrollPaneLog.setViewportView(textAreaLog);
	}
}
