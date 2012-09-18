package no.koredu.android;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import no.koredu.common.UserLocation;

public class LocationPublishingService extends IntentService {

  public static final String ACTION_SEND_LAST_KNOWN = "ACTION_SEND_LAST_KNOWN";
  public static final String ACTION_SEND_LOCATION = "ACTION_SEND_LOCATION";

  private static final String TAG = LocationPublishingService.class.getName();
  private static final String SERVICE_NAME = "LocationPublishingService";
  private static final long MIN_UPDATE_TIME_MS = 10000L; // TODO: increase to 60 sec
  private static final float MIN_DISTANCE_METERS = 50.0f;
  private static final long LAST_KNOWN_LOCATION_AGE_LIMIT = 5 * 60 * 1000;

  private ObjectSender objectSender;
  private DeviceIdProvider deviceIdProvider;
  private LocationManager locationManager;
  private PendingIntent sendLocationIntent;

  public LocationPublishingService() {
    super(SERVICE_NAME);
  }

  @Override
  public void onCreate() {
    Log.d(TAG, "creating " + SERVICE_NAME);    
    super.onCreate();
    ObjectRegistry reg = ObjectRegistry.get(this);
    objectSender = reg.getObjectSender();
    deviceIdProvider = reg.getDeviceIdProvider();
    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    sendLocationIntent = createPendingIntent();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String action = intent.getAction();
    Log.d(TAG, SERVICE_NAME + " handling action " + action);
    if (Intent.ACTION_RUN.equals(action)) {
      doRun();
    } else if (ACTION_SEND_LAST_KNOWN.equals(action)) {
      doSendLastKnown();
    } else if (ACTION_SEND_LOCATION.equals(action)) {
      doSendLocation(intent);
    } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
      doShutdown();
    }
  }

  private void doRun() {
    for (String provider : locationManager.getAllProviders()) {
      locationManager.requestLocationUpdates(
        provider, MIN_UPDATE_TIME_MS, MIN_DISTANCE_METERS, sendLocationIntent);
    }
  }

  private void doSendLastKnown() {
    Location lastKnownLocation = getLastKnownLocation();
    if (lastKnownLocation != null) {
      sendLocation(lastKnownLocation);
    }
  }

  private void doSendLocation(Intent intent) {
    Location location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
    sendLocation(location);
  }

  private Location getLastKnownLocation() {
    long now = System.currentTimeMillis();
    Location bestLocation = null;
    for (String provider : locationManager.getProviders(true)) {
      Location candidate = locationManager.getLastKnownLocation(provider);
      if ((candidate != null) && ((now - candidate.getTime()) < LAST_KNOWN_LOCATION_AGE_LIMIT)) {
        if (isBetter(candidate, bestLocation)) {
          bestLocation = candidate;
        }
      }
    }
    return bestLocation;
  }

  private boolean isBetter(Location newLocation, Location oldLocation) {
    // TODO: return true if newer or refinement
    return true;
  }

//  private void requestSingleLocation(String phoneNumber) {
//    Intent intent = new Intent(this, LocationPublishingService.class);
//    intent
//        .setAction(ACTION_SEND_LOCATION_SMS)
//        .putExtra(MainActivity.EXTRA_PHONE_NUMBER, phoneNumber);
//    PendingIntent pendingIntent =  PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);   
    //locationManager.requestSingleUpdate(getLocationProvider(), pendingIntent);
//    Location lastKnownLocation = locationManager.getLastKnownLocation(getLocationProvider());
 // }

  private PendingIntent createPendingIntent() {
    Intent intent = new Intent(this, LocationPublishingService.class);
    intent.setAction(ACTION_SEND_LOCATION);
    return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private void doShutdown() {
    locationManager.removeUpdates(sendLocationIntent);
    stopSelf();
  }

  private void sendLocation(Location location) {
    Log.d(TAG, "Sending location " + location);
    UserLocation userLocation = createUserLocation(location);
    objectSender.send("/publishLocation", userLocation);
  }

  private UserLocation createUserLocation(Location location) {
    return new UserLocation(
        deviceIdProvider.get(),
        location.getProvider(),
        location.getTime(),
        location.getLatitude(),
        location.getLongitude(),
        location.hasAccuracy() ? location.getAccuracy() : null,
        location.hasAltitude() ? location.getAltitude() : null,
        location.hasBearing() ? location.getBearing() : null,
        location.hasSpeed() ? location.getSpeed() : null);
  }

}
