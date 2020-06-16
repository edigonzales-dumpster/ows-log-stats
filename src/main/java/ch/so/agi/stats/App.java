package ch.so.agi.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

public class App {

    public static void main(String[] args) throws FileNotFoundException, IOException, URISyntaxException {
        LogParser logparser = new LogParser();
        logparser.parse("/Users/stefan/tmp/access1.log");
        
        
        
        System.out.println("Hallo Welt.");
    }
}
