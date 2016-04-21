package com.ispasov.nbujpmd.common.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class HelpCommand implements ICommand {
	private static final String[] FILTER = {"help"};

	private final List<ICommand> commandList;

	public HelpCommand(List<ICommand> commandList) {
		this.commandList = commandList;
	}

	@Override
	public boolean onCommand(String... args) throws ExitException {
		for(ICommand command : commandList) {
			for(String filter : command.getFilters()) {
				String description = command.getCommandDescription(filter);
				System.out.println(filter + "\t\t" + ((description != null)?description:""));
			}
		}
		return false;
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		return "Display help.";
	}
}

class ExitCommand implements ICommand {
	private static final String[] FILTER = {"quit", "exit"};

	@Override
	public boolean onCommand(String... args) throws ExitException {
		throw new ExitException("ExitCommand");
	}

	@Override
	public String[] getFilters() {
		return FILTER;
	}

	@Override
	public String getCommandDescription(String cmd) {
		return "Exit application.";
	}
}

public final class CommandHandler {
	private static final Logger LOG = Logger.getLogger(CommandHandler.class.getName());

	private final List<ICommand> commandList = new ArrayList<>();

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
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		} catch (ExitException ee) {
		}
	}
}