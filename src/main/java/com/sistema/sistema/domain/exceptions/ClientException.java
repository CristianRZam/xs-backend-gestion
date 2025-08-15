package com.sistema.sistema.domain.exceptions;

import com.sistema.sistema.shared.constants.ErrorCodes;
import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
	private final int code;

	public ClientException(ErrorCodes errorCode, String message) {
		super(message);
		this.code = errorCode.getCode();
	}
}
