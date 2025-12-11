import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 1. DATABASE CONFIGURATION
    private static final String URL = System.getenv().getOrDefault(
            "HOSPITAL_DB_URL",
            "jdbc:postgresql://localhost:5432/hospital_db");
    private static final String USER = System.getenv().getOrDefault("HOSPITAL_DB_USER", "postgres");
    private static final String PASSWORD = System.getenv().getOrDefault("HOSPITAL_DB_PASS", "postgres");

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // 2. LOAD THE DRIVER
            Class.forName("org.postgresql.Driver");
            
            // 3. CONNECT
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("[ERROR] PostgreSQL JDBC Driver not found. Did you add the JAR?");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("[ERROR] Connection failed. Check URL, user, or password.");
            e.printStackTrace();
        }
        return connection;
    }
}