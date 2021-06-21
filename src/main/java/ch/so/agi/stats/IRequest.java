package ch.so.agi.stats;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.regex.Matcher;

public interface IRequest {
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, UnsupportedEncodingException;
}