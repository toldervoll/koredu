package no.koredu.common;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import no.koredu.android.Peer;
import no.koredu.server.DAO;
import no.koredu.server.KoreduApi;
import no.koredu.server.TokenFactory;
import no.koredu.testing.ActionTracker;
import no.koredu.testing.DirectObjectPusher;
import no.koredu.testing.MockKoreduClient;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PeeringTest {

  private static final String INVITER_GAIA_ID = "1";
  private static final String INVITER_NAME = "Thomas Oldervoll";
  private static final String INVITER_DEVICE_ID = "inviter";
  private static final String INVITER_CANONICAL_PHONE_NUMBER = "+4748193450";
  private static final String INVITER_PHONE_NUMBER = "48 19 34 50";

  private static final String INVITEE_GAIA_ID = "2";
  private static final String INVITEE_NAME = "Ann Oldervoll";
  private static final String INVITEE_DEVICE_ID = "invitee";
  private static final String INVITEE_CANONICAL_PHONE_NUMBER = "+4746938849";
  private static final String INVITEE_PHONE_NUMBER = "469 38 849";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private IMocksControl mocksControl;
  private ActionTracker tracker;
  private MockKoreduClient inviter;
  private MockKoreduClient invitee;
  private Peer inviterPeer;
  private Peer inviteePeer;
  private TokenFactory tokenFactory;
  private DirectObjectPusher objectPusher;
  private KoreduApi koreduApi;

  @Before
  public void setUp() {
    helper.setUp();
    mocksControl = EasyMock.createStrictControl();
    tracker = new ActionTracker();

    // inviter client
    inviter = new MockKoreduClient(INVITER_DEVICE_ID, INVITER_CANONICAL_PHONE_NUMBER, mocksControl, tracker);

    // invitee client
    invitee = new MockKoreduClient(INVITEE_DEVICE_ID, INVITEE_CANONICAL_PHONE_NUMBER, mocksControl, tracker);

    // server
    tokenFactory = mocksControl.createMock(TokenFactory.class);
    objectPusher = new DirectObjectPusher(tracker);
    koreduApi = new KoreduApi(new DAO(tokenFactory), objectPusher);

    // wire inviter client and server
    inviter.setKoreduApi(koreduApi);
    objectPusher.addSmsSender(INVITER_DEVICE_ID, inviter.getSmsSender());
    objectPusher.addPeeringClient(INVITER_DEVICE_ID, inviter.getPeeringClient());

    // wire invitee client and server
    invitee.setKoreduApi(koreduApi);
    objectPusher.addSmsSender(INVITEE_DEVICE_ID, invitee.getSmsSender());
    objectPusher.addPeeringClient(INVITEE_DEVICE_ID, invitee.getPeeringClient());

    // wire inviter and invitee
    inviter.getSmsSender().addSmsProcessor(INVITEE_CANONICAL_PHONE_NUMBER, invitee.getSmsProcessor());
    invitee.getSmsSender().addSmsProcessor(INVITER_CANONICAL_PHONE_NUMBER, inviter.getSmsProcessor());
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testTest() {
    assertTrue(true);
  }

  @Test
  public void testInvite_bothKnown() {
    try {
      inviterPeer = new Peer("Ann Oldervoll", INVITEE_CANONICAL_PHONE_NUMBER);  // peer of inviter, i.e. invitee
      inviterPeer.setId(1);
      inviteePeer = new Peer("Thomas Oldervoll", INVITER_CANONICAL_PHONE_NUMBER); // peer of invitee, i.e. inviter
      inviteePeer.setId(2);
      koreduApi.getDao().getOrCreateUser(INVITER_GAIA_ID, INVITER_NAME, INVITER_DEVICE_ID);
      koreduApi.getDao().getOrCreateUser(INVITEE_GAIA_ID, INVITEE_NAME, INVITEE_DEVICE_ID);

      Capture<PeeringSession> sessionCapture = new Capture<PeeringSession>();
      Capture<String> confirmationCapture = new Capture<String>();
      Capture<Peer> peerCapture = new Capture<Peer>();

      // inviter creates session
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // server send CONFIRM_SESSION to invitee
      invitee.getUserInteraction().askWhetherToAllowSession(capture(sessionCapture), false);
      // invitee confirms, server sends SESSION_CONFIRMED to inviter
      // inviter shows confirmation and starts publishing locations
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      expect(inviter.getDb().putPeer(capture(peerCapture))).andReturn(inviterPeer);
      inviter.getUserInteraction().showNotification(capture(confirmationCapture));
      inviter.getLocationPublisher().start();
      // invitee starts publishing locations
      invitee.getLocationPublisher().start();

      mocksControl.replay();
      inviter.getPeeringClient().sendInvite(1);
      PeeringSession session = sessionCapture.getValue();
      invitee.getPeeringClient().approveSession(session.getId(), true);
      mocksControl.verify();

    } finally {
      System.out.println("Summary:");
      for (String action : tracker.getActions()) {
        System.out.println(action);
      }
    }
  }

  @Test
  public void testInvite_onlyInviterKnown() {
    try {
      // inviterPeer is unknown since the phone number does not match due to different formatting
      inviterPeer = new Peer("Ann Oldervoll", INVITEE_PHONE_NUMBER);  // peer of inviter, i.e. invitee
      inviterPeer.setId(1);
      inviteePeer = new Peer("Thomas Oldervoll", INVITER_CANONICAL_PHONE_NUMBER); // peer of invitee, i.e. inviter
      inviteePeer.setId(2);
      koreduApi.getDao().getOrCreateUser(INVITER_GAIA_ID, INVITER_NAME, INVITER_DEVICE_ID);
      koreduApi.getDao().getOrCreateUser(INVITEE_GAIA_ID, INVITEE_NAME, INVITEE_DEVICE_ID);

      Capture<PeeringSession> sessionCapture = new Capture<PeeringSession>();
      Capture<String> confirmationCapture = new Capture<String>();
      Capture<Peer> peerCapture = new Capture<Peer>();

      // inviter creates session
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // server sends token to inviter
      expect(tokenFactory.nextRandomToken()).andReturn("token1");
      // inviter sends SMS to invitee, who looks up the session
      expect(invitee.getDb().getPeerByPhoneNumber(INVITER_CANONICAL_PHONE_NUMBER)).andReturn(inviteePeer);
      // server send CONFIRM_SESSION to invitee
      invitee.getUserInteraction().askWhetherToAllowSession(capture(sessionCapture), false);
      // invitee confirms, server sends SESSION_CONFIRMED to inviter
      // inviter shows confirmation and starts publishing locations
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      expect(inviter.getDb().putPeer(capture(peerCapture))).andReturn(inviterPeer);
      inviter.getUserInteraction().showNotification(capture(confirmationCapture));
      inviter.getLocationPublisher().start();
      // invitee starts publishing locations
      invitee.getLocationPublisher().start();

      mocksControl.replay();
      inviter.getPeeringClient().sendInvite(1);
      PeeringSession session = sessionCapture.getValue();
      invitee.getPeeringClient().approveSession(session.getId(), true);
      mocksControl.verify();
      Peer updatedPeer = peerCapture.getValue();
      assertNotNull(updatedPeer.getUserId());
      assertEquals(INVITEE_CANONICAL_PHONE_NUMBER, updatedPeer.getPhoneNumber());

    } finally {
      System.out.println("Summary:");
      for (String action : tracker.getActions()) {
        System.out.println(action);
      }
    }
  }

  @Test
  public void testInvite_knownInviterNewInvitee() {
    try {
      // inviterPeer is unknown since no phone number is known (maybe not a user yet)
      inviterPeer = new Peer("Ann Oldervoll", INVITEE_PHONE_NUMBER);  // peer of inviter, i.e. invitee
      inviterPeer.setId(1);
      inviteePeer = new Peer("Thomas Oldervoll", INVITER_CANONICAL_PHONE_NUMBER); // peer of invitee, i.e. inviter
      inviteePeer.setId(2);
      koreduApi.getDao().getOrCreateUser(INVITER_GAIA_ID, INVITER_NAME, INVITER_DEVICE_ID);

      Capture<PeeringSession> sessionCapture = new Capture<PeeringSession>();
      Capture<String> confirmationCapture = new Capture<String>();
      Capture<Peer> peerCapture = new Capture<Peer>();
      Capture<Verification> verificationCapture = new Capture<Verification>();

      // inviter creates session
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // server sends token to inviter
      expect(tokenFactory.nextRandomToken()).andReturn("token1");
      // inviter sends SMS to invitee, who tries to look up the session
      expect(invitee.getDb().getPeerByPhoneNumber(INVITER_CANONICAL_PHONE_NUMBER)).andReturn(inviteePeer);
      // to confirm invitee's phone number, server asks invitee to send SMS to inviter and inviter to reply
      expect(tokenFactory.nextRandomToken()).andReturn("token2");
      expect(invitee.getDb().getVerification("token2")).andReturn(null);
      invitee.getDb().putVerification(capture(verificationCapture));
      Verification verificationFromServer = new Verification("token2", 2);
      expect(invitee.getDb().getVerification("token2")).andReturn(verificationFromServer);
      expect(invitee.getDb().getPeer(2)).andReturn(inviteePeer);
      // invitee sends /reportVerified, server sends CONFIRM_SESSION to invitee
      invitee.getUserInteraction().askWhetherToAllowSession(capture(sessionCapture), false);
      // invitee confirms, server sends SESSION_CONFIRMED to inviter
      // inviter shows confirmation and starts publishing locations
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      expect(inviter.getDb().putPeer(capture(peerCapture))).andReturn(inviterPeer);
      inviter.getUserInteraction().showNotification(capture(confirmationCapture));
      inviter.getLocationPublisher().start();
      // invitee starts publishing locations
      invitee.getLocationPublisher().start();

      mocksControl.replay();
      inviter.getPeeringClient().sendInvite(1);
      PeeringSession session = sessionCapture.getValue();
      invitee.getPeeringClient().approveSession(session.getId(), true);
      mocksControl.verify();
      Peer updatedPeer = peerCapture.getValue();
      assertNotNull(updatedPeer.getUserId());
      assertEquals(INVITEE_CANONICAL_PHONE_NUMBER, updatedPeer.getPhoneNumber());

    } finally {
      System.out.println("Summary:");
      for (String action : tracker.getActions()) {
        System.out.println(action);
      }
    }
  }

  @Test
  public void testInvite_onlyInviteeKnown() {
    try {
      // inviterPeer is unknown since no phone number is known (maybe not a user yet)
      inviterPeer = new Peer("Ann Oldervoll", INVITEE_CANONICAL_PHONE_NUMBER);  // peer of inviter, i.e. invitee
      inviterPeer.setId(1);
      inviteePeer = new Peer("Thomas Oldervoll", INVITER_CANONICAL_PHONE_NUMBER); // peer of invitee, i.e. inviter
      inviteePeer.setId(2);
      koreduApi.getDao().getOrCreateUser(INVITEE_GAIA_ID, INVITEE_NAME, INVITEE_DEVICE_ID);

      Capture<PeeringSession> sessionCapture = new Capture<PeeringSession>();
      Capture<String> confirmationCapture = new Capture<String>();
      Capture<Peer> peerCapture = new Capture<Peer>();
      Capture<Verification> verificationCapture = new Capture<Verification>();

      // inviter creates session
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // server sends token to inviter
      expect(tokenFactory.nextRandomToken()).andReturn("token1");
      // inviter sends SMS to invitee, who tries to look up the session
      expect(invitee.getDb().getPeerByPhoneNumber(INVITER_CANONICAL_PHONE_NUMBER)).andReturn(inviteePeer);
      // server initiates verification since inviter's phone number is unknown
      expect(tokenFactory.nextRandomToken()).andReturn("token2");
      expect(inviter.getDb().getVerification("token2")).andReturn(null);
      inviter.getDb().putVerification(capture(verificationCapture));
      Verification verificationFromServer = new Verification("token2", 1);
      expect(inviter.getDb().getVerification("token2")).andReturn(verificationFromServer);
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // inviter sends /reportVerified, server sends CONFIRM_SESSION to invitee
      invitee.getUserInteraction().askWhetherToAllowSession(capture(sessionCapture), false);
      // invitee confirms, server sends SESSION_CONFIRMED to inviter
      // inviter shows confirmation and starts publishing locations
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      expect(inviter.getDb().putPeer(capture(peerCapture))).andReturn(inviterPeer);
      inviter.getUserInteraction().showNotification(capture(confirmationCapture));
      inviter.getLocationPublisher().start();
      // invitee starts publishing locations
      invitee.getLocationPublisher().start();

      mocksControl.replay();
      inviter.getPeeringClient().sendInvite(1);
      PeeringSession session = sessionCapture.getValue();
      invitee.getPeeringClient().approveSession(session.getId(), true);
      mocksControl.verify();
      Peer updatedPeer = peerCapture.getValue();
      assertNotNull(updatedPeer.getUserId());
      assertEquals(INVITEE_CANONICAL_PHONE_NUMBER, updatedPeer.getPhoneNumber());

    } finally {
      System.out.println("Summary:");
      for (String action : tracker.getActions()) {
        System.out.println(action);
      }
    }
  }

  @Test
  public void testInvite_bothUnknown() {
    try {
      // inviterPeer is unknown since no phone number is known (maybe not a user yet)
      inviterPeer = new Peer("Ann Oldervoll", INVITEE_CANONICAL_PHONE_NUMBER);  // peer of inviter, i.e. invitee
      inviterPeer.setId(1);
      inviteePeer = new Peer("Thomas Oldervoll", INVITER_CANONICAL_PHONE_NUMBER); // peer of invitee, i.e. inviter
      inviteePeer.setId(2);

      Capture<PeeringSession> sessionCapture = new Capture<PeeringSession>();
      Capture<String> confirmationCapture = new Capture<String>();
      Capture<Peer> peerCapture = new Capture<Peer>();
      Capture<Verification> verificationCapture = new Capture<Verification>();
      Capture<Verification> verificationCapture2 = new Capture<Verification>();

      // inviter creates session
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // server sends token to inviter
      expect(tokenFactory.nextRandomToken()).andReturn("token1");
      // inviter sends SMS to invitee, who tries to look up the session
      expect(invitee.getDb().getPeerByPhoneNumber(INVITER_CANONICAL_PHONE_NUMBER)).andReturn(inviteePeer);
      // server initiates verification since inviter's phone number is unknown
      expect(tokenFactory.nextRandomToken()).andReturn("token2");
      expect(inviter.getDb().getVerification("token2")).andReturn(null);
      inviter.getDb().putVerification(capture(verificationCapture));
      Verification verificationFromServer = new Verification("token2", 1);
      expect(inviter.getDb().getVerification("token2")).andReturn(verificationFromServer);
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      // inviter sends /reportVerified, server initiates verification of invitee
      expect(tokenFactory.nextRandomToken()).andReturn("token3");
      expect(invitee.getDb().getVerification("token3")).andReturn(null);
      invitee.getDb().putVerification(capture(verificationCapture2));
      Verification verificationFromServer2 = new Verification("token3", 2);
      expect(invitee.getDb().getVerification("token3")).andReturn(verificationFromServer);
      expect(invitee.getDb().getPeer(1)).andReturn(inviteePeer);
      // invitee sends /reportVerified, server sends CONFIRM_SESSION to invitee
      invitee.getUserInteraction().askWhetherToAllowSession(capture(sessionCapture), false);
      // inviter shows confirmation and starts publishing locations
      expect(inviter.getDb().getPeer(1)).andReturn(inviterPeer);
      expect(inviter.getDb().putPeer(capture(peerCapture))).andReturn(inviterPeer);
      inviter.getUserInteraction().showNotification(capture(confirmationCapture));
      inviter.getLocationPublisher().start();
      // invitee starts publishing locations
      invitee.getLocationPublisher().start();

      mocksControl.replay();
      inviter.getPeeringClient().sendInvite(1);
      PeeringSession session = sessionCapture.getValue();
      invitee.getPeeringClient().approveSession(session.getId(), true);
      mocksControl.verify();
      Peer updatedPeer = peerCapture.getValue();
      assertNotNull(updatedPeer.getUserId());
      assertEquals(INVITEE_CANONICAL_PHONE_NUMBER, updatedPeer.getPhoneNumber());

    } finally {
      System.out.println("Summary:");
      for (String action : tracker.getActions()) {
        System.out.println(action);
      }
    }
  }

}
