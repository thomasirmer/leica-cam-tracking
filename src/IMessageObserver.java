

public interface IMessageObserver {
	public void receivedCAMCommand(String camCommand);
	public void receivedLogMessage(String logMessage);
}
