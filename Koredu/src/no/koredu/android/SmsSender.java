package no.koredu.android;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public interface SmsSender {
  void send(String phoneNumber, String message);
}
