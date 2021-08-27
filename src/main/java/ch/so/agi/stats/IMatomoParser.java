package ch.so.agi.stats;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public interface IMatomoParser {
    public void doImport(String fileName) throws FileNotFoundException, IOException, SQLException;
}
