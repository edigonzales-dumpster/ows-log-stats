package ch.so.agi.stats;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.net.URIBuilder;

public class OwnerRequest extends AbstractRequest implements IRequest {
    private static final String OWNER_REQUEST_INSERT = "INSERT INTO owner_request (id, md5, ip, "
            + "request_time, request_method, request, egrid) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement pstmt = null;

    public OwnerRequest(Connection conn) throws SQLException {
        super(conn);
        pstmt = conn.prepareStatement(OWNER_REQUEST_INSERT);        
    }
    
    @Override
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException {        
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(4), formatter);        
        String requestMethod = m.group(5).toLowerCase();
        String request = m.group(6);
                
        URIBuilder builder = new URIBuilder(request, Charset.forName("UTF-8"));
        List<String> pathSegments = builder.getPathSegments();
        String egrid = pathSegments.get(pathSegments.size()-1);
        
        pstmt.setLong(1, id);
        pstmt.setString(2, md5);
        pstmt.setString(3, ip);
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
        Calendar cal = GregorianCalendar.from(zonedDateTime);
        pstmt.setTimestamp(4, timestamp, cal);
        pstmt.setString(5, requestMethod);
        pstmt.setString(6, request);
        pstmt.setString(7, egrid);

        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // duplicate lines
        }        
    }
}
