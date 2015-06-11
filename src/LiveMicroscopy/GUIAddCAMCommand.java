package LiveMicroscopy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;

public class GUIAddCAMCommand extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField textFieldAddNewLine;

	private BlockingQueue<String> commandQueue;

	public GUIAddCAMCommand(BlockingQueue<String> commandQueue, String clientName) {
		setTitle("Add CAM Command");

		this.commandQueue = commandQueue;

		setResizable(false);
		getContentPane().setLayout(null);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "Custom Line"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelNewLine = new JPanel();
		panelNewLine.setBounds(10, 316, 424, 75);
		getContentPane().add(panelNewLine);
		panelNewLine.setLayout(null);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// TextField "Enter-KeyListener" (same as Buttons 'ok' / 'cancel')
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		textFieldAddNewLine = new JTextField();
		textFieldAddNewLine.setBounds(10, 11, 404, 20);
		panelNewLine.add(textFieldAddNewLine);
		textFieldAddNewLine.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {

				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					actionOk(textFieldAddNewLine.getText());
					break;

				case KeyEvent.VK_ESCAPE:
					actionCancel();
					break;
				}
			}
		});
		textFieldAddNewLine.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Ok"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnOk = new JButton("Ok");
		btnOk.setBounds(260, 42, 72, 23);
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOk(textFieldAddNewLine.getText());
			}
		});
		panelNewLine.add(btnOk);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Cancel"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(342, 42, 72, 23);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCancel();
			}
		});
		panelNewLine.add(btnCancel);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Panel "Predefined Commands"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JPanel panelPredefinedCommands = new JPanel();
		panelPredefinedCommands.setBounds(10, 11, 424, 294);
		getContentPane().add(panelPredefinedCommands);
		panelPredefinedCommands.setLayout(null);

		// ---- BASIC COMMANDS
		// ----------------------------------------------------

		JLabel lblBasicCommands = new JLabel("Basic Commands");
		lblBasicCommands.setBounds(10, 11, 79, 14);
		panelPredefinedCommands.add(lblBasicCommands);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Start Scan"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnStartScan = new JButton("Start Scan");
		btnStartScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:startscan");
			}
		});
		btnStartScan.setBounds(10, 36, 107, 23);
		panelPredefinedCommands.add(btnStartScan);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Stop Scan"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnStopScan = new JButton("Stop Scan");
		btnStopScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:stopscan");
			}
		});
		btnStopScan.setBounds(10, 70, 107, 23);
		panelPredefinedCommands.add(btnStopScan);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Delete CAM List"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnDeleteCamList = new JButton("Delete CAM List");
		btnDeleteCamList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:deletelist");
			}
		});
		btnDeleteCamList.setBounds(10, 172, 107, 23);
		panelPredefinedCommands.add(btnDeleteCamList);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Start CAM Scan"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnStartCamScan = new JButton("Start CAM Scan");
		btnStartCamScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:startcamscan /runtime:10 /repeattime:10");
			}
		});
		btnStartCamScan.setBounds(10, 104, 107, 23);
		panelPredefinedCommands.add(btnStartCamScan);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Add CAM position"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnAddPosition = new JButton("Add Position");
		btnAddPosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine
						.setText("/cli:"
								+ clientName
								+ " /app:matrix /cmd:add /tar:camlist /exp:JOBNAME /ext:none /slide:0 /wellx:0 /welly:0 /fieldx:0 /fieldy:0 /dxpos:0 /dypos:0");
			}
		});
		btnAddPosition.setBounds(10, 206, 107, 23);
		panelPredefinedCommands.add(btnAddPosition);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Stop CAM Scan"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnStopCamScan = new JButton("Stop CAM Scan");
		btnStopCamScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:stopcamscan");
			}
		});
		btnStopCamScan.setBounds(10, 138, 107, 23);
		panelPredefinedCommands.add(btnStopCamScan);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Skip Waiting Message Box"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnSkipWaiting = new JButton("Skip Waiting");
		btnSkipWaiting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /sys:1 /cmd:stopwaitingforcam");
			}
		});
		btnSkipWaiting.setBounds(10, 240, 107, 23);
		panelPredefinedCommands.add(btnSkipWaiting);

		// ---- GET INFORMATION COMMANDS
		// ------------------------------------------

		JLabel lblGetInformation = new JLabel("Get Information");
		lblGetInformation.setBounds(127, 11, 79, 14);
		panelPredefinedCommands.add(lblGetInformation);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Stage Position"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnStagePosition = new JButton("Stage Position");
		btnStagePosition.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:getinfo /scmd:position");
			}
		});
		btnStagePosition.setBounds(127, 36, 107, 23);
		panelPredefinedCommands.add(btnStagePosition);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Available Jobs"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnJobs = new JButton("Jobs");
		btnJobs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:getinfo /dev:joblist");
			}
		});
		btnJobs.setBounds(127, 70, 107, 23);
		panelPredefinedCommands.add(btnJobs);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Available Patterns"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnPatterns = new JButton("Patterns");
		btnPatterns.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:getinfo /dev:patternlist");
			}
		});
		btnPatterns.setBounds(127, 104, 107, 23);
		panelPredefinedCommands.add(btnPatterns);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Experiment Information"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnExperiment = new JButton("Experiment");
		btnExperiment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldAddNewLine.setText("/cli:" + clientName + " /app:matrix /cmd:getinfo /dev:experiment");
			}
		});
		btnExperiment.setBounds(127, 138, 107, 23);
		panelPredefinedCommands.add(btnExperiment);
	}

	private void actionOk(String command) {
		try {
			commandQueue.put(command);
			dispose();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private void actionCancel() {
		try {
			commandQueue.put("");
			dispose();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
