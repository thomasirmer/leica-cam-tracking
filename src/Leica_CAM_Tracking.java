import ij.plugin.PlugIn;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This class represents the ImageJ plugin. It sets up the GUI and shows it on
 * the screen. <br>
 * <b>Please note:</b> This class must be placed in the default-package for the
 * plugin to work properly!
 * 
 * @author Thomas Irmer
 * @version 0.9
 */
public class Leica_CAM_Tracking implements PlugIn {
	// PlugIn class name must contain at least 1 underscore (_) - otherwise
	// ImageJ won't detect it as plugin. They will be displayed as blank spaces
	// in the plugin name in the ImageJ menu bar.

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// Plugin properties
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	public static final String PLUGIN_NAME = Leica_CAM_Tracking.class.getName();
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

	private static final int GUI_WIDTH = 1280;
	private static final int GUI_HEIGHT = 960;

	PluginWindow gui = null;

	@Override
	public void run(String arg0) {
		try { // set look and feel to OS style
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			// doesn't matter if this goes wrong
		}
		
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
