package LiveMicroscopy;

import java.util.Hashtable;

/**
 * The CAMCommandParser is able to validate a given CAM command and parsing a
 * CAM command to a dictionary format. It also provides a bunch of predefined
 * CAM commands that can be obtained from here.
 * 
 * @author Thomas Irmer
 */
public class CAMCommandParser {

	public static final int MOVE_ABSOLUTE = 0;
	public static final int MOVE_RELATIVE = 1;
	public static final int UNIT_METER = 0;
	public static final int UNIT_MICRONS = 1;

	private CAMCommandParser() {
	}

	/**
	 * Parses a given CAM command and puts all commands and their parameters
	 * into a hashmap.
	 * 
	 * @param camCommand
	 *            - The CAM command as single string
	 * @return hashmap containing all commands as keys and their parameter as
	 *         values
	 */
	public static Hashtable<String, String> parseStringToCAMCommand(String camCommand) {
		// Hashtable that contains the parsed command
		Hashtable<String, String> parsedCommand = new Hashtable<String, String>();

		// split into all commands
		String[] splittedCommand = camCommand.split("/");

		// add command and corresponding parameter to hashtable
		for (String commandAndParameter : splittedCommand) {
			if (!commandAndParameter.isEmpty()) {
				String command = commandAndParameter.split(":")[0].trim();
				String parameter = commandAndParameter.split(":")[1].trim();

				parsedCommand.put(command, parameter);
			}
		}

		return parsedCommand;
	}

	public static boolean isValidCAMCommand(String camCommand) {
		// split into {/command:parameter} blocks
		String[] splittedCAMCommand = camCommand.split(" ");

		// check for valid syntax per block
		for (String block : splittedCAMCommand) {
			if (block.contains(" ") || !block.contains("/") || !block.contains(":"))
				return false;
			String command = block.substring(1, block.indexOf(":"));
			String parameter = block.substring(block.indexOf(":") + 1, block.length());
			if (command.isEmpty() || parameter.isEmpty() || command.contains(":") || command.contains("/")
					|| parameter.contains(":") || parameter.contains("/"))
				return false;
		}

		return true;
	}

	public static String getCommandStageInfo() {
		return "/cli:" + Leica_CAM_Tracking.PLUGIN_NAME + " /app:matrix /cmd:getinfo /dev:stage";
	}

	public static String getCommandScanStatus() {
		return "/cli:" + Leica_CAM_Tracking.PLUGIN_NAME + " /app:matrix /cmd:getinfo /dev:scanstatus";
	}

	/**
	 * 
	 * @param xPos
	 *            the new x-Position for the stage
	 * @param yPos
	 *            the new y-Position for the stage
	 * @param moveType
	 *            either absolute or relative (use the constants in this class:
	 *            MOVE_ABSOLUTE, MOVE_RELATIVE)
	 * @param unit
	 *            either meter or microns (use the constants in this class:
	 *            UNIT_METER, UNIT_MICRONS)
	 * @return
	 * @throws Exception
	 */
	public static String createCommandMoveStage(double xPos, double yPos, int type, int unit) throws Exception {
		String typeS;
		if (type == MOVE_ABSOLUTE)
			typeS = "absolute";
		else if (type == MOVE_RELATIVE)
			typeS = "relative";
		else
			throw new Exception("Wrong movement type. Possible values: MOVE_ABSOLUTE, MOVE_RELATIVE");

		String unitS;
		if (unit == UNIT_METER)
			unitS = "meter";
		else if (unit == UNIT_MICRONS)
			unitS = "microns";
		else
			throw new Exception("Wrong unit type. Possible values: UNIT_METER, UNIT_MICRONS");

		return "/cli:" + Leica_CAM_Tracking.PLUGIN_NAME + " /app:matrix /sys:1 /cmd:setposition /typ:" + typeS
				+ " /dev:stage /unit:" + unitS + " /xpos:" + xPos + " /ypos:" + yPos;
	}
}
