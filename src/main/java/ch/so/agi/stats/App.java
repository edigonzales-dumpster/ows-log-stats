package ch.so.agi.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
        
        String databasePath = "/Users/stefan/tmp/sogismon";
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
                String tmpDir = Files.createTempDirectory("sogismon").toFile().getAbsolutePath();
                File tmpFile = new File(Paths.get(tmpDir, "init.sql").toFile().getAbsolutePath());
                InputStream is = App.class.getResourceAsStream("/init.sql"); 
                Files.copy(is, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                is.close();
                String content = new String(Files.readAllBytes(Paths.get(tmpFile.getAbsolutePath())));
                
                // init db
                stmt.execute(content);
                
                

            }
            
            
            
            LogParser logparser = new LogParser();
            logparser.parse("/Users/stefan/Downloads/api-gateway-logs/00-api-gateway-10-9f9kb.log");

            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        
        
        
        
        
        System.out.println("Hallo Welt.");
    }
}
