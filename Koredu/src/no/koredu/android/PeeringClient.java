package no.koredu.android;

import com.squareup.otto.Bus;
import no.koredu.android.database.DatabaseManager;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;

/**
 * Client-side logic for peering. Should not have any Android dependencies.
 *
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PeeringClient {

  private final DatabaseManager db;
  private final DeviceIdProvider deviceIdProvider;
  private final DisplayNameResolver displayNameResolver;
  private final ObjectSender objectSender;
  private final LocationPublisher locationPublisher;
  private final UserInteraction userInteraction;
  private final PhoneNumberVerifier phoneNumberVerifier;
  private final Bus bus;

  public PeeringClient(DatabaseManager db, DeviceIdProvider deviceIdProvider, DisplayNameResolver displayNameResolver,
                       ObjectSender objectSender, LocationPublisher locationPublisher, UserInteraction userInteraction, PhoneNumberVerifier phoneNumberVerifier, Bus bus) {
    this.db = db;
    this.deviceIdProvider = deviceIdProvider;
    this.displayNameResolver = displayNameResolver;
    this.objectSender = objectSender;
    this.locationPublisher = locationPublisher;
    this.userInteraction = userInteraction;
    this.phoneNumberVerifier = phoneNumberVerifier;
    this.bus = bus;
  }

  public void sendInvite(int peerId) {
    Peer peer = db.getPeer(peerId);
    // TODO: let user specify duration
    PeeringSession session =
        new PeeringSession(deviceIdProvider.get(), peer.getId(), peer.getUserId(), peer.getPhoneNumber(), 60);
    objectSender.send("/createSession", session);
  }

  public void requestSession(String inviteToken, String inviterPhoneNumber) {
    Peer peer = db.getPeerByPhoneNumber(inviterPhoneNumber);
    if (peer == null) {
      String displayName = displayNameResolver.getDisplayName(inviterPhoneNumber);
      peer = db.putPeer(new Peer(displayName, inviterPhoneNumber));
    }
    PeeringSession session =
        PeeringSession.fromToken(inviteToken, inviterPhoneNumber, peer.getId(), deviceIdProvider.get());
    objectSender.send("/requestSession", session);
  }

  public void askWhetherToAllowSession(PeeringSession session) {
    userInteraction.askWhetherToAllowSession(session);
  }

  public void handleSessionConfirmation(PeeringSession session, boolean approved) {
    String decision = approved ? "accepted" : "denied";
    Peer peer = db.getPeer(session.getInviterPeerId());
    peer.setUserId(session.getInviteeId());
    peer.setPhoneNumber(session.getInviteePhoneNumber());
    db.putPeer(peer);
    userInteraction.showNotification(peer.getDisplayName() + " " + decision + " your request to exchange locations");
    if (approved) {
      // TODO: make expiration configurable. Use 1 hour for now.
      locationPublisher.start();
    }
  }

  public void verifyPhoneNumber(Verification verification) {
    phoneNumberVerifier.verify(verification);
  }

  public void approveSession(long sessionId, boolean approved) {
    String path = approved ? "/approveSession" : "/denySession";
    objectSender.send(path, String.valueOf(sessionId));
    if (approved) {
      locationPublisher.start();
    }
  }

  public void receivePeerLocation(UserLocation userLocation) {
    Peer peer = db.getPeerByUserId(userLocation.getUserId());
    // TODO: error handling if peer is unknown
    if (peer != null) {
      userLocation.setDisplayName(peer.getDisplayName());
    }
    db.putLocation(userLocation);
    bus.post(new LocationUpdateEvent());
  }

}
