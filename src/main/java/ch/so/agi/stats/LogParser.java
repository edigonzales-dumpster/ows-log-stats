package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
import java.sql.Statement;
import java.sql.Timestamp;
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
            + "wms_width, wms_height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    Connection conn = null;
    PreparedStatement pstmtWms = null;
    
    public LogParser(Connection conn) throws SQLException {
        this.conn = conn;
        pstmtWms = conn.prepareStatement(WMS_REQUEST_INSERT, Statement.RETURN_GENERATED_KEYS);

    }
    
    public void doImport(String fileName) throws FileNotFoundException, IOException, URISyntaxException, SQLException {
        log.info(fileName);
        
        
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (i>10000) break;
                
                // TODO:
                // - Es gibt Zeilen mit Error o.ä. -> separat behandeln
                // - Gibt es wirklich viele doppelte Requests? -> in separate Tabelle?
                // - Timezone? Was steht genau im Logfile? Was soll in der DB stehen?
                
                Matcher m = parseFromLogLine(line);
                
                if (m == null) {
                    continue;
                }              
                
                if (line.toLowerCase().contains("wms") && line.toLowerCase().contains("service") && line.toLowerCase().contains("request")) {
                    readWmsLine(m, line);
                }
                
                
//                String queryString = m.group(6);
//                String decodedQueryString = URLDecoder.decode(queryString, "UTF-8");
                
//                if (queryString.toLowerCase().contains("21781")) {
//                    //System.out.println(queryString);
//                    //System.out.println(m.group(4));
//                }
                
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
                
                
//                System.out.println(zonedDateTime);

                
                //System.out.println(decodedQueryString);
                
                // java.net.URISyntaxException
                // Warum decode ich den Query-String? Hier fliegt er mir um die Ohren. 
//                List<NameValuePair> params = URLEncodedUtils.parse(new URI(queryString), Charset.forName("UTF-8"));

                //log.info(decodedQueryString);
//                for (NameValuePair param : params) {
//                    //System.out.println(param.getName() + " : " + param.getValue());
//                    
//                    
//                    if (param.getName().equalsIgnoreCase("LAYERS") && param.getValue().length() > 40) {
//                        //log.error("FUUUUUUBAR");
//                        System.out.println(param.getName() + " : " + param.getValue());
//                    }
//                }

               
               i++;
            }
        }
        
        
    }
    
    
    // https://databricks.gitbooks.io/databricks-spark-reference-applications/content/logs_analyzer/chapter1/java8/src/main/java/com/databricks/apps/logs/ApacheAccessLog.java
    private Matcher parseFromLogLine(String logline) {
        Matcher m = PATTERN.matcher(logline);
        if (!m.find()) {
          //log.info("Cannot parse logline" + logline);
          //throw new RuntimeException("Error parsing logline");
          return null;
        }
        return m;
    }
    
    private void readWmsLine(Matcher m, String line) throws SQLException, URISyntaxException {
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(4), formatter);        
        String requestMethod = m.group(5);
        String request = m.group(6);
        String wmsequestType = null;
        int wmsSrs = -1;
        String wmsBbox = null;
        int wmsWidth = -1;
        int wmsHeight = -1;
        
        
      List<NameValuePair> params = URLEncodedUtils.parse(new URI(request), Charset.forName("UTF-8"));
      for (NameValuePair param : params) {
          System.out.println(param.getName() + " : " + param.getValue());
          
          
//          if (param.getName().equalsIgnoreCase("LAYERS") && param.getValue().length() > 40) {
//              //log.error("FUUUUUUBAR");
//              System.out.println(param.getName() + " : " + param.getValue());
//          }
      }

        
        
        
//        pstmtWms.setLong(1, id);
//        pstmtRequest.setString(1, md5);
//        pstmtRequest.setString(2, ip);
//        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
//        Calendar cal = GregorianCalendar.from(zonedDateTime);
//        pstmtRequest.setTimestamp(3, timestamp, cal);
//        pstmtRequest.setString(4, requestMethod);
//        pstmtRequest.setString(5, request);
//        pstmtRequest.setString(6, "foo");
//        pstmtRequest.setInt(7, 0);
//        pstmtRequest.setString(8, "foo");
//        pstmtRequest.setInt(9, 0);
//        pstmtRequest.setInt(10, 0);
        
        // TODO: Mist, da gibt es tatsächlich einige doppelte.
        // D.h. Identische Zeit, identischer Request. Wie kann
        // das sein?
        // -> Wegspeichern?
//        try {
//            int row = pstmtRequest.executeUpdate();
//            
//            ResultSet res = pstmtRequest.getGeneratedKeys();
////            while (res.next()) {
////                System.out.println(res.getString(1));
////             }
//
//            
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

    }
    
    
    private Long getId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT nextval('public.ows_log_sequence')");
        long id;
        while(rs.next()) {
            id = rs.getLong(1);
            stmt.close();
            return id;
        }
        return null;
    }
}
