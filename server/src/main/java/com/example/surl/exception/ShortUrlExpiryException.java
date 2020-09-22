package com.example.surl.exception;

public class ShortUrlExpiryException extends Exception{

    public ShortUrlExpiryException() { super();
    }

    public ShortUrlExpiryException(String message) {
        super(message);
    }
}
