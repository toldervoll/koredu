package no.koredu.testing;

import com.squareup.otto.Bus;
import no.koredu.android.*;
import no.koredu.android.database.DatabaseManager;
import no.koredu.server.KoreduApi;
import org.easymock.IMocksControl;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class MockKoreduClient {

  private final DatabaseManager db;
  private final DeviceIdProvider deviceIdProvider;
  private final DisplayNameResolver displayNameResolver;
  private final DirectObjectSender objectSender;
  private final LocationPublisher locationPublisher;
  private final UserInteraction userInteraction;
  private final PhoneNumberVerifier phoneNumberVerifier;
  private final Bus bus;
  private final PeeringClient peeringClient;
  private final SmsProcessor smsProcessor;
  private final DirectSmsSender smsSender;

  public MockKoreduClient(String deviceId, String phoneNumber, IMocksControl mocksControl, ActionTracker tracker) {
    db = mocksControl.createMock(DatabaseManager.class);
    deviceIdProvider = new FakeDeviceIdProvider(deviceId);
    displayNameResolver = mocksControl.createMock(DisplayNameResolver.class);
    objectSender = new DirectObjectSender(deviceId, tracker);
    locationPublisher = mocksControl.createMock(LocationPublisher.class);
    userInteraction = mocksControl.createMock(UserInteraction.class);
    phoneNumberVerifier = new PhoneNumberVerifier(db, deviceIdProvider, objectSender);
    bus = new Bus();
    peeringClient = new PeeringClient(db, deviceIdProvider, displayNameResolver, objectSender, locationPublisher,
        userInteraction, phoneNumberVerifier, bus);
    smsProcessor = new SmsProcessor(phoneNumberVerifier, peeringClient);
    smsSender = new DirectSmsSender(phoneNumber, tracker);
  }

  public void setKoreduApi(KoreduApi koreduApi) {
    objectSender.setKoreduApi(koreduApi);
  }

  public DatabaseManager getDb() {
    return db;
  }

  public DeviceIdProvider getDeviceIdProvider() {
    return deviceIdProvider;
  }

  public DisplayNameResolver getDisplayNameResolver() {
    return displayNameResolver;
  }

  public DirectObjectSender getObjectSender() {
    return objectSender;
  }

  public LocationPublisher getLocationPublisher() {
    return locationPublisher;
  }

  public UserInteraction getUserInteraction() {
    return userInteraction;
  }

  public PhoneNumberVerifier getPhoneNumberVerifier() {
    return phoneNumberVerifier;
  }

  public Bus getBus() {
    return bus;
  }

  public PeeringClient getPeeringClient() {
    return peeringClient;
  }

  public SmsProcessor getSmsProcessor() {
    return smsProcessor;
  }

  public DirectSmsSender getSmsSender() {
    return smsSender;
  }
}
