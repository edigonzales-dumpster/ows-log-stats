package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
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
            // 1:IP  2:client 3:user 4:date time 5:method 6:req 7:proto   8:respcode 9:size
            "^(\\S+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(\\S+) (\\S+) (\\S+)\" (\\d{3}) (\\d+)";
    private static final Pattern PATTERN = Pattern.compile(LOG_ENTRY_PATTERN);

    public void parse(String fileName) throws FileNotFoundException, IOException, URISyntaxException {
        log.info(fileName);
        
        int i=0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (i>1000) continue;
                
                
                
                String queryString = parseFromLogLine(line);
                String decodedQueryString = URLDecoder.decode(queryString, "UTF-8");


                List<NameValuePair> params = URLEncodedUtils.parse(new URI(decodedQueryString), Charset.forName("UTF-8"));

                for (NameValuePair param : params) {
                    System.out.println(param.getName() + " : " + param.getValue());
                }

               
               i++;
            }
        }
        
        
    }
    
    
    public String parseFromLogLine(String logline) {
        Matcher m = PATTERN.matcher(logline);
        if (!m.find()) {
          log.info("Cannot parse logline" + logline);
          throw new RuntimeException("Error parsing logline");
        }
        return m.group(6);
    }
}
