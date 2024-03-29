
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

	public static final String PLUGIN_NAME = "Leica_CAM_Tracking";
	
	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// GUI
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

//	private static final int GUI_WIDTH_PLUGIN_WINDOW  = 900;
//	private static final int GUI_HEIGHT_PLUGIN_WINDOW = 900;
	
	private static final int GUI_WIDTH_CAM_CONTROL_WINDOW  = 500;
	private static final int GUI_HEIGHT_CAM_CONTROL_WINDOW = 800;

//	PluginWindow guiPluginWindow = null;
	GUIPluginMain guiPluginMain = null;

	@Override
	public void run(String arg0) {
		//Hallo
		try { // set look and feel to OS style
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			// doesn't matter if this goes wrong
		}
		
		// Create GUI <<PluginWindow>> and show
//		guiPluginWindow = PluginWindow.getInstance();
//		guiPluginWindow.setSize(GUI_WIDTH_PLUGIN_WINDOW, GUI_HEIGHT_PLUGIN_WINDOW);
//		guiPluginWindow.setLocation(getGuiOriginForCenter(GUI_WIDTH_PLUGIN_WINDOW, GUI_HEIGHT_PLUGIN_WINDOW));
//		guiPluginWindow.setVisible(true);
		
		// Create GUI GUIPluginMain and show
		guiPluginMain = GUIPluginMain.getInstance();
		guiPluginMain.setSize(GUI_WIDTH_CAM_CONTROL_WINDOW, GUI_HEIGHT_CAM_CONTROL_WINDOW);
		guiPluginMain.setLocation(getGuiOriginWithOffset(GUI_WIDTH_CAM_CONTROL_WINDOW, GUI_HEIGHT_CAM_CONTROL_WINDOW, 0.325f, 0));
		guiPluginMain.setVisible(true);
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
	private Point getGuiOriginForCenter(int guiWidth, int guiHeigth) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		return new Point(screenWidth / 2 - guiWidth / 2, screenHeight / 2 - guiHeigth / 2);
	}
	
	/**
	 * Gives the top left point for a GUI window of the given size based on the
	 * screen resolution. This point can be used to place the GUI at the center of the screen with the given offsets
	 * 
	 * @param guiWidth
	 * @param guiHeigth
	 * @param xOffset returnedOrigin.x += xOffset * screenWidth
	 * @param yOffset returnedOrigin.y += yOffset * screenWidth
	 * @return top left corner point for GUI
	 */
	private Point getGuiOriginWithOffset(int guiWidth, int guiHeigth, float xOffset, float yOffset) {
		Point origin = getGuiOriginForCenter(guiWidth, guiHeigth);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		origin.x += xOffset * screenSize.width;
		origin.y += yOffset * screenSize.height;
		return origin;
	}
}
