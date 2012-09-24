package no.koredu.server;

import com.google.common.base.Function;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;
import no.koredu.common.Invite;
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
    ObjectifyService.register(Invite.class);
    ObjectifyService.register(KoreduUser.class);
    ObjectifyService.register(PeeringSession.class);
    ObjectifyService.register(PhoneNumberVerification.class);
    ObjectifyService.register(UserLocation.class);
  }

  public KoreduUser getUserById(long userId) {
    return ofy().find(KoreduUser.class, userId);
  }

  public KoreduUser getUser(String deviceId) {
    return ofy().query(KoreduUser.class)
        .filter("deviceId", deviceId)
        .get();
  }

  public KoreduUser getOrCreateUser(String deviceId) {
    Objectify ofy = ObjectifyService.beginTransaction();
    try {
      KoreduUser user = getUser(deviceId);
      if (user == null) {
        user = new KoreduUser(deviceId);
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

  public PeeringSession createSession(final PeeringSession session) {
    log.info("Saving session " + session);
    KoreduUser inviter = getOrCreateUser(session.getInviterDeviceId());
    session.setInviterId(inviter.getId());
    session.setState(PeeringSession.State.CREATED);
    ofy().put(session);
    return session;
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

  public Iterable<PeeringSession> getAllSessions() {
    return ofy().query(PeeringSession.class);
  }

  public Invite putInvite(final Invite invite) {
    return putWithNewToken(
        new Function<String, Invite>() {
          @Override
          public Invite apply(String token) {
            return getInviteByToken(token);
          }
        },
        new Function<String, Invite>() {
          @Override
          public Invite apply(String token) {
            invite.setToken(token);
            return invite;
          }
        }
    );
  }

  public Invite getInviteByToken(String token) {
    return ofy().query(Invite.class).filter("token", token).get();

  }

}
