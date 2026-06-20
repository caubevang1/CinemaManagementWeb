package com.cinemaweb.API.Cinema.Web.exception;

import lombok.Data;

@Data
public class AppException extends RuntimeException {
    ErrorCode errorCode;
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
