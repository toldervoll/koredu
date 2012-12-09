package no.koredu.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import com.google.common.util.concurrent.SettableFuture;

import java.util.concurrent.ExecutionException;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class GCMDeviceIdProvider implements DeviceIdProvider {

  private static final String TAG = GCMDeviceIdProvider.class.getName();

  private final Context context;
  private final ObjectSender objectSender;
  private SettableFuture<String> deviceId = SettableFuture.create();

  public GCMDeviceIdProvider(Context context, ObjectSender objectSender) {
    this.context = context;
    this.objectSender = objectSender;
    new WaitForDeviceIdTask().execute();
  }

  @Override
  public String get() {
    try {
      return deviceId.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private class WaitForDeviceIdTask extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
      GCMRegistrar.checkDevice(context);
      GCMRegistrar.checkManifest(context);
      String regId = GCMRegistrar.getRegistrationId(context);
      if (regId.equals("")) {
        Log.v(TAG, "Registering with GCM");
        GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
      } else {
        Log.v(TAG, "Already registered with GCM");
      }
      while ("".equals(regId)) {
        try {
          Log.v(TAG, "Waiting for device id...");
          Thread.sleep(1000);
          regId = GCMRegistrar.getRegistrationId(context);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
      deviceId.set(regId);
      return null;
    }
  }

}
