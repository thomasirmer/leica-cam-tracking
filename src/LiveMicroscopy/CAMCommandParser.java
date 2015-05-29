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
		camCommand = camCommand.replace("\n", " ");
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
}
