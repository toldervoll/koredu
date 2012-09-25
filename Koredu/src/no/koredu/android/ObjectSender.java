package no.koredu.android;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public interface ObjectSender {

  void send(String path, Object object);

  String syncSend(String path, Object object);

}
