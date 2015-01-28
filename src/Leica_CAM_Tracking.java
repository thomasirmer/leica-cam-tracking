import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import ij.plugin.PlugIn;

/**
 * Leica_CAM_Tracking
 */

/**
 * @author Thomas Irmer
 *
 */
public class Leica_CAM_Tracking implements PlugIn {
	
	private static final int GUI_WIDTH = 800;
	private static final int GUI_HEIGHT = 600;
	
	@Override
	public void run(String arg0) {
		// Create GUI and show
		PluginWindow gui = PluginWindow.getInstance();
		gui.setSize(GUI_WIDTH, GUI_HEIGHT);
		gui.setLocation(getGuiOrigin());
		gui.setVisible(true);
	}

	// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
	// HELPER FUNCTIONS
	// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	
	private Point getGuiOrigin() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		return new Point(screenWidth / 2 - GUI_WIDTH / 2, screenHeight / 2 - GUI_HEIGHT / 2 );
	}
}
