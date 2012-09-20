package no.koredu.android;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public interface AccountProvider {
  String getAccountName();

  String getAuthenticationCookie();
}
