package model;

import java.io.Serializable;

/**
 * Classe modèle représentant un utilisateur du système.
 * Cette classe suit le principe de responsabilité unique (SRP) en ne gérant
 * que les données relatives à un utilisateur.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String email;
    private String passwordHash;
    private boolean isAdmin;
    private String salt;

    /**
     * Constructeur pour un nouvel utilisateur (sans ID)
     */
    public User(String email, String passwordHash, boolean isAdmin, String salt) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.isAdmin = isAdmin;
        this.salt = salt;
    }

    /**
     * Constructeur pour un utilisateur existant avec ID
     */
    public User(int id, String email, String passwordHash, boolean isAdmin, String salt) {
        this(email, passwordHash, isAdmin, salt);
        this.id = id;
    }

    // Getters et setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
} 