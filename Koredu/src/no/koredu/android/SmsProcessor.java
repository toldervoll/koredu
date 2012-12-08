package no.koredu.android;

import com.google.android.maps.GeoPoint;
import no.koredu.common.Verification;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsProcessor {

  private final PeeringClient peeringClient;

  public SmsProcessor(PeeringClient peeringClient) {
    this.peeringClient = peeringClient;
  }

  public void processMessage(String phoneNumber, String message) {
    ParseResult parseResult = new ParseResult(message);
    if (parseResult.isKoreduMessage()) {
      peeringClient.requestSession(parseResult.getToken(), phoneNumber);
    }
  }

  private static class ParseResult {

    private static final Pattern KOREDU_URL_PATTERN = Pattern.compile(".*(http://koreduno.appspot.com/.+)");
    private String token;

    public ParseResult(String message) {
      Matcher matcher = KOREDU_URL_PATTERN.matcher(message);
      if (matcher.matches()) {
        String urlString = matcher.group(1);
        try {
          URL url = new URL(urlString);
          parseUrl(url);
        } catch (MalformedURLException e) {
          throw new RuntimeException("Malformed url: " + urlString);
        }
      }
    }

    private void parseUrl(URL url) {
      token = url.getFile().substring(1);
    }

    public String getToken() {
      return token;
    }

    public boolean isKoreduMessage() {
      return token != null;
    }
  }

}