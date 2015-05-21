package LiveMicroscopy;

import ij.gui.ImageWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class AddNewLineGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField textFieldAddNewLine;
	
	private BlockingQueue<String> newLineQueue;
	
	public AddNewLineGUI(BlockingQueue<String> newLineQueue) {
		
		this.newLineQueue = newLineQueue;
		
		setResizable(false);
		getContentPane().setLayout(null);

		JLabel lblAddNewCommand = new JLabel("Add new command to CAM pipeline");
		lblAddNewCommand.setBounds(10, 11, 167, 14);
		getContentPane().add(lblAddNewCommand);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// TextField "Enter-KeyListener" (same as Buttons 'ok' / 'cancel')
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		textFieldAddNewLine = new JTextField();
		textFieldAddNewLine.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					actionOk();
					break;
					
				case KeyEvent.VK_ESCAPE:
					actionCancel();
					break;
				}
			}
		});
		textFieldAddNewLine.setBounds(10, 36, 424, 20);
		getContentPane().add(textFieldAddNewLine);
		textFieldAddNewLine.setColumns(10);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Ok"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnOk = new JButton("Ok");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionOk();
			}
		});
		btnOk.setBounds(144, 67, 72, 23);
		getContentPane().add(btnOk);

		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		// Button "Cancel"
		// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionCancel();
			}
		});
		btnCancel.setBounds(226, 67, 72, 23);
		getContentPane().add(btnCancel);
	}
	
	private void actionOk() {
		try {
			newLineQueue.put(textFieldAddNewLine.getText());
			dispose();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
	
	private void actionCancel() {
		try {
			newLineQueue.put("");
			dispose();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}
}
