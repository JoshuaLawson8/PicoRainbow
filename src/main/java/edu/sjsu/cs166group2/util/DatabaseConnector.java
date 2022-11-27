package edu.sjsu.cs166group2.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Hello world!
 */
public class DatabaseConnector {
    public Connection initiateConnection() {
        Properties props = new Properties();
        props.setProperty("rewriteBatchedStatements", "true");
        String local = "jdbc:mysql://root:password@localhost:3306/rainbowtable?ssl-mode=REQUIRED?rewriteBatchedStatements=true";
        String secretUrl = "jdbc:mysql://doadmin:AVNS_dku4d9HQcjNBDAQYqHg@db-mysql-nyc1-68984-do-user-12649737-0.b.db.ondigitalocean.com:25060/rainbowtable?ssl-mode=REQUIRED?rewriteBatchedStatements=true";
        try {
            return DriverManager.getConnection(local,props);
        } catch (Exception e) {
            System.out.println("Error in establishing connection: "+e.getMessage());
            return null;
        }
    }
}
