package ch.so.agi.stats;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.net.URIBuilder;

public class DocumentRequest {
    private static final String SEQUENCE_NAME = "api_log_sequence";

    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final String WMS_REQUEST_INSERT = "INSERT INTO document_request (id, md5, ip, "
            + "request_time, request_method, request, document) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    Connection conn = null;
    PreparedStatement pstmt = null;
    
    public DocumentRequest(Connection conn) throws SQLException {
        this.conn = conn;
        pstmt = conn.prepareStatement(WMS_REQUEST_INSERT);
    }
    
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException { 
//        System.out.println(line);
        
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(4), formatter);        
        String requestMethod = m.group(5).toLowerCase();
        String request = m.group(6);
        
        URIBuilder builder = new URIBuilder(request, Charset.forName("UTF-8"));
        List<String> pathSegments = builder.getPathSegments();
        String document = pathSegments.get(pathSegments.size()-1);
        
        pstmt.setLong(1, id);
        pstmt.setString(2, md5);
        pstmt.setString(3, ip);
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
        Calendar cal = GregorianCalendar.from(zonedDateTime);
        pstmt.setTimestamp(4, timestamp, cal);
        pstmt.setString(5, requestMethod);
        pstmt.setString(6, request);
        pstmt.setString(7, document);

        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // duplicate lines
        }        
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
