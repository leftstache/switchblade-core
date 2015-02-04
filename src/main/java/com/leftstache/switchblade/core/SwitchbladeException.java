package com.leftstache.switchblade.core;

/**
 * @author Joel Johnson
 */
public class SwitchbladeException extends RuntimeException {
	public SwitchbladeException() {
	}

	public SwitchbladeException(String message) {
		super(message);
	}

	public SwitchbladeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SwitchbladeException(Throwable cause) {
		super(cause);
	}

	public SwitchbladeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
