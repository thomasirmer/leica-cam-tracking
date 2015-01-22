import ij.plugin.PlugIn;

/**
 * 
 */

/**
 * @author thoirm
 *
 */
public class Leica_CAM_Tracking implements PlugIn {

	@Override
	public void run(String arg0) {
		PluginWindow gui = PluginWindow.getInstance();
		gui.setVisible(true);
	}

}
