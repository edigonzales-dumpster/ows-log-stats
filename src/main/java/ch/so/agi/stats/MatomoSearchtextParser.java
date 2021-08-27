package ch.so.agi.stats;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MatomoSearchtextParser extends AbstractMatomoParser implements IMatomoParser {
    private static final String INSERT_STMT = "INSERT INTO matomo_searchtext (id, year, month, "
            + "searchtext, acount) "
            + "VALUES (?, ?, ?, ?, ?)";

    public MatomoSearchtextParser(Connection conn) throws SQLException, IOException {
        super(conn);
        pstmt = conn.prepareStatement(INSERT_STMT);
    }

    @Override
    public void doImport(String fileName) throws FileNotFoundException, IOException, SQLException {        
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("searchtext") && !line.toLowerCase().contains("others")) {
                    String[] parts = line.split(",");
                    int year = Integer.valueOf(parts[0].split("-")[0]);
                    int month = Integer.valueOf(parts[0].split("-")[1]);
                    try {
                        String layername = parts[1].replace("\"", "").split("-")[1].trim().replace("{", "").replace("}", "").split(":")[1];
                        int count = Integer.valueOf(parts[2]);
                        
                        long id = getId();
                        
                        pstmt.setLong(1, id);
                        pstmt.setInt(2, year);
                        pstmt.setInt(3, month);
                        pstmt.setString(4, layername);
                        pstmt.setInt(5, count);

                        pstmt.executeUpdate();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        continue;
                    }
                }
            }
        }
    }
}
