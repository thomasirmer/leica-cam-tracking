import ij.plugin.PlugIn;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

/**
 * This class represents the ImageJ plug-in. It sets up the GUI and shows it on
 * the screen.
 */
public class Leica_CAM_Tracking implements PlugIn {

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI properties
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static final int GUI_WIDTH = 800;
	private static final int GUI_HEIGHT = 600;
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Fields
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	PluginWindow gui = null;

	@Override
	public void run(String arg0) {
		// Create GUI and show
		gui = PluginWindow.getInstance();
		gui.setSize(GUI_WIDTH, GUI_HEIGHT);
		gui.setLocation(getGuiOrigin(GUI_WIDTH, GUI_HEIGHT));
		gui.setVisible(true);
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Helper functions
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	/**
	 * Gives the top left point for a GUI window of the given size based on the
	 * screen resolution. This point can be used to place the GUI exactly in the
	 * middle of the screen.
	 * 
	 * @param guiWidth
	 * @param guiHeigth
	 * @return top left corner point for GUI
	 */
	private Point getGuiOrigin(int guiWidth, int guiHeigth) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		return new Point(screenWidth / 2 - guiWidth / 2, screenHeight / 2 - guiHeigth / 2);
	}
}
