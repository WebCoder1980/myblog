package org.myblog.users.exception;

public class UsernameIsAlreadyTakenException extends RuntimeException {
    public UsernameIsAlreadyTakenException() {
        super("Username is already taken");
    }
}
