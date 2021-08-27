package ch.so.agi.stats;

import java.io.IOException;
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
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

public class SearchtextRequest extends AbstractRequest implements IRequest {
    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final String SEARCHTEXT_REQUEST_INSERT = "INSERT INTO searchtext_request (id, md5, ip, "
            + "request_time, request_method, request, searchtext) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement pstmt = null;
    
    public SearchtextRequest(Connection conn) throws SQLException, IOException {
        super(conn);
        pstmt = conn.prepareStatement(SEARCHTEXT_REQUEST_INSERT);
    }
    
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException { 
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(1), formatter);        
        String requestMethod = m.group(6).toLowerCase();
        String request = m.group(7);
        
        
        URIBuilder builder = new URIBuilder(request, Charset.forName("UTF-8"));
        List<NameValuePair> params = builder.getQueryParams();
        for (NameValuePair param : params) {
            if (param.getName().equalsIgnoreCase("searchtext")) {
                String searchtext = param.getValue();
                
                pstmt.setLong(1, id);
                pstmt.setString(2, md5);
                pstmt.setString(3, ip);
                Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
                Calendar cal = GregorianCalendar.from(zonedDateTime);
                pstmt.setTimestamp(4, timestamp, cal);
                pstmt.setString(5, requestMethod);
                pstmt.setString(6, request);
                pstmt.setString(7, searchtext);

                try {
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    // duplicate lines
                }
            }
        }
    }
}
