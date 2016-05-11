package com.ispasov.nbujpmd.common.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class HelpCommand implements ICommand {
	private static final String[] FILTER = {"help"};

	private final List<ICommand> commandList;

	public HelpCommand(List<ICommand> commandList) {
		this.commandList = commandList;
	}

	@Override
	public void onCommand(String... args) throws ExitException {
		commandList.stream()
			 .map(x -> printCommandInfo(x))
			 .forEach(System.out::println);
	}

	private String printCommandInfo(ICommand command) {
		return Arrays.stream(command.getFilters())
			 .map(cmd -> printCommandDescription(cmd, command))
			 .reduce((x, y) -> x + "\n" + y)
			 .get();
	}

	private String printCommandDescription(String cmd, ICommand command) {
		return cmd + "\t\t" + command.getCommandDescription(cmd);
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
	public void onCommand(String... args) throws ExitException {
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
			while((commandLine = br.readLine()) != null) {
				String[] splitStr = commandLine.split("\\s+");
				String cmd = splitStr[0].toLowerCase();

				List<ICommand> runCommand = commandList.stream()
					 .filter(f -> ICommand.matchCommand(cmd, f))
					 .collect(Collectors.toList());

				if (!runCommand.isEmpty()) {
					for(ICommand command : runCommand) {
						command.onCommand(splitStr);
					}
				} else {
					System.out.println("Command not found. Type \"help\" for command list.");
				}
			}
		} catch (IOException ioe) {
			LOG.log(Level.SEVERE, ioe.toString(), ioe);
		} catch (ExitException ee) {
		}
	}
}