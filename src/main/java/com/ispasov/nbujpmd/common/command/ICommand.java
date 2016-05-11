package com.ispasov.nbujpmd.common.command;

import java.util.Arrays;

public interface ICommand {
	public void onCommand(String... args) throws ExitException;
	public String[] getFilters();
	public String getCommandDescription(String cmd);

	public static boolean matchCommand(String type, ICommand cmd) {
		return Arrays.stream(cmd.getFilters())
			 .filter(f -> type.equals(f))
			 .findAny()
			 .isPresent();
	}
}