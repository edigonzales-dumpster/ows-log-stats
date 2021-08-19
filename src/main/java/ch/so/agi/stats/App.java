package ch.so.agi.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class App {
    static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException, GeoIp2Exception {
        
        String databasePath = "/Users/stefan/tmp/api-gateway-log-v2";
        boolean doInit = false;
        
        for(int i=0; i<args.length;i++) { 
            System.out.println(args[i]);
            
            if (args[i].equals("--init")) {
                doInit = true;
            }
        }
        
        try (Connection conn = DriverManager.getConnection ("jdbc:h2:"+databasePath, "sa","");
                Statement stmt = conn.createStatement();) {
            
            
            if (doInit) {
                String tmpDir = Files.createTempDirectory("apigatewaylog").toFile().getAbsolutePath();
                File tmpFile = new File(Paths.get(tmpDir, "init.sql").toFile().getAbsolutePath());
                InputStream is = App.class.getResourceAsStream("/init.sql"); 
                Files.copy(is, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                is.close();
                String content = new String(Files.readAllBytes(Paths.get(tmpFile.getAbsolutePath())));
                
                // init db
                stmt.execute(content);
            }
            
            LogParser logparser = new LogParser(conn);
//            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/00-api-gateway-10-9f9kb_20210531.log");
//            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/01-api-gateway-10-nrfvc_20210531.log");
//            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/api-gateway-10-scx2z_20210621.log");
//            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/api-gateway-10-xqqgw_20210621.log");
            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/api-gateway-14-fnptr_20210813.log");
            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/api-gateway-14-mszbm_20210813.log");
            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/api-gateway-14-fnptr_20210818.log");
            logparser.doImport("/Users/stefan/Downloads/api-gateway-logs/api-gateway-14-mszbm_20210818.log");

        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
