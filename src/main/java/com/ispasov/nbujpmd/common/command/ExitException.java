package com.ispasov.nbujpmd.common.command;

public class ExitException extends Exception {
	private static final long serialVersionUID = 69;

	public ExitException(String message) {
		super(message);
	}
}