import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EncodingDecoding {



    public static String encode(String url) {

        try {
            String encodedUrl  = URLEncoder.encode(url, "US-ASCII"); // Helst UTF-8 ?
            return encodedUrl;
        }
        catch(UnsupportedEncodingException e) {

            return "Happened when encoding: " + e.getMessage();
        }
    }

    public static String decode(String url) {

        try {

            String previousUrl = "";
            String decodedUrl = url;

            while(!previousUrl.equals(decodedUrl)) {

                previousUrl = decodedUrl;
                decodedUrl = URLDecoder.decode(decodedUrl, "US-ASCII"); // Helst UTF-8 ?
            }
            return decodedUrl;

        }
        catch(UnsupportedEncodingException e) {

            return "Happened when decoding: " + e.getMessage();
        }

    }
}
