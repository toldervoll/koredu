package no.koredu.server;

import no.koredu.common.Sanitizable;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public interface ObjectPusher {
  void pushSmsCommand(String phoneNumber, String smsMessage, String... deviceId);

  void pushObject(String action, Sanitizable object, String... deviceId);
}
