package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    

    public void parse(String fileName) throws FileNotFoundException, IOException, URISyntaxException {
        log.info(fileName);
        
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (i>10000) break;
                
                // TODO:
                // - Es gibt Zeilen mit Error o.ä. -> separat behandeln
                // - ...
                
                Matcher m = parseFromLogLine(line);
                
                if (m == null) {
                    continue;
                }                
                
                //System.out.println(m.group(4));
                
                String queryString = m.group(6);
                String decodedQueryString = URLDecoder.decode(queryString, "UTF-8");
                
                if (queryString.toLowerCase().contains("21781")) {
                    //System.out.println(queryString);
                    //System.out.println(m.group(4));
                }
                
//                System.out.println("m0: " + m.group(0));
//                System.out.println("m1: " + m.group(1));
//                System.out.println("m2: " + m.group(2));
//                System.out.println("m3: " + m.group(3));
                System.out.println("m4: " + m.group(4));
//                System.out.println("m5: " + m.group(5));
//                System.out.println("m6: " + m.group(6));
//                System.out.println("m7: " + m.group(7));
//                System.out.println("m8: " + m.group(8));
//                System.out.println("m9: " + m.group(9));
                
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(4), formatter);
                
                System.out.println(zonedDateTime);
                
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
    public Matcher parseFromLogLine(String logline) {
        Matcher m = PATTERN.matcher(logline);
        if (!m.find()) {
          //log.info("Cannot parse logline" + logline);
          //throw new RuntimeException("Error parsing logline");
          return null;
        }
        return m;
    }
}
