package com.jozufozu.flywheel.backend.loading;

public class ShaderLoadingException extends RuntimeException {
	public ShaderLoadingException() {
	}

	public ShaderLoadingException(String message) {
		super(message);
	}

	public ShaderLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ShaderLoadingException(Throwable cause) {
		super(cause);
	}

	public ShaderLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
