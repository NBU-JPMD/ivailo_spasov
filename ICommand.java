
class ExitException extends Exception {
    public ExitException(String message) {
        super(message);
    }
}

public interface ICommand {
	public boolean onCommand(String... args) throws ExitException;
	public String[] getFilters();
	public String getCommandDescription(String cmd);
}