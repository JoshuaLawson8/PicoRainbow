package edu.sjsu.cs166group2.model;

public class PassHash {

    private final String hash;
    private final String password;

    public PassHash(String hash, String password){
        this.hash = hash;
        this.password = password;
    }

    public String getHash() {
        return hash;
    }

    public String getPassword() {
        return password;
    }
}