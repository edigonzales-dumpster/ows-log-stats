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

public class WmsRequest extends AbstractRequest implements IRequest {
    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final String WMS_REQUEST_INSERT = "INSERT INTO wms_request (id, md5, ip, "
            + "request_time, request_method, request, wms_request_type, wms_srs, wms_bbox, "
            + "wms_width, wms_height, dpi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String WMS_REQUEST_LAYER_INSERT = "INSERT INTO wms_request_layer (id, "
            + "request_id, layer_name) VALUES (?, ?, ?)";

    PreparedStatement pstmt = null;
    PreparedStatement pstmtLayer = null;

    public WmsRequest(Connection conn) throws SQLException, IOException {
        super(conn);
        pstmt = conn.prepareStatement(WMS_REQUEST_INSERT);
        pstmtLayer = conn.prepareStatement(WMS_REQUEST_LAYER_INSERT);
    }
    
    @Override
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException { 
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(1), formatter);        
        String requestMethod = m.group(6).toLowerCase();
        String request = m.group(7);
        String wmsRequestType = null;
        Integer wmsSrs = null;
        String wmsBbox = null;
        Integer wmsWidth = null;
        Integer wmsHeight = null;
        Double dpi = null;
        
        String[] layers = new String[0];
        
        URIBuilder builder = new URIBuilder(request, Charset.forName("UTF-8"));
        List<NameValuePair> params = builder.getQueryParams();
        for (NameValuePair param : params) {
            String paramName = param.getName();
            String paramValue = param.getValue();
            
            if (paramName.equalsIgnoreCase("request")) {
                wmsRequestType = paramValue.toLowerCase();
            } else if (paramName.equalsIgnoreCase("srs") || paramName.equalsIgnoreCase("crs")) {
                if (paramValue.length() > 5) {
                    String decodedValue = URLDecoder.decode(paramValue, "UTF-8");
                    wmsSrs = Integer.valueOf(decodedValue.split(":")[1]);
                }
            } else if (paramName.equalsIgnoreCase("bbox")) {
                wmsBbox = paramValue;
            } else if (paramName.equalsIgnoreCase("width")) {
                try {
                    wmsWidth = Integer.valueOf(paramValue);
                } catch (NumberFormatException e) {}
            } else if (paramName.equalsIgnoreCase("height")) {
                try {
                    wmsHeight = Integer.valueOf(paramValue);
                } catch (NumberFormatException e) {}
            } else if (paramName.equalsIgnoreCase("dpi")) {
                try {
                    dpi = Double.valueOf(paramValue);
                } catch (NumberFormatException e) {}
            } else if (paramName.equalsIgnoreCase("layers")) {
                String decodedValue = URLDecoder.decode(paramValue, "UTF-8");
                layers = decodedValue.split(",");                
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
        pstmt.setString(7, wmsRequestType);
        pstmt.setObject(8, wmsSrs, Types.INTEGER);
        pstmt.setString(9, wmsBbox);
        pstmt.setObject(10, wmsWidth, Types.INTEGER);
        pstmt.setObject(11, wmsHeight, Types.INTEGER);
        pstmt.setObject(12, dpi, Types.DOUBLE);
          
        // Es gibt identische Einträge im Logfile. Ob das tatsächlich identische
        // Requests sind?
        try {
            pstmt.executeUpdate();
            for (String layer : layers) {
                pstmtLayer.setLong(1, getId());
                pstmtLayer.setLong(2, id);
                pstmtLayer.setString(3, layer);
                pstmtLayer.executeUpdate();
            }
        } catch (SQLException e) {
            // duplicate lines
        }
    }    
}
