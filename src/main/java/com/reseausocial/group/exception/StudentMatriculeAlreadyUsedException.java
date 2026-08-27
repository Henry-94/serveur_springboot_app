package com.reseausocial.group.exception;

public class StudentMatriculeAlreadyUsedException extends RuntimeException {
    public StudentMatriculeAlreadyUsedException(String matricule) {
        super("Ce numero etudiant est deja utilise : " + matricule);
    }
}
