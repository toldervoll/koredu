package no.koredu.testing;

import no.koredu.android.ObjectSender;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;
import no.koredu.server.KoreduApi;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class DirectObjectSender implements ObjectSender {

  private final String deviceId;
  private final ActionTracker tracker;
  private KoreduApi koreduApi;

  public DirectObjectSender(String deviceId, ActionTracker tracker) {
    this.deviceId = deviceId;
    this.tracker = tracker;
  }

  public void setKoreduApi(KoreduApi koreduApi) {
    this.koreduApi = koreduApi;
  }

  @Override
  public void send(String path, Object object) {
    tracker.track(deviceId, path, "server");
    if ("/createSession".equals(path)) {
      koreduApi.createSession((PeeringSession) object);
    } else if ("/requestSession".equals(path)) {
      koreduApi.requestSession((PeeringSession) object);
    } else if ("/approveSession".equals(path)) {
      koreduApi.approveSession(Long.parseLong((String) object), true);
    } else if ("/denySession".equals(path)) {
      koreduApi.approveSession(Long.parseLong((String) object), false);
    } else if ("/publishLocation".equals(path)) {
      koreduApi.publishLocation((UserLocation) object);
    } else if ("/reportPhoneNumber".equals(path)) {
      koreduApi.reportPhoneNumber((Verification) object);
    } else if ("/reportVerified".equals(path)) {
      koreduApi.reportVerifiedPhoneNumber((Verification) object);
    } else {
      throw new IllegalArgumentException("unknown path " + path);
    }
  }
}
