package com.ispasov.nbujpmd.common.command;

public interface ICommand {
	public void onCommand(String... args) throws ExitException;
	public String[] getFilters();
	public String getCommandDescription(String cmd);
}