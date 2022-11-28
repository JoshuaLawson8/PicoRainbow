package edu.sjsu.cs166group2.util;

import edu.sjsu.cs166group2.model.PassHash;

import java.security.InvalidParameterException;
import java.sql.*;
import java.util.List;

public class HashDao {

    private final Connection connection;

    private final String hashType;

    public HashDao(Connection connection, String hashType) throws InvalidParameterException {
        this.connection = connection;
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            // check if hashType table is there
            ResultSet tables = dbm.getTables(null, null, hashType, null);
            // Table doesn't exist, create it.
            if (!tables.next()){
                System.out.println("table for " + hashType + " didn't exist. Creating new table...");
                int hashLength = new HashUtil().hash("test",hashType).length();
                String createTable ="CREATE TABLE " + hashType +" (\n" +
                        "  hash char("+ hashLength+") NOT NULL,\n" +
                        "  password varchar(128) NOT NULL,\n" +
                        "  PRIMARY KEY (hash)\n" +
                        ")";
                PreparedStatement pStatement = connection.prepareStatement(createTable);
                pStatement.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Else table exists in DB and assign it
        this.hashType = hashType;
    }

    public String getHashType() {
        return hashType;
    }

    /**
     * Takes in a list of hashes in our PassHash format and adds them to the currently selected table.
     * Due to the functioning of batch statements, replaces prior hashes.
     *
     * @param listOfHashes
     * @return Null
     * @throws SQLException
     */
    public boolean insert(List<PassHash> listOfHashes) throws SQLException {
        int i = 0;
        String insertSQL = "REPLACE INTO " + hashType + "(hash,password) VALUES(?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            for(PassHash ph : listOfHashes) {
                try {

                    preparedStatement.setString(1, ph.getHash());
                    preparedStatement.setString(2, ph.getPassword());

                    preparedStatement.addBatch();
                    i++;
                    if (i % 10000 == 0 || i == listOfHashes.size()) {
                        System.out.println("Added " + i + " hashes");
                        preparedStatement.executeBatch(); // Execute every 1000 items.
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }
            }
        System.out.println("Added " + i + " hashes total");
        return false;
    }

    public boolean insertHashPair(PassHash passHash) {
        if(passHash == null || passHash.getHash() == null || passHash.getPassword() == null ||
                passHash.getHash().isEmpty()|| passHash.getPassword().isEmpty()){
            System.out.println("Pass/hash is null or empty");
            return false;
        }
        try{
            String insertSQL = "REPLACE INTO "+hashType+"(hash,password) VALUES(?,?)";
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
