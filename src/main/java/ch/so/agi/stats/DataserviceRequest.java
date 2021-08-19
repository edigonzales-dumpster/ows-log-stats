package ch.so.agi.stats;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URIBuilder;

import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.IspResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Postal;
import com.maxmind.geoip2.record.Subdivision;

public class DataserviceRequest extends AbstractRequest implements IRequest {
    private static final String DATETIME_FORMAT = "dd/MMM/yyyy:HH:mm:ss Z";
    private static final String DATASERVICE_REQUEST_INSERT = "INSERT INTO dataservice_request (id, md5, ip, "
            + "request_time, request_method, request, dataset, filter) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private PreparedStatement pstmt = null;
    
    public DataserviceRequest(Connection conn) throws SQLException, IOException {
        super(conn);
        pstmt = conn.prepareStatement(DATASERVICE_REQUEST_INSERT);
    }
    
    public void readLine(Matcher m, String line) throws URISyntaxException, SQLException, IOException, GeoIp2Exception {         
        long id = getId();
        String md5 = DigestUtils.md5Hex(line).toUpperCase();
        String ip = m.group(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(m.group(1), formatter);        
        String requestMethod = m.group(6).toLowerCase();
        String request = m.group(7);
        
        String[] requestSegments = request.split("\\?");
        String[] pathSegments = requestSegments[0].split("/");
        
        String dataset = null;
        if (pathSegments.length > 4) {
            dataset = pathSegments[4];
        }
        
        String filter = null;
        if (requestSegments.length > 1) {
            filter = requestSegments[1];
        }
        
//        String countryName = null;
//        String cityName = null;
//        try {
//            InetAddress ipAddress = InetAddress.getByName(ip);
//            CityResponse response = reader.city(ipAddress);
//            Country country = response.getCountry();
//            countryName = country.getName();
//            City city = response.getCity();
//            cityName = city.getName();
//        } catch (com.maxmind.geoip2.exception.AddressNotFoundException e) {
//            // do nothing
//        } 

        pstmt.setLong(1, id);
        pstmt.setString(2, md5);
        pstmt.setString(3, ip);
        Timestamp timestamp = Timestamp.from(zonedDateTime.toInstant());
        Calendar cal = GregorianCalendar.from(zonedDateTime);
        pstmt.setTimestamp(4, timestamp, cal);
        pstmt.setString(5, requestMethod);
        pstmt.setString(6, request);
        pstmt.setString(7, dataset);
        pstmt.setString(8, filter);

        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            // duplicate lines
        }        
    }
}
