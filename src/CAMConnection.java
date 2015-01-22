import ij.IJ;



/**
 * 
 */

/**
 * @author Thomas Irmer
 *
 */
public class CAMConnection {
	
	private String host;
	private int port;
	
	public CAMConnection(String host, int port) {
		// TODO: Construction
		this.host = host;
		this.port = port;
	}
	
	public void connect() {
		// TODO Auto-generated method stub
		IJ.showMessage("Connection to host " + host + " at port " + port + ".");
	}

}
