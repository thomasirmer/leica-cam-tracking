import java.util.Hashtable; 
public class CAMCommandParser {
	
	/**
	 * Parses a given CAM command and puts all commands and their parameters into a hashmap.
	 * @param camCommand - The CAM command as single string
	 * @return hashmap containing all commands as keys and their parameter as values
	 */
	public Hashtable<String, String> parseCAMCommand(String camCommand) {
		// Hashtable that contains the parsed command
		Hashtable<String, String> parsedCommand = new Hashtable<String, String>();
		
		// split into all commands
		String[] splittedCommand = camCommand.split("/");
		
		// add command and corresponding parameter to hashtable
		for (String commandAndParameter : splittedCommand) {
			if (!commandAndParameter.isEmpty()) {
				String command   = commandAndParameter.split(":")[0].trim();
				String parameter = commandAndParameter.split(":")[1].trim();
				
				parsedCommand.put(command, parameter);
			}
		}
		
		return parsedCommand;
	}
}
