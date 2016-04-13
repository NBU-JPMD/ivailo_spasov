package com.ispasov.nbujpmd.common.command;

public interface ICommand {
	public boolean onCommand(String... args) throws ExitException;
	public String[] getFilters();
	public String getCommandDescription(String cmd);
}