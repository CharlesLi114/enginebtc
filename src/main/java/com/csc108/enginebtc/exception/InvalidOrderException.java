package com.csc108.enginebtc.exception;

/**
 * Created by LI JT on 2019/9/19.
 * Description:
 */
public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String reason) {
        super(reason);
    }



}
