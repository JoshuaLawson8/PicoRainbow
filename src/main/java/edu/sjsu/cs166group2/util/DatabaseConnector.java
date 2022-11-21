package edu.sjsu.cs166group2.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Hello world!
 */
public class DatabaseConnector {
    public Connection initiateConnection() {
        String secretUrl = "jdbc:mysql://doadmin:AVNS_dku4d9HQcjNBDAQYqHg@db-mysql-nyc1-68984-do-user-12649737-0.b.db.ondigitalocean.com:25060/rainbowtable?ssl-mode=REQUIRED";
        try {
            return DriverManager.getConnection(secretUrl);
        } catch (Exception e) {
            System.out.println("Error in establishing connection: "+e.getMessage());
            return null;
        }
    }
}
