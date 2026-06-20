package com.cinemaweb.API.Cinema.Web.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentResponse implements Serializable {
    private String status;
    private String message;
    private String URL;
}
