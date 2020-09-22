package com.example.surl.exception;

public class UserIdNotFoundException extends Exception{

    public UserIdNotFoundException() { super();
    }

    public UserIdNotFoundException(String message) {
        super(message);
    }
}
