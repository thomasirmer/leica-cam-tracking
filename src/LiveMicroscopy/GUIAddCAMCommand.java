package LiveMicroscopy;

import ij.gui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComboBox;

public class GUIAddCAMCommand extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField textFieldAddNewLine;

	private BlockingQueue<String> commandQueue;
	private JTextField textFieldCellID;
	private JComboBox<CAMJob> comboBoxCAMjobs;

	public GUIAddCAMCommand(BlockingQueue<String> commandQueue, String clientName) {
		
		setResizable(false);
		setTitle("Add CAM Command");

		this.commandQueue = commandQueue;
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
			public void keyPressed(KeyEvent e)  {

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

				// get coordinates for <<Add position command>>
				int xCoord = 0;
				int yCoord = 0;
				
				if (!textFieldCellID.getText().isEmpty()) {
					int cellID = Integer.parseInt(textFieldCellID.getText());
					List<Particle> particleList = GUIPluginMain.getParticleList();
					
					if (particleList != null) {
						xCoord = (int) Math.round(particleList.get(cellID - 1).getX());
						yCoord = (int) Math.round(particleList.get(cellID - 1).getY());
					}
				}
				
				// get experiment name for <<Add position command>>
				String jobname = "[EXPERIMENT_NAME]";
				
				CAMJob job = (CAMJob) comboBoxCAMjobs.getSelectedItem();
				if (job != null) {
					jobname = job.getJobname();
				}
				
				textFieldAddNewLine
						.setText("/cli:" + clientName
								+ " /app:matrix /cmd:add /tar:camlist"
								+ " /exp:" + jobname
								+ " /ext:none /slide:0 /wellx:0 /welly:0 /fieldx:0 /fieldy:0"
								+ " /dxpos:" + xCoord
								+ " /dypos:" + yCoord);
			}
		});
		btnAddPosition.setBounds(285, 104, 129, 23);
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
		btnSkipWaiting.setBounds(10, 206, 107, 23);
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
		
		JLabel lblCamPositions = new JLabel("CAM Positions");
		lblCamPositions.setBounds(244, 11, 67, 14);
		panelPredefinedCommands.add(lblCamPositions);
		
		JLabel lblCellId = new JLabel("Cell ID");
		lblCellId.setBounds(244, 40, 31, 14);
		panelPredefinedCommands.add(lblCellId);
		
		textFieldCellID = new JTextField();
		textFieldCellID.setBounds(285, 37, 129, 20);
		panelPredefinedCommands.add(textFieldCellID);
		textFieldCellID.setColumns(10);
		
		JLabel lblJob = new JLabel("Job");
		lblJob.setBounds(244, 74, 31, 14);
		panelPredefinedCommands.add(lblJob);
		
		comboBoxCAMjobs = new JComboBox<CAMJob>();
		comboBoxCAMjobs.setBounds(285, 71, 129, 20);
		panelPredefinedCommands.add(comboBoxCAMjobs);
		
		// add CAM job items to combo box
		List<CAMJob> jobList = GUIPluginMain.jobList;
		if (jobList != null) {
			for (int i = 0; i < jobList.size(); i++) {
				comboBoxCAMjobs.addItem(jobList.get(i));
			}
		}
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
