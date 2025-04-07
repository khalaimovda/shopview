package com.github.khalaimovda.shopview.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;


@Getter
@RequiredArgsConstructor
public class PaymentServiceException extends RuntimeException {
    protected final HttpStatusCode status;
    protected final String reason;
}
