package server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DatabaseManager {
    
    // A PAth to the database file
    private final String dbPath = "jdbc:ucanaccess://KishonViewer1.accdb";

    public DatabaseManager() {
        try (Connection conn = DriverManager.getConnection(dbPath)) {
            System.out.println("connection has been established");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void logConnection(String sessionKey, String hostIp) {
        String sql = "INSERT INTO Sessions (SessionKey, HostIP, Status, StartTime) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessionKey);
            pstmt.setString(2, hostIp);
            pstmt.setString(3, "WAITING"); // Session is waiting for a viewer
            pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            
            pstmt.executeUpdate();
            System.out.println("[DB] Session created: " + sessionKey + " (Host: " + hostIp + ")");
            
        } catch (SQLException e) {
            System.err.println("[DB Error] Error inserting host: " + e.getMessage());
        }
    }

    public void logViewerJoining(String sessionKey, String viewerIp) {
        // We only update rows that are currently 'WAITING' to ensure we don't overwrite active sessions
        String sql = "UPDATE Sessions SET ViewerIP = ?, Status = 'ACTIVE' WHERE SessionKey = ? AND Status = 'WAITING'";
        
        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, viewerIp);
            pstmt.setString(2, sessionKey);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[DB] Session activated: " + sessionKey + " (Viewer: " + viewerIp + ")");
            } else {
                System.out.println("[DB] Warning: No waiting session found for key: " + sessionKey);
            }
            
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void closeSession(String sessionKey) {
        String sql = "UPDATE Sessions SET Status = 'CLOSED' WHERE SessionKey = ?";
        
        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sessionKey);
            pstmt.executeUpdate();
            System.out.println("[DB] Session closed: " + sessionKey);
            
        } catch (SQLException e) {
            System.err.println("[DB Error] Error closing session: " + e.getMessage());
        }
    }
}