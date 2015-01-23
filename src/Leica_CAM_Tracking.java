import ij.plugin.PlugIn;

/**
 * Leica_CAM_Tracking
 */

/**
 * @author thoirm
 *
 */
public class Leica_CAM_Tracking implements PlugIn {

	@Override
	public void run(String arg0) {
		// Create GUI and show
		PluginWindow gui = PluginWindow.getInstance();
		gui.setLocationByPlatform(true);
		gui.setVisible(true);
	}

}
