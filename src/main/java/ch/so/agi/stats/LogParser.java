package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogParser {
    private static Logger log = LoggerFactory.getLogger(LogParser.class);

    private static final String LOG_ENTRY_PATTERN =
            // 1:IP  2:client 3:user 4:date time 5:method 6:req 7:proto 8:respcode 9:size
            "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)";
    private static final Pattern PATTERN = Pattern.compile(LOG_ENTRY_PATTERN);
    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    
    private static final String WMS_REQUEST_INSERT = "INSERT INTO wms_request (id, md5, ip, "
            + "request_time, request_method, request, wms_request_type, wms_srs, wms_bbox, "
            + "wms_width, wms_height, dpi) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String WMS_REQUEST_LAYER_INSERT = "INSERT INTO wms_request_layer (id, "
            + "request_id, layer_name) VALUES (?, ?, ?)";
    
    private static final String SEQUENCE_NAME = "api_log_sequence";
    
    Connection conn = null;
    PreparedStatement pstmtWms = null;
    PreparedStatement pstmtWmsLayer = null;
    
    public LogParser(Connection conn) throws SQLException {
        this.conn = conn;
        pstmtWms = conn.prepareStatement(WMS_REQUEST_INSERT);
        pstmtWmsLayer = conn.prepareStatement(WMS_REQUEST_LAYER_INSERT);
    }
    
    public void doImport(String fileName) throws FileNotFoundException, IOException, URISyntaxException, SQLException {
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                //if (i>10000) break;
                
                // TODO:
                // - Statuscake ignorieren.
                if (line.contains("piwik")) continue; 
                
                
                // TODO:
                // - Es gibt Zeilen mit Error o.ä. -> separat behandeln
                // - Gibt es wirklich viele doppelte Requests? -> in separate Tabelle?
                // - Timezone? Was steht genau im Logfile? Was soll in der DB stehen?
                
                Matcher m = parseFromLogLine(line);
                
                if (m == null) {
                    continue;
                }              
                
                if (line.toLowerCase().contains("wms") && line.toLowerCase().contains("service") && line.toLowerCase().contains("request")) {
//                    try {
                        readWmsLine(m, line);
//                    } catch (Exception e) {
//                        log.error(line);
//                    }
                    
                }
                
//                String decodedQueryString = URLDecoder.decode(queryString, "UTF-8");
                                
//                System.out.println("m0: " + m.group(0));
//                System.out.println("m1: " + m.group(1));
//                System.out.println("m2: " + m.group(2));
//                System.out.println("m3: " + m.group(3));
//                System.out.println("m4: " + m.group(4));
//                System.out.println("m5: " + m.group(5));
//                System.out.println("m6: " + m.group(6));
//                System.out.println("m7: " + m.group(7));
//                System.out.println("m8: " + m.group(8));
//                System.out.println("m9: " + m.group(9));
                
               i++;
            }
        }
        
        
    }
    
    
    // https://databricks.gitbooks.io/databricks-spark-reference-applications/content/logs_analyzer/chapter1/java8/src/main/java/com/databricks/apps/logs/ApacheAccessLog.java
    private Matcher parseFromLogLine(String logline) {
        Matcher m = PATTERN.matcher(logline);
        if (!m.find()) {
          return null;
        }
        return m;
    }
    
    private void readWmsLine(Matcher m, String line) throws SQLException, URISyntaxException, UnsupportedEncodingException {
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(4), formatter);        
        String requestMethod = m.group(5);
        String request = m.group(6);
        String wmsRequestType = null;
        Integer wmsSrs = null;
        String wmsBbox = null;
        Integer wmsWidth = null;
        Integer wmsHeight = null;
        Double dpi = null;
        
        String[] layers = new String[0];
        
        List<NameValuePair> params = URLEncodedUtils.parse(new URI(request), Charset.forName("UTF-8"));
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
          
//          if (param.getName().equalsIgnoreCase("LAYERS") && param.getValue().length() > 40) {
//              //log.error("FUUUUUUBAR");
//              System.out.println(param.getName() + " : " + param.getValue());
//          }
      }
        
      pstmtWms.setLong(1, id);
      pstmtWms.setString(2, md5);
      pstmtWms.setString(3, ip);
      Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
      Calendar cal = GregorianCalendar.from(zonedDateTime);
      pstmtWms.setTimestamp(4, timestamp, cal);
      pstmtWms.setString(5, requestMethod);
      pstmtWms.setString(6, request);
      pstmtWms.setString(7, wmsRequestType);
      pstmtWms.setObject(8, wmsSrs, Types.INTEGER);
      pstmtWms.setString(9, wmsBbox);
      pstmtWms.setObject(10, wmsWidth, Types.INTEGER);
      pstmtWms.setObject(11, wmsHeight, Types.INTEGER);
      pstmtWms.setObject(12, dpi, Types.DOUBLE);
        
        // TODO: Mist, da gibt es tatsächlich einige doppelte.
        // D.h. Identische Zeit, identischer Request. Wie kann
        // das sein?
        // -> Wegspeichern?
        try {
            pstmtWms.executeUpdate(); 
            for (String layer : layers) {                
                pstmtWmsLayer.setLong(1, getId());
                pstmtWmsLayer.setLong(2, id);
                pstmtWmsLayer.setString(3, layer);
                pstmtWmsLayer.executeUpdate();
            }
        } catch (SQLException e) {
            //log.error(line);
            //log.error("duplicate line");
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
