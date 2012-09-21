package no.koredu.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PeeringService extends IntentService {

  public static final String ACTION_INVITE = "ACTION_INVITE";
  public static final String ACTION_REQUEST_SESSION = "ACTION_REQUEST_SESSION";

  private static final String SERVICE_NAME = "PeeringService";
  private static final String TAG = PeeringService.class.getName();

  private PeeringClient peeringClient;

  public PeeringService() {
    super(SERVICE_NAME);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ObjectRegistry reg = ObjectRegistry.get(this);
    peeringClient = reg.getPeeringClient();
  }

  public static void sendInvite(Context context, int peerId) {
    context.startService(new Intent(context, PeeringService.class)
        .setAction(ACTION_INVITE)
        .putExtra(MainActivity.EXTRA_PEER_ID, peerId));
  }

  public static void requestSession(Context context, String inviteToken, String inviterPhoneNumber) {
    context.startService(new Intent(context, PeeringService.class)
        .setAction(ACTION_REQUEST_SESSION)
        .putExtra(MainActivity.EXTRA_INVITE_TOKEN, inviteToken)
        .putExtra(MainActivity.EXTRA_PHONE_NUMBER, inviterPhoneNumber));
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String action = intent.getAction();
    Log.d(TAG, SERVICE_NAME + " handling action " + action);
    if (ACTION_INVITE.equals(action)) {
      peeringClient.sendInvite(intent.getIntExtra(MainActivity.EXTRA_PEER_ID, -1));
    } else if (ACTION_REQUEST_SESSION.equals(action)) {
      peeringClient.requestSession(intent.getStringExtra(MainActivity.EXTRA_INVITE_TOKEN));
    }
  }


}
