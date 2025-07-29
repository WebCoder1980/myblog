package org.myblog.users.exception;

public class EmailIsAlreadyTakenException extends RuntimeException {
    public EmailIsAlreadyTakenException() {
        super("Email is already in use");
    }
}
