package ch.so.agi.stats;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMatomoParser {
    protected static Logger log = LoggerFactory.getLogger(LogParser.class);

    private static final String SEQUENCE_NAME = "api_log_sequence";
   
    private Connection conn = null;
    protected PreparedStatement pstmt = null;
    
    public AbstractMatomoParser(Connection conn) throws SQLException, IOException {
        this.conn = conn;        
    }

    protected Long getId() throws SQLException {
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
