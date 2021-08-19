package ch.so.agi.stats;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.regex.Matcher;

import com.maxmind.geoip2.exception.GeoIp2Exception;

public interface IRequest {
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, IOException, GeoIp2Exception;
}