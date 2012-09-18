package no.koredu.server;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public interface ObjectPusher {
  void pushSmsCommand(String phoneNumber, String smsMessage, String deviceId);
  <T> void pushObject(String action, T object, String deviceId);
}
