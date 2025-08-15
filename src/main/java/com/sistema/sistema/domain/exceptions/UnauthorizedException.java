package com.sistema.sistema.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException() {
		this(null, null);
	}

	public UnauthorizedException(final String message) {
		this(message, null);
	}

	public UnauthorizedException(final Throwable cause) {
		this(cause != null ? cause.getMessage() : null, cause);
	}

	public UnauthorizedException(final String message, final Throwable cause) {
		super(message);
		if (cause != null)
			super.initCause(cause);
	}
}
