import java.util.ArrayList;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

class HelpCommand implements ICommand {
	private ArrayList<ICommand> commandList;

	public HelpCommand(ArrayList<ICommand> commandList) {
		this.commandList = commandList;
	}

	public boolean onCommand(String... args) throws ExitException {
		for(ICommand command : commandList) {
			for(String filter : command.getFilters()) {
				String description = command.getCommandDescription(filter);
				System.out.println(filter + "\t\t" + ((description != null)?description:""));
			}
		}
		return false;
	}

	public String[] getFilters() {
		return new String[]{"help"};
	}

	public String getCommandDescription(String cmd) {
		return "Display help.";
	}
}

class ExitCommand implements ICommand {
	public boolean onCommand(String... args) throws ExitException {
		throw new ExitException("ExitCommand");
	}

	public String[] getFilters() {
		return new String[]{"quit", "exit"};
	}

	public String getCommandDescription(String cmd) {
		return "Exit application.";
	}
}

public class CommandHandler {
	private ArrayList<ICommand> commandList = new ArrayList<>();

	public CommandHandler() {
		registerCommand(new HelpCommand(commandList));
		registerCommand(new ExitCommand());
	}

	public void registerCommand(ICommand command) {
		commandList.add(command);
	}

	public void start() {
		String commandLine;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			boolean isCommandFound;
			while((commandLine = br.readLine()) != null) {
				String[] splitStr = commandLine.split("\\s+");
				isCommandFound = false;
				String cmd = splitStr[0].toLowerCase();
				for(ICommand command : commandList) {
					for(String filter : command.getFilters()) {
						if (cmd.equals(filter.toLowerCase())) {
							isCommandFound = true;
							if(!command.onCommand(splitStr)) {
								break;
							}
						}
					}
				}
				if (isCommandFound == false) {
					System.out.println("Command not found. Type \"help\" for command list.");
				}
			}
		} catch (IOException io) {
			io.printStackTrace();
		} catch (ExitException ee) {
		}
	}
}