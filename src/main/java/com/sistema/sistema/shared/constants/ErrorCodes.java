package com.sistema.sistema.shared.constants;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    INVALID_CREDENTIALS(1001);

    private final int code;

    ErrorCodes(int code) {
        this.code = code;
    }
}
