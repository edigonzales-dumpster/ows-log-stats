package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatomoAddLayerParser {
    private static Logger log = LoggerFactory.getLogger(LogParser.class);

    private static final String SEQUENCE_NAME = "api_log_sequence";

    private Connection conn = null;
    private PreparedStatement pstmt = null;

    private static final String INSERT_STMT = "INSERT INTO matomo_add_layer (id, year, month, "
            + "layername, acount) "
            + "VALUES (?, ?, ?, ?, ?)";
    
    public MatomoAddLayerParser(Connection conn) throws SQLException {
        this.conn = conn;
        pstmt = conn.prepareStatement(INSERT_STMT);
    }
    
    public void doImport(String fileName) throws FileNotFoundException, IOException, SQLException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("add_layer") && line.toLowerCase().contains("layername")) {
                    String[] parts = line.split(",");
                    int year = Integer.valueOf(parts[0].split("-")[0]);
                    int month = Integer.valueOf(parts[0].split("-")[1]);
                    String layername = parts[1].replace("\"", "").split("-")[1].trim().replace("{", "").replace("}", "").split(":")[1];
                    int count = Integer.valueOf(parts[2]);

                    long id = getId();
                    
                    pstmt.setLong(1, id);
                    pstmt.setInt(2, year);
                    pstmt.setInt(3, month);
                    pstmt.setString(4, layername);
                    pstmt.setInt(5, count);

                    pstmt.executeUpdate();
                }
            }
        }
    }
    
    protected Long getId() throws SQLException {
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
