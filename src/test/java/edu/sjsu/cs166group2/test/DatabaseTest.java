package edu.sjsu.cs166group2.test;

import edu.sjsu.cs166group2.model.PassHash;
import edu.sjsu.cs166group2.util.DatabaseConnector;
import edu.sjsu.cs166group2.util.HashDao;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.*;

public class DatabaseTest {

    static Connection connection;
    static HashDao hashDao;
    String testHash = "0000000000000000000000000000000000000000000000000000000000000000";
    String testPass = "testPass";
    static String hashType = "sha256";

    @BeforeAll
    public static void setup(){
        connection = new DatabaseConnector().initiateConnection();
        hashDao = new HashDao(connection, hashType);
    }

    @AfterAll
    public static void teardown(){
        connection = null;
        hashDao = null;
    }

    @Test
    public void getAllData() {
        String sql = "select * from "+hashType;
            try {
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String hash = rs.getString("hash");
                    String password = rs.getString("password");
                    System.out.println("Password: "+password);
                    System.out.println("Hash: "+hash);
                }
            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
            }
    }

    @Test
    public void insertHashPairTest() {
        assert(hashDao.deleteHashPair(testHash));
        PassHash newPassHash = new PassHash(testHash, testPass);
        assert(hashDao.insertHashPair(newPassHash));
    }

    @Test
    public void findPassWithHashTest() {
        assert(hashDao.decryptHash(testHash).equals(testPass));
    }
}
