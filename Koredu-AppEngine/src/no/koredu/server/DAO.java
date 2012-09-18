package no.koredu.server;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DAO extends DAOBase {

  private static final Logger log = Logger.getLogger(DAO.class.getName());

  private final TokenFactory tokenFactory;

  public DAO(TokenFactory tokenFactory) {
    this.tokenFactory = tokenFactory;
  }

  public DAO() {
    this(new TokenFactory());
  }

  static {
    ObjectifyService.register(User.class);
    ObjectifyService.register(PeeringSession.class);
    ObjectifyService.register(PhoneNumberVerification.class);
    ObjectifyService.register(UserLocation.class);
  }

  public User getUserById(long userId) {
    return ofy().find(User.class, userId);
  }

  public User getUserByDeviceId(String deviceId) {
    return ofy().query(User.class)
        .filter("deviceId", deviceId)
        .get();
  }

  public User getUserByPhoneNumber(String phoneNumber) {
    return ofy().query(User.class)
        .filter("phoneNumber", phoneNumber)
        .get();
  }

  public User getOrCreateUser(String deviceId) {
    Objectify ofy = ObjectifyService.beginTransaction();
    try {
      User user = getUserByDeviceId(deviceId);
      if (user == null) {
        user = new User(deviceId);
        ofy.put(user);
      } else {
        log.info("using existing user " + user.getId());
      }
      log.info("committing txn");
      ofy.getTxn().commit();
      return user;
    } catch (RuntimeException e) {
      log.log(Level.WARNING, "Failed to get or create user with deviceId=" + deviceId, e);
      throw e;
    } finally {
      if (ofy.getTxn().isActive()) {
        log.info("rolling back txn");
        ofy.getTxn().rollback();
      }
    }
  }

  public PeeringSession putSession(final PeeringSession session) {
    log.info("Saving session " + session);
    User inviter = getOrCreateUser(session.getInviterDeviceId());
    log.info("found inviter " + inviter.getId() + " with phoneNumber=" + inviter.getPhoneNumber());
    session.setInviterId(inviter.getId());
    session.setInviterPhoneNumber(inviter.getPhoneNumber());
    session.setState(PeeringSession.State.CREATED);
    if (session.getInviteeId() == null) {
      log.info("no inviteeId, looking up by phone number " + session.getInviteePhoneNumber());
      User invitee = getUserByPhoneNumber(session.getInviteePhoneNumber());
      if (invitee != null) {
        log.info("found invitee " + invitee.getId());
        session.setInviteeId(invitee.getId());
        session.setInviteePhoneNumber(invitee.getPhoneNumber());
        session.setInviteeDeviceId(invitee.getDeviceId());
      }
    }
    if (needsToken(session)) {
      log.info("generating token for sms invite");
      PeeringSession storedSession = putWithNewToken(
          new Function<String, PeeringSession>() {
            @Override
            public PeeringSession apply(String inviteToken) {
              return getSessionByToken(inviteToken);
            }
          },
          new Function<String, PeeringSession>() {
            @Override
            public PeeringSession apply(String inviteToken) {
              session.setInviteToken(inviteToken);
              return session;
            }
          }
      );
      return storedSession;
    } else {
      ofy().put(session);
      return session;
    }
  }

  private boolean needsToken(final PeeringSession session) {
    return (session.getInviteeId() == null) || (session.getInviterPhoneNumber() == null);
  }

  private <T> T putWithNewToken(Function<String, T> objectFinder, Function<String, T> objectUpdater) {
    T updatedObject = null;
    String chosenToken = null;
    while (chosenToken == null) {
      String candidateToken = tokenFactory.nextRandomToken();
      Objectify ofy = ObjectifyService.beginTransaction();
      try {
        T existingObject = objectFinder.apply(candidateToken);
        if (existingObject == null) {
          chosenToken = candidateToken;
          log.info("found good token " + chosenToken);
          updatedObject = objectUpdater.apply(chosenToken);
          ofy.put(updatedObject);
        } else {
          log.info("token " + candidateToken + " is no good");
        }
        log.info("committing txn");
        ofy.getTxn().commit();
      } catch (RuntimeException e) {
        log.log(Level.WARNING, "Failed to save a session with a new token", e);
        throw e;
      } finally {
        if (ofy.getTxn().isActive()) {
          log.info("rolling back txn");
          ofy.getTxn().rollback();
        }
      }
    }
    return updatedObject;
  }

  public PeeringSession getSessionByToken(String inviteToken) {
    return getSessionByToken(ofy(), inviteToken);
  }

  private PeeringSession getSessionByToken(Objectify ofy, String inviteToken) {
    return ofy().query(PeeringSession.class)
        .filter("inviteToken", inviteToken)
        .get();
  }

  public PhoneNumberVerification createPhoneNumberVerification(final PhoneNumberVerification verification) {
    final User reportingUser = getUserByDeviceId(verification.getReportingDeviceId());
    if (reportingUser == null) {
      throw new RuntimeException("No reporting user found for reportingDeviceId " + verification.getReportingDeviceId());
    }
    verification.setReportingUserId(reportingUser.getId());
    boolean alreadyVerified = getUserById(verification.getUserId()).getPhoneNumber() != null;
    if (alreadyVerified) {
      verification.setVerified();
      ofy().put(verification);
      return verification;
    } else {
      return putWithNewToken(
          new Function<String, PhoneNumberVerification>() {
            @Override
            public PhoneNumberVerification apply(String verificationToken) {
              return getPhoneNumberVerificationByToken(verificationToken);
            }
          },
          new Function<String, PhoneNumberVerification>() {
            @Override
            public PhoneNumberVerification apply(String verificationToken) {
              verification.setToken(verificationToken);
              return verification;
            }
          }
      );
    }
  }

  public PhoneNumberVerification getPhoneNumberVerificationByToken(String verificationToken) {
    return getPhoneNumberVerificationByToken(ofy(), verificationToken);
  }

  private PhoneNumberVerification getPhoneNumberVerificationByToken(Objectify ofy, String verificationToken) {
    return ofy.query(PhoneNumberVerification.class)
        .filter("token", verificationToken)
        .get();
  }

  public void setPhoneNumber(Long userId, final String phoneNumber) {
    update(userId, User.class, new Function<User, User>() {
      @Override
      public User apply(User user) {
        user.setPhoneNumber(phoneNumber);
        return user;
      }
    });
    for (PeeringSession session : getActiveSessionsForInviter(userId)) {
      update(session.getId(), PeeringSession.class, new Function<PeeringSession, PeeringSession>() {
        @Override
        public PeeringSession apply(PeeringSession session) {
          session.setInviterPhoneNumber(phoneNumber);
          return session;
        }
      });
    }
    for (PeeringSession session : getActiveSessionsForInvitee(userId)) {
      update(session.getId(), PeeringSession.class, new Function<PeeringSession, PeeringSession>() {
        @Override
        public PeeringSession apply(PeeringSession session) {
          session.setInviteePhoneNumber(phoneNumber);
          return session;
        }
      });
    }

  }

  private PeeringSession setSessionState(Long sessionId, final PeeringSession.State state) {
    return update(sessionId, PeeringSession.class, new Function<PeeringSession, PeeringSession>() {
      @Override
      public PeeringSession apply(PeeringSession session) {
        session.setState(state);
        return session;
      }
    });
  }

  public <T> T update(Long id, Class<T> clazz, Function<T, T> updater) {
    Objectify ofy = ObjectifyService.beginTransaction();
    try {
      T persitentObject = ofy.get(clazz, id);
      updater.apply(persitentObject);
      ofy.put(persitentObject);
      ofy.getTxn().commit();
      return persitentObject;
    } finally {
      if (ofy.getTxn().isActive()) {
        log.info("rolling back txn");
        ofy.getTxn().rollback();
      }
    }
  }

  public PeeringSession setSessionInvitee(Long sessionId, final User invitee, final Integer inviteePeerId,
                                          final PeeringSession.State state) {
    return update(sessionId, PeeringSession.class,
        new Function<PeeringSession, PeeringSession>() {
          @Override
          public PeeringSession apply(PeeringSession session) {
            session.setInviteeId(invitee.getId());
            session.setInviteePhoneNumber(invitee.getPhoneNumber());
            session.setInviteeDeviceId(invitee.getDeviceId());
            session.setInviteePeerId(inviteePeerId);
            session.setState(state);
            return session;
          }
        });
  }

  public Iterable<PeeringSession> getPendingSessions(Long userId) {
    Iterable<PeeringSession> inviterSessions = ofy().query(PeeringSession.class)
        .filter("inviterId", userId)
        .filter("state", PeeringSession.State.PENDING_VERIFICATION);
    Iterable<PeeringSession> inviteeSessions = ofy().query(PeeringSession.class)
        .filter("inviteeId", userId)
        .filter("state", PeeringSession.State.PENDING_VERIFICATION);
    return Iterables.concat(inviterSessions, inviteeSessions);
  }

  public PeeringSession setSessionApproval(long sessionId, boolean approved) {
    PeeringSession.State newState = approved ? PeeringSession.State.APPROVED : PeeringSession.State.DENIED;
    return setSessionState(sessionId, newState);
  }

  public PeeringSession setSessionState(long sessionId, PeeringSession.State newState) {
    Objectify ofy = ObjectifyService.beginTransaction();
    try {
      PeeringSession session = ofy.get(PeeringSession.class, sessionId);
      session.setState(newState);
      ofy.put(session);
      ofy.getTxn().commit();
      return session;
    } finally {
      if (ofy.getTxn().isActive()) {
        log.info("rolling back txn");
        ofy.getTxn().rollback();
      }
    }
  }

  public void putLocation(UserLocation userLocation) {
    ofy().put(userLocation);
  }

  public Iterable<PeeringSession> getActiveSessionsForInviter(Long inviterId) {
    return ofy().query(PeeringSession.class)
        .filter("inviterId", inviterId);
  }

  public Iterable<PeeringSession> getActiveSessionsForInvitee(Long inviteeId) {
    return ofy().query(PeeringSession.class)
        .filter("inviteeId", inviteeId);
  }

  public PhoneNumberVerification setPhoneNumberToVerify(Long phoneNumberVerificationId, final String phoneNumber) {
    return update(phoneNumberVerificationId, PhoneNumberVerification.class,
        new Function<PhoneNumberVerification, PhoneNumberVerification>() {
          @Override
          public PhoneNumberVerification apply(PhoneNumberVerification phoneNumberVerification) {
            phoneNumberVerification.setPhoneNumber(phoneNumber);
            return phoneNumberVerification;
          }
        });
  }

  public PhoneNumberVerification setPhoneNumberVerified(Long phoneNumberVerificationId) {
    return update(phoneNumberVerificationId, PhoneNumberVerification.class,
        new Function<PhoneNumberVerification, PhoneNumberVerification>() {
          @Override
          public PhoneNumberVerification apply(PhoneNumberVerification phoneNumberVerification) {
            phoneNumberVerification.setVerified();
            return phoneNumberVerification;
          }
        });
  }

  public Iterable<PeeringSession> getAllSessions() {
    return ofy().query(PeeringSession.class);
  }
}
