package ch.so.agi.stats;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

public class WfsRequest extends AbstractRequest implements IRequest {
    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final String WFS_REQUEST_INSERT = "INSERT INTO wfs_request (id, md5, ip, "
            + "request_time, request_method, request, wfs_request_type, wfs_srs, wfs_bbox, "
            + "wfs_typename) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
       
    PreparedStatement pstmt = null;

    public WfsRequest(Connection conn) throws SQLException, IOException {
        super(conn);
        pstmt = conn.prepareStatement(WFS_REQUEST_INSERT);
    }
    
    @Override
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException {
        //System.out.println(m.group(1));
        //System.out.println(m.group(2));
        //System.out.println(m.group(3));
        //System.out.println(m.group(4));
        //System.out.println(m.group(5));
       // System.out.println(m.group(6));
       // System.out.println(m.group(7));
        //System.out.println(m.group(8));
        
        
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(1), formatter);        
        String requestMethod = m.group(6).toLowerCase();
        String request = m.group(7);
        String wfsRequestType = null;
        Integer wfsSrs = null;
        String wfsBbox = null;
        String wfsTypename = null;
        
        URIBuilder builder = new URIBuilder(request, Charset.forName("UTF-8"));
        List<NameValuePair> params = builder.getQueryParams();
        for (NameValuePair param : params) {
            String paramName = param.getName();
            String paramValue = param.getValue();
            
            if (paramName.equalsIgnoreCase("request")) {
                wfsRequestType = paramValue.toLowerCase();
            } else if (paramName.equalsIgnoreCase("srs") || paramName.equalsIgnoreCase("crs")) {
                if (paramValue.length() > 5) {
                    String decodedValue = URLDecoder.decode(paramValue, "UTF-8");
                    wfsSrs = Integer.valueOf(decodedValue.split(":")[1]);
                }
            } else if (paramName.equalsIgnoreCase("bbox")) {
                wfsBbox = paramValue;
            } else if (paramName.equalsIgnoreCase("typename")) {
                wfsTypename = paramValue;
            }          
        }
        
        pstmt.setLong(1, id);
        pstmt.setString(2, md5);
        pstmt.setString(3, ip);
        // TODO: Das stimmt noch nicht (?). Welche Zeitzone wird geloggt?
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
        Calendar cal = GregorianCalendar.from(zonedDateTime);
        pstmt.setTimestamp(4, timestamp, cal);
        pstmt.setString(5, requestMethod);
        pstmt.setString(6, request);
        pstmt.setString(7, wfsRequestType);
        pstmt.setObject(8, wfsSrs, Types.INTEGER);
        pstmt.setString(9, wfsBbox);
        pstmt.setObject(10, wfsTypename);
          
        // Es gibt identische Einträge im Logfile. Ob das tatsächlich identische
        // Requests sind?
        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // duplicate lines
        }
    }    
}
