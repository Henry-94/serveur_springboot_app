package com.reseausocial.group.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Email ou mot de passe incorrect");
    }
}
