package edu.sjsu.cs166group2.util;

import edu.sjsu.cs166group2.model.PassHash;

import java.security.InvalidParameterException;
import java.sql.*;

public class HashDao {

    private final Connection connection;

    private final String hashType;

    public HashDao(Connection connection, String hashType) throws InvalidParameterException {
        this.connection = connection;
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            // check if hashType table is there
            ResultSet tables = dbm.getTables(null, null, hashType, null);
            // Table table doesnt exists
            if (!tables.next())
                throw new InvalidParameterException("Table '" + hashType + "' doesn't exist");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Else table exists in DB and assign it
        this.hashType = hashType;
    }

    public String getHashType() {
        return hashType;
    }

    public boolean insertHashPair(String hash, String password) {
        if(hash == null || password == null || hash.isEmpty()|| password.isEmpty()){
            System.out.println("Pass/hash is null or empty");
            return false;
        }
        else if(HashUtil.hashToLength.get(hashType) == null ||
                hash.length() != HashUtil.hashToLength.get(hashType)){
            System.out.println("Hash type is invalid according to HashUtil," +
                    " not provided or has invalid length for its hash type");
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

    public boolean insertHashPair(PassHash passHash) {
        if(passHash == null || passHash.getHash() == null || passHash.getPassword() == null ||
                passHash.getHash().isEmpty()|| passHash.getPassword().isEmpty()){
            System.out.println("Pass/hash is null or empty");
            return false;
        }
        else if(HashUtil.hashToLength.get(hashType) == null ||
                passHash.getHash().length() != HashUtil.hashToLength.get(hashType)){
            System.out.println("Hash type is invalid according to HashUtil," +
                    " not provided or has invalid length for its hash type");
            return false;
        }
        try{
            String insertSQL = "INSERT INTO "+hashType+"(hash,password) VALUES(?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);

            preparedStatement.setString(1, passHash.getHash());
            preparedStatement.setString(2, passHash.getPassword());

            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("Insert failed because: "+e.getMessage());
            return false;
        }
        return true;
    }

    public String decryptHash(String hash){
        if(hash == null || hash.isEmpty()){
            System.out.println("Hash is null or empty");
            return null;
        }
        else if(HashUtil.hashToLength.get(hashType) == null ||
                hash.length() != HashUtil.hashToLength.get(hashType)){
            System.out.println("Hash type is invalid according to HashUtil," +
                    " not provided or has invalid length for its hash type");
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

    public boolean deleteHashPair(String hash) {
        if(hash == null || hash.isEmpty()){
            System.out.println("Hash is null or empty");
            return false;
        }
        else if(HashUtil.hashToLength.get(hashType) == null ||
                hash.length() != HashUtil.hashToLength.get(hashType)){
            System.out.println("Hash type is invalid according to HashUtil," +
                    " not provided or has invalid length for its hash type");
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
