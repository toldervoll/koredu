package no.koredu.testing;

import com.google.appengine.repackaged.com.google.common.collect.Maps;
import no.koredu.android.PeeringClient;
import no.koredu.android.SmsSender;
import no.koredu.common.PeeringSession;
import no.koredu.common.Sanitizable;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;
import no.koredu.server.ObjectPusher;

import java.util.Map;

/**
 * Fake implementation of GCM pushing and GCMIntentService handling of the messages.
 *
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class DirectObjectPusher implements ObjectPusher {

  private final ActionTracker tracker;
  private Map<String, SmsSender> smsSenders = Maps.newHashMap();
  private Map<String, PeeringClient> peeringsClients = Maps.newHashMap();

  public DirectObjectPusher(ActionTracker tracker) {
    this.tracker = tracker;
  }

  public void addSmsSender(String deviceId, SmsSender smsSender) {
    smsSenders.put(deviceId, smsSender);
  }

  public void addPeeringClient(String deviceId, PeeringClient peeringClient) {
    peeringsClients.put(deviceId, peeringClient);
  }

  @Override
  public void pushSmsCommand(String phoneNumber, String smsMessage, String... deviceIds) {
    for (String deviceId : deviceIds) {
      tracker.track("server", "SEND_SMS", deviceId);
      smsSenders.get(deviceId).send(phoneNumber, smsMessage);
    }
  }

  @Override
  public void pushObject(String action, Sanitizable object, String... deviceIds) {
    for (String deviceId : deviceIds) {
      tracker.track("server", action, deviceId);
      PeeringClient peeringClient = peeringsClients.get(deviceId);
      if ("CONFIRM_SESSION".equals(action)) {
        peeringClient.askWhetherToAllowSession((PeeringSession) object);
      } else if ("VERIFY".equals(action)) {
        peeringClient.verifyPhoneNumber((Verification) object);
      } else if ("SESSION_CONFIRMED".equals(action)) {
        peeringClient.handleSessionConfirmation((PeeringSession) object, true);
      } else if ("SESSION_DENIED".equals(action)) {
        peeringClient.handleSessionConfirmation((PeeringSession) object, false);
      } else if ("LOCATION_UPDATE".equals(action)) {
        peeringClient.receivePeerLocation((UserLocation) object);
      }
    }
  }

}
