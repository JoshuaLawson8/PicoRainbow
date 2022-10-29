package edu.sjsu.cs166group2.util;

import java.nio.charset.StandardCharsets;
import java.sql.*;


import com.mysql.cj.jdbc.Driver;

/**
 * Hello world!
 */
public class DatabaseConnector {
    public static void main(String[] args) {
        String sql = "select * from sha256;";
        String secretUrl = "jdbc:mysql://doadmin:AVNS_dku4d9HQcjNBDAQYqHg@db-mysql-nyc1-68984-do-user-12649737-0.b.db.ondigitalocean.com:25060/rainbowtable?ssl-mode=REQUIRED";
        try (Connection conn = DriverManager.getConnection(secretUrl);
             Statement stmt = conn.createStatement()) {
            try {
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String hash = rs.getString("hash");
                    String password = rs.getString("password");
                    System.out.println(password);
                    System.out.println(hash);
                }
            } catch (SQLException ex) {
                // handle any errors
                System.out.println("SQLException: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("VendorError: " + ex.getErrorCode());
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Error");
            System.exit(1);
        }
    }
}
