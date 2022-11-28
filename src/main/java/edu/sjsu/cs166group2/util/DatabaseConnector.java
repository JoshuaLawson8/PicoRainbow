package edu.sjsu.cs166group2.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Hello world!
 */
public class DatabaseConnector {
    public Connection initiateConnection(String host, int port, String username, String password)  {
        Properties props = new Properties();
        props.setProperty("rewriteBatchedStatements", "true");
        String connectURL = "jdbc:mysql://" + username +":" + password + "@" + host + ":" + port + "/rainbowtable?createDatabaseIfNotExist=true";
        String local = "jdbc:mysql://root:password@localhost:3306/rainbowtable?ssl-mode=REQUIRED?rewriteBatchedStatements=true";
        try {
            return DriverManager.getConnection(connectURL,props);
        } catch (Exception e) {
            System.out.println("Error in establishing connection: "+e.getMessage());
            return null;
        }
    }
    public Connection initiateConnection(){
        Properties props = new Properties();
        props.setProperty("rewriteBatchedStatements", "true");
        String local = "jdbc:mysql://root:password@localhost:3306/rainbowtable?ssl-mode=REQUIRED?rewriteBatchedStatements=true";
        try {
            return DriverManager.getConnection(local,props);
        } catch (Exception e) {
            System.out.println("Error in establishing connection: "+e.getMessage());
            return null;
        }
    }
}
