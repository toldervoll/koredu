package no.koredu.server;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TokenFactoryTest {

  @Test
  public void testTokens() {
	TokenFactory factory = new TokenFactory();
	String token = factory.nextRandomToken();
	assertTrue("Token " + token + " longer than 4 charachters", token.length() <= 4);
  }

}
