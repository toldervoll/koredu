package no.koredu.server;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;

public class TokenFactory {

  private static final int TOKEN_BYTES = 3; // gives 6 Base64 characters, 69 billion combinations 
  private Random random = new Random();

  public String nextRandomToken() {
    byte[] randomBytes = new byte[TOKEN_BYTES];
    random.nextBytes(randomBytes);
    return Base64.encodeBase64URLSafeString(randomBytes);
  }

}
