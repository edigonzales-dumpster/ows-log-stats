package ch.so.agi.stats;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractRequest {
    private static final String SEQUENCE_NAME = "api_log_sequence";
    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    private Connection conn = null;
    
    public AbstractRequest(Connection conn) {
        this.conn = conn;
    }
    
    private Long getId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT nextval('"+SEQUENCE_NAME+"')");
        long id;
        while(rs.next()) {
            id = rs.getLong(1);
            stmt.close();
            return id;
        }
        return null;
    }
}
