package no.koredu.server;

import com.google.appengine.api.users.User;
import com.google.common.base.Function;
import no.koredu.common.Invite;
import no.koredu.common.InviteReply;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;

import java.util.logging.Logger;

public class KoreduApi {

  private static final Logger log = Logger.getLogger(KoreduApi.class.getName());

  private final DAO dao;
  private final ObjectPusher objectPusher;

  public KoreduApi() {
    this(new DAO(), new GCMObjectPusher());
  }

  public KoreduApi(DAO dao, ObjectPusher objectPusher) {
    this.dao = dao;
    this.objectPusher = objectPusher;
  }

  public void createSession(PeeringSession session, User user) {
    //log.info("createSession called with user=" + user + ", session=" + session);
    PeeringSession storedSession = dao.createSession(session);
    Invite invite = new Invite(storedSession.getInviteePhoneNumber(), storedSession.getId());
    Invite storedInvite = dao.putInvite(invite);
    String token = storedInvite.getToken();
    log.info("createSession got token " + token);
    // TODO: Use invitee's language in SMS. May want to let client generate string since Android can handle i18n
    String smsMessage = "Where are you? http://koreduno.appspot.com/" + token;
    objectPusher.pushSmsCommand(storedSession.getInviteePhoneNumber(), smsMessage, storedSession.getInviterDeviceId());
  }

  public void requestSession(final InviteReply inviteReply, User user) {
    log.info("requestSession called");
    Invite invite = dao.getInviteByToken(inviteReply.getToken());
    if (invite == null) {
      throw new RuntimeException("No invite found for " + inviteReply);
    }
    final KoreduUser invitee = dao.getUser(inviteReply.getInviteeDeviceId());
    PeeringSession session =
        dao.update(invite.getSessionId(), PeeringSession.class, new Function<PeeringSession, PeeringSession>() {
          @Override
          public PeeringSession apply(PeeringSession storedSession) {
            storedSession.setInviteeId(invitee.getId());
            storedSession.setInviteeDeviceId(inviteReply.getInviteeDeviceId());
            storedSession.setInviterPhoneNumber(inviteReply.getInviterPhoneNumber());
            storedSession.setState(PeeringSession.State.REQUESTED);
            return storedSession;
          }
        });
    objectPusher.pushObject("CONFIRM_SESSION", session, inviteReply.getInviteeDeviceId());
  }

  public void approveSession(long sessionId, boolean approved, User user) {
    PeeringSession session = dao.setSessionApproval(sessionId, approved);
    KoreduUser invitee = dao.getUserById(session.getInviteeId());
    final PeeringSession.State newState;
    String pushAction;
    if (approved) {
      newState = PeeringSession.State.APPROVED;
      pushAction = "SESSION_CONFIRMED";
    } else {
      newState = PeeringSession.State.DENIED;
      pushAction = "SESSION_DENIED";
    }
    dao.update(session.getId(), PeeringSession.class,
        new Function<PeeringSession, PeeringSession>() {
          @Override
          public PeeringSession apply(PeeringSession storedSession) {
            storedSession.setState(newState);
            return storedSession;
          }
        });
    objectPusher.pushObject(pushAction, session, session.getInviterDeviceId());
  }

  public void publishLocation(UserLocation userLocation, User user) {
    if (userLocation.getDeviceId() == null) {
      log.warning("null deviceId when publishing location, ignoring");
      return;
    }
    KoreduUser koreduUser = dao.getUser(userLocation.getDeviceId());
    if (koreduUser == null) {
      log.warning("unknown tried to publish location, ignoring. deviceId=" + userLocation.getDeviceId());
      return;
    }
    userLocation.setUserId(koreduUser.getId());
    dao.putLocation(userLocation);
    log.warning(("Finding sessions where " + koreduUser.getId() + " is inviter:"));
    Iterable<PeeringSession> inviterSessions = dao.getActiveSessionsForInviter(koreduUser.getId());
    for (PeeringSession session : inviterSessions) {
      log.warning("Sending location of " + userLocation.getUserId() + " to " + session.getInviteeId());
      objectPusher.pushObject("LOCATION_UPDATE", userLocation, session.getInviteeDeviceId());
    }
    log.warning(("Finding sessions where " + koreduUser.getId() + " is invitee:"));
    Iterable<PeeringSession> inviteeSessions = dao.getActiveSessionsForInvitee(koreduUser.getId());
    for (PeeringSession session : inviteeSessions) {
      log.warning("Sending location of " + userLocation.getUserId() + " to " + session.getInviterId());
      objectPusher.pushObject("LOCATION_UPDATE", userLocation, session.getInviterDeviceId());
    }
  }

  public DAO getDao() {
    return dao;
  }

}
