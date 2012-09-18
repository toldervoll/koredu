package no.koredu.android;

import android.content.Context;
import android.util.Log;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import no.koredu.android.database.AndroidDatabaseManager;
import no.koredu.android.database.DatabaseManager;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class ObjectRegistry {

  private static final String TAG = ObjectRegistry.class.getName();

  private static ObjectRegistry instance;

  private final Context context;
  private final DeviceIdProvider deviceIdProvider;
  private final ObjectSender objectSender;
  private final JsonMapper jsonMapper;
  private final DatabaseManager databaseManager;
  private final Bus bus;
  private final SmsSender smsSender;
  private final DisplayNameResolver displayNameResolver;
  private final UserInteraction userInteraction;
  private final PhoneNumberVerifier phoneNumberVerifier;
  private final LocationPublisher locationPublisher;
  private final PeeringClient peeringClient;
  private final SmsProcessor smsProcessor;

  public static synchronized ObjectRegistry get(Context context) {
    if (instance == null) {
      instance = new ObjectRegistry(context.getApplicationContext());
    }
    return instance;
  }

  public static synchronized ObjectRegistry get() {
    if (instance == null) {
      throw new IllegalStateException("ObjectRegistry context not set");
    }
    return instance;
  }

  public ObjectRegistry(Context applicationContext) {
    long startTime = System.currentTimeMillis();
    this.context = applicationContext;
    jsonMapper = new JsonMapper();
    objectSender = new HttpObjectSender(context, jsonMapper);
    deviceIdProvider = new GCMDeviceIdProvider(applicationContext, objectSender);
    databaseManager = new AndroidDatabaseManager(context);
    bus = new Bus(ThreadEnforcer.ANY);
    smsSender = new AndroidSmsSender();
    displayNameResolver = new DisplayNameResolver(context);
    userInteraction = new UserInteraction(context, displayNameResolver);
    phoneNumberVerifier = new PhoneNumberVerifier(databaseManager, deviceIdProvider, objectSender);
    locationPublisher = new AndroidLocationPublisher(context);
    peeringClient = new PeeringClient(databaseManager, deviceIdProvider, displayNameResolver, objectSender,
        locationPublisher, userInteraction, phoneNumberVerifier, bus);
    smsProcessor = new SmsProcessor(phoneNumberVerifier, peeringClient);
    long duration = System.currentTimeMillis() - startTime;
    Log.v(TAG, "initialized in " + duration + " ms");
  }

  public DeviceIdProvider getDeviceIdProvider() {
    return deviceIdProvider;
  }

  public ObjectSender getObjectSender() {
    return objectSender;
  }

  public JsonMapper getJsonMapper() {
    return jsonMapper;
  }

  public DatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  public Bus getBus() {
    return bus;
  }

  public SmsSender getSmsSender() {
    return smsSender;
  }

  public DisplayNameResolver getDisplayNameResolver() {
    return displayNameResolver;
  }

  public UserInteraction getUserInteraction() {
    return userInteraction;
  }

  public PhoneNumberVerifier getPhoneNumberVerifier() {
    return phoneNumberVerifier;
  }

  public LocationPublisher getLocationPublisher() {
    return locationPublisher;
  }

  public PeeringClient getPeeringClient() {
    return peeringClient;
  }

  public SmsProcessor getSmsProcessor() {
    return smsProcessor;
  }
}
