package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogParser {
    private static Logger log = LoggerFactory.getLogger(LogParser.class);

    private static final String LOG_ENTRY_PATTERN =
            // 1:IP  2:client 3:user 4:date time 5:method 6:req 7:proto 8:respcode 9:size
            //"^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)";
    
            // neu (ab 2021-08):
            "^\\[([\\w:/]+\\s[+\\-]\\d{4})\\] (\\S+) (\\S+) (\\S+) (\\S+) \"(\\S+) (\\S+) (\\S+)\" (\\S+) (\\d+)";

    private static final Pattern PATTERN = Pattern.compile(LOG_ENTRY_PATTERN);
    
    Connection conn = null;
    WmsRequest wmsRequest = null;
    WfsRequest wfsRequest = null;
    DocumentRequest docRequest = null;
    OwnerRequest ownerRequest = null;
    
    public LogParser(Connection conn) throws SQLException {
        this.conn = conn;
        wmsRequest = new WmsRequest(conn);
        wfsRequest = new WfsRequest(conn);
        docRequest = new DocumentRequest(conn);
        ownerRequest = new OwnerRequest(conn);
    }
    
    public void doImport(String fileName) throws FileNotFoundException, IOException {
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                i++;

                //if (i>200000) break;
                
                // Ignore requests from health checks etc.
                if (line.toLowerCase().contains("piwik") || line.toLowerCase().contains("statuscake") ||
                        line.toLowerCase().contains("nagios")) continue; 
                                
                Matcher m = parseFromLogLine(line);
                if (m == null) {
                    continue;
                }              
                                
                // WMS requests
                if (line.toLowerCase().contains("wms") && line.toLowerCase().contains("service") && line.toLowerCase().contains("request")) {
                    try {
                        wmsRequest.readLine(m, line);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                
                // WFS requests
                if (line.toLowerCase().contains("wfs") && line.toLowerCase().contains("service") && line.toLowerCase().contains("getfeature")) {
                    try {
                        wfsRequest.readLine(m, line);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                
//                // Document requests
//                if (line.toLowerCase().contains("get") && line.toLowerCase().contains("api") && line.toLowerCase().contains("document")) {                    
//                    try {
//                        docRequest.readLine(m, line);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//                }

//                // Owner requests
//                if(line.toLowerCase().contains("owner") && line.toLowerCase().contains("token")) {
//                    try {
//                        ownerRequest.readLine(m, line);
//                    } catch (URISyntaxException e) {
//                        e.printStackTrace();
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
//
//                }
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
}
