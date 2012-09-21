package no.koredu.testing;

import com.google.appengine.api.users.User;
import no.koredu.android.ObjectSender;
import no.koredu.common.InviteReply;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;
import no.koredu.server.KoreduApi;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class DirectObjectSender implements ObjectSender {

  private final User user;
  private final String deviceId;
  private final ActionTracker tracker;
  private KoreduApi koreduApi;

  public DirectObjectSender(String deviceId, ActionTracker tracker) {
    this.user = new User(deviceId + "@gmail.com", "gmail.com");
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
      koreduApi.createSession((PeeringSession) object, user);
    } else if ("/requestSession".equals(path)) {
      koreduApi.requestSession((InviteReply) object, user);
    } else if ("/approveSession".equals(path)) {
      koreduApi.approveSession(Long.parseLong((String) object), true, user);
    } else if ("/denySession".equals(path)) {
      koreduApi.approveSession(Long.parseLong((String) object), false, user);
    } else if ("/publishLocation".equals(path)) {
      koreduApi.publishLocation((UserLocation) object, user);
    } else {
      throw new IllegalArgumentException("unknown path " + path);
    }
  }
}
