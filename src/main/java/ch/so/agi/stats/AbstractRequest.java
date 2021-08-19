package ch.so.agi.stats;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;

import com.maxmind.geoip2.DatabaseReader;

public abstract class AbstractRequest {
    private static final String SEQUENCE_NAME = "api_log_sequence";
    protected static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";

    private Connection conn = null;
    protected DatabaseReader reader = null;
    
    public AbstractRequest(Connection conn) throws SQLException, IOException {
        this.conn = conn;
        
        //File database = new File("/Users/stefan/tmp/GeoLite2-City.mmdb");
        //reader = new DatabaseReader.Builder(database).build();
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
