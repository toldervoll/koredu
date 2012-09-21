package no.koredu.android;

import com.google.android.maps.GeoPoint;
import no.koredu.common.Verification;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsProcessor {

  private final PhoneNumberVerifier phoneNumberVerifier;
  private final PeeringClient peeringClient;

  public SmsProcessor(PhoneNumberVerifier phoneNumberVerifier, PeeringClient peeringClient) {
    this.phoneNumberVerifier = phoneNumberVerifier;
    this.peeringClient = peeringClient;
  }

  public void processMessage(String phoneNumber, String message) {
    ParseResult parseResult = new ParseResult(message);
    if (parseResult.isKoreduMessage()) {
      if (parseResult.isVerification()) {
        phoneNumberVerifier.verify(new Verification(parseResult.getToken(), phoneNumber));
      } else if (parseResult.isVerificationRequest()) {
        phoneNumberVerifier.reportPhoneNumber(parseResult.getToken(), phoneNumber);
      } else {
        peeringClient.requestSession(parseResult.getToken());
      }
    }
  }

  private static class ParseResult {

    private static final Pattern KOREDU_URL_PATTERN = Pattern.compile(".*(http://koreduno.appspot.com/.+)");
    private String token;
    private boolean verificationRequest = false;
    private boolean verification = false;
    private GeoPoint location;

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
      if (token.startsWith("?")) {
        verificationRequest = true;
        token = token.substring(1);
      } else if (token.startsWith("!")) {
        verification = true;
        token = token.substring(1);
      }

      if (verificationRequest || (url.getQuery() == null)) {
        location = null;
      } else {
        int latitudeE6 = -1;
        int longitudeE6 = -1;
        for (String keyValuePair : url.getQuery().split("&")) {
          String[] keyValue = keyValuePair.split("=");
          if ("lat".equalsIgnoreCase(keyValue[0])) {
            latitudeE6 = Integer.parseInt(keyValue[1]);
          } else if ("long".equalsIgnoreCase(keyValue[0])) {
            longitudeE6 = Integer.parseInt(keyValue[1]);
          }
        }
        location = new GeoPoint(latitudeE6, longitudeE6);
      }
    }

    public String getToken() {
      return token;
    }

    public GeoPoint getLocation() {
      return location;
    }

    public boolean isKoreduMessage() {
      return token != null;
    }

    public boolean isVerification() {
      return verification;
    }

    public boolean isVerificationRequest() {
      return verificationRequest;
    }

    public boolean hasLocation() {
      return location != null;
    }
  }
}