package edu.sjsu.cs166group2.util;

import edu.sjsu.cs166group2.model.PassHash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HashDao {

    private final Connection connection;

    public HashDao(Connection connection){
        this.connection = connection;
    }

    public boolean insertHashPair(String hash, String password, String hashType) {
        if(hash == null || password == null || hash.isEmpty()|| password.isEmpty()){
            System.out.println("Pass/hash is null or empty");
            return false;
        }
        else if(hash.length() != 64){
            System.out.println("Provided Hash is not of length 64. Invalid input.");
            return false;
        }
        else if(hashType == null || hashType.isEmpty()){
            System.out.println("Hash type is not provided or invalid. Please choose between sha256....");
            return false;
        }
        try{
            String insertSQL = "INSERT INTO "+hashType+"(hash,password) VALUES(?,?)";
            PassHash newHash = new PassHash(hash, password);
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

            preparedStatement.setString(1, newHash.getHash());
            preparedStatement.setString(2, newHash.getPassword());

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Insert failed because: "+e.getMessage());
            return false;
        }
        return true;
    }

    public String decryptHash(String hash, String hashType){
        if(hash == null || hash.isEmpty()){
            System.out.println("Hash is null or empty");
            return null;
        }
        else if(hash.length() != 64){
            System.out.println("Provided Hash is not of length 64. Invalid input.");
            return null;
        }
        else if(hashType == null || hashType.isEmpty()){
            System.out.println("Hash type is not provided or invalid. Please choose between sha256....");
            return null;
        }
        try{
            String readSQL = "SELECT password FROM "+hashType+" WHERE hash = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(readSQL);

            preparedStatement.setString(1, hash);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            return rs.getString("password");
        }
        catch (SQLException e) {
            System.out.println("Read failed because: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteHashPair(String hash, String hashType) {
        if(hash == null || hash.isEmpty()){
            System.out.println("Hash is null or empty");
            return false;
        }
        else if(hash.length() != 64){
            System.out.println("Provided Hash is not of length 64. Invalid input.");
            return false;
        }
        else if(hashType == null || hashType.isEmpty()){
            System.out.println("Hash type is not provided or invalid. Please choose between sha256....");
            return false;
        }
        try{
            String deleteSQL = "DELETE FROM "+hashType+" WHERE hash = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);

            preparedStatement.setString(1, hash);

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Delete failed because: "+e.getMessage());
            return false;
        }
        return true;
    }
}
