package no.koredu.server;

import com.google.appengine.api.users.User;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;

import java.util.List;
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

  public long registerDevice(String deviceId, com.google.appengine.api.users.User gaiaUser) {
    // TODO: get Gaia user, look up User, add deviceId to the user
    log.info("Gaia user is " + gaiaUser);
    if (gaiaUser != null) {
      log.info("Gaia nickname=" + gaiaUser.getNickname());
    }
    KoreduUser user = dao.getOrCreateUser(deviceId);
    return user.getId();
  }

  public void createSession(PeeringSession session, User user) {
    log.info("createSession called");
    PeeringSession storedSession = dao.putSession(session);
    String token = storedSession.getInviteToken();
    log.info("createSession got token " + token);
    if (token != null) {
      String smsMessage = "Where are you? http://koreduno.appspot.com/" + token;
      objectPusher.pushSmsCommand(storedSession.getInviteePhoneNumber(), smsMessage, storedSession.getInviterDeviceId());
    } else {
      // TODO: include inviteeDeviceId in session but strip deviceIds when converting to json?
      KoreduUser invitee = dao.getUserById(storedSession.getInviteeId());
      objectPusher.pushObject("CONFIRM_SESSION", storedSession, invitee.getDeviceId());
    }
  }

  public void reportPhoneNumber(Verification incomingVerification, User user) {
    PhoneNumberVerification verification = dao.getPhoneNumberVerificationByToken(incomingVerification.getId());
    verification = dao.setPhoneNumberToVerify(verification.getId(), incomingVerification.getPhoneNumber());
    KoreduUser userToBeVerified = dao.getUserById(verification.getUserId());
    String smsMessage = createVerificationMessage(verification.getToken());
    Verification outgoingVerification = new Verification(verification.getToken(), verification.getReportingPeerId());
    objectPusher.pushObject("VERIFY", outgoingVerification, userToBeVerified.getDeviceId());
    objectPusher.pushSmsCommand(verification.getPhoneNumber(), smsMessage, incomingVerification.getDeviceId());
  }

  public void reportVerifiedPhoneNumber(Verification verification, User user) {
    PhoneNumberVerification phoneNumberVerification = dao.getPhoneNumberVerificationByToken(verification.getId());
    if (phoneNumberVerification == null) {
      throw new RuntimeException("No PhoneNumberVerification found for token " + verification.getId());
    }
    // TODO: use logged in user to check that the user is existingVerification.getUserId()
    KoreduUser koreduUser = dao.getUserByDeviceId(verification.getDeviceId());
    if (user == null) {
      throw new RuntimeException("No user found for verifiedAtDeviceId=" + verification.getDeviceId());
    }
    if (!phoneNumberVerification.getUserId().equals(koreduUser.getId())) {
      throw new RuntimeException("User mismatch when verifying " + phoneNumberVerification.getPhoneNumber()
          + ", expected: " + phoneNumberVerification.getUserId() + ", was: " + koreduUser.getId());
    }
    KoreduUser reportingUser = dao.getUserById(phoneNumberVerification.getReportingUserId());
    if (reportingUser == null) {
      throw new RuntimeException("No reporting user found for reportingUserId=" + phoneNumberVerification.getReportingUserId());
    }
    if (phoneNumberVerification.isVerified()) {
      log.warning("Attempt to verify already-verified " + phoneNumberVerification.getPhoneNumber()
          + ". User=" + koreduUser.getId() + ", verification=" + phoneNumberVerification.getId());
    }
    phoneNumberVerification.setVerified();
    dao.setPhoneNumberVerified(phoneNumberVerification.getId());
    dao.setPhoneNumber(koreduUser.getId(), phoneNumberVerification.getPhoneNumber());
    // find all pending sessions,  push them and set the REQUESTED
    List<PeeringSession> allSessions = Lists.newArrayList(dao.getAllSessions());
    Iterable<PeeringSession> pendingInviterSessions = dao.getPendingSessions(koreduUser.getId());
    for (PeeringSession session : pendingInviterSessions) {
      if (session.getInviteePhoneNumber() != null) {
        dao.setSessionState(session.getId(), PeeringSession.State.REQUESTED);
        objectPusher.pushObject("CONFIRM_SESSION", session, session.getInviteeDeviceId());
      } else {
        KoreduUser invitee = dao.getUserById(session.getInviteeId());
        requestInviteeVerification(invitee, phoneNumberVerification.getPhoneNumber(), invitee.getDeviceId(), session);
      }
    }
  }

  public void requestSession(PeeringSession tokenHolder, User user) {
    log.info("requestSession called");
    String token = Preconditions.checkNotNull(tokenHolder.getInviteToken());
    String phoneNumber = Preconditions.checkNotNull(tokenHolder.getInviterPhoneNumber());
    String deviceId = Preconditions.checkNotNull(tokenHolder.getInviteeDeviceId());
    Integer inviteePeerId = Preconditions.checkNotNull(tokenHolder.getInviteePeerId());

    PeeringSession session = dao.getSessionByToken(token);
    if (session == null) {
      throw new RuntimeException("No session found for token '" + token);
    }
    KoreduUser invitee = dao.getOrCreateUser(deviceId);
    verifyCorrectInvitee(session, invitee);
    verifyCorrectPhoneNumber(session, phoneNumber);

    PeeringSession.State newState = PeeringSession.State.REQUESTED;
    if ((session.getInviterPhoneNumber() == null) || (invitee.getPhoneNumber() == null)) {
      newState = PeeringSession.State.PENDING_VERIFICATION;
    }
    session = dao.setSessionInvitee(session.getId(), invitee, inviteePeerId, newState);

    // phoneNumber is the tentative phone number of the inviter
    if (session.getInviterPhoneNumber() == null) {
      requestInviterPhoneNumberVerification(phoneNumber, deviceId, session);
    }
    if (invitee.getPhoneNumber() == null) {
      requestInviteeVerification(invitee, phoneNumber, deviceId, session);
    }
    if (newState == PeeringSession.State.REQUESTED) {
      objectPusher.pushObject("CONFIRM_SESSION", session, deviceId);
    }
  }

  public void approveSession(long sessionId, boolean approved, User user) {
    PeeringSession session = dao.setSessionApproval(sessionId, approved);
    String action = approved ? "SESSION_CONFIRMED" : "SESSION_DENIED";
    objectPusher.pushObject(action, session, session.getInviterDeviceId());
  }

  public void publishLocation(UserLocation userLocation, User user) {
    if (userLocation.getDeviceId() == null) {
      log.warning("null deviceId when publishing location, ignoring");
      return;
    }
    KoreduUser koreduUser = dao.getUserByDeviceId(userLocation.getDeviceId());
    if (user == null) {
      log.warning("null userId tried to publish location, ignoring");
      return;
    }
    userLocation.setUserId(koreduUser.getId());
    dao.putLocation(userLocation);
    log.warning(("Finding sessions where " + koreduUser.getId() + " is inviter:"));
    Iterable<PeeringSession> inviterSessions = dao.getActiveSessionsForInviter(koreduUser.getId());
    for (PeeringSession session : inviterSessions) {
      log.warning("Sending location of " + userLocation.getUserId() + " to " + session.getInviteeId());
      // TODO: when multiple devices per user is supported, get all devices for user
      objectPusher.pushObject("LOCATION_UPDATE", userLocation, session.getInviteeDeviceId());
    }
    log.warning(("Finding sessions where " + koreduUser.getId() + " is invitee:"));
    Iterable<PeeringSession> inviteeSessions = dao.getActiveSessionsForInvitee(koreduUser.getId());
    for (PeeringSession session : inviteeSessions) {
      log.warning("Sending location of " + userLocation.getUserId() + " to " + session.getInviterId());
      // TODO: when multiple devices per user is supported, get all devices for user
      objectPusher.pushObject("LOCATION_UPDATE", userLocation, session.getInviterDeviceId());
    }
  }

  private void verifyPhoneNumberCandidate(PhoneNumberVerification verification) {
    Preconditions.checkNotNull(verification.getPhoneNumber());
    String smsMessage = createVerificationMessage(verification.getToken());
    KoreduUser user = dao.getUserById(verification.getUserId());
    Verification clientVerification = new Verification(verification.getToken(), verification.getReportingPeerId());
    objectPusher.pushObject("VERIFY", clientVerification, user.getDeviceId());
    objectPusher.pushSmsCommand(verification.getPhoneNumber(), smsMessage, verification.getReportingDeviceId());
  }

  private String createVerificationMessage(String token) {
    return "Koredu one-time verification: http://koreduno.appspot.com/!" + token;
  }

  private String createVerificationRequestMessage(String token) {
    return "Koredu one-time verification: http://koreduno.appspot.com/?" + token;
  }

  private void requestInviterPhoneNumberVerification(String inviterPhoneNumber, String inviteeDeviceId,
                                                     PeeringSession session) {
    PhoneNumberVerification verification =
        new PhoneNumberVerification(session.getInviterId(), inviterPhoneNumber, session.getInviteePeerId(),
            inviteeDeviceId);
    verification = dao.createPhoneNumberVerification(verification);
    if (!verification.isVerified()) {
      verifyPhoneNumberCandidate(verification);
    }
  }

  private void requestInviteeVerification(KoreduUser invitee, String inviterPhoneNumber, String inviteeDeviceId,
                                          PeeringSession session) {
    // we don't have a candiate number, so we need to get invitee to send an SMS to inviter
    PhoneNumberVerification verification =
        new PhoneNumberVerification(invitee.getId(), null, session.getInviteePeerId(), session.getInviterDeviceId());
    verification = dao.createPhoneNumberVerification(verification);
    if (!verification.isVerified()) {
      String smsMessage = createVerificationRequestMessage(verification.getToken());
      objectPusher.pushSmsCommand(inviterPhoneNumber, smsMessage, inviteeDeviceId);
    }
  }

  private void verifyCorrectInvitee(PeeringSession session, KoreduUser invitee) {
    if ((invitee != null) && (session.getInviteeId() != null) && !invitee.getId().equals(session.getInviteeId())) {
      if ((invitee != null) && (invitee.getId() != session.getInviteeId())) {
        throw new RuntimeException("User " + invitee.getId() + " tried to get session with token "
            + session.getInviteToken() + " but session's invitee is " + session.getInviteeId());
      }
    }
  }

  private void verifyCorrectPhoneNumber(PeeringSession session, String inviterPhoneNumber) {
    if ((session.getInviterPhoneNumber() != null) && !session.getInviterPhoneNumber().equals(inviterPhoneNumber)) {
      throw new RuntimeException("Invalid request to get session by token " + session.getInviteToken()
          + ", inviter phone number " + inviterPhoneNumber + " does not match session's inviterPhoneNumber "
          + session.getInviterPhoneNumber());
    }
  }

  public DAO getDao() {
    return dao;
  }

}
