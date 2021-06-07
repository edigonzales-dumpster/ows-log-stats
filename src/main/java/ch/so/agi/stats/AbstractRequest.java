package ch.so.agi.stats;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;

public abstract class AbstractRequest {
    private static final String SEQUENCE_NAME = "api_log_sequence";
    protected static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    private Connection conn = null;
    
    public AbstractRequest(Connection conn) throws SQLException {
        this.conn = conn;
    }
    
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException {
        
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
