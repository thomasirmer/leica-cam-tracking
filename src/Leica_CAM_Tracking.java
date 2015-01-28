import ij.plugin.PlugIn;

/**
 * Leica_CAM_Tracking
 */

/**
 * @author Thomas Irmer
 *
 */
public class Leica_CAM_Tracking implements PlugIn {
	@Override
	public void run(String arg0) {
		// Create GUI and show
		PluginWindow gui = PluginWindow.getInstance();
		gui.setSize(250, 170);
		gui.setVisible(true);
	}

}
