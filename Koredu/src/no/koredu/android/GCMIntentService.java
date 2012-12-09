package no.koredu.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import no.koredu.common.PeeringSession;
import no.koredu.common.UserLocation;

public class GCMIntentService extends GCMBaseIntentService {

  private final static String TAG = GCMIntentService.class.getName();
  public static final String SENDER_ID = "430522666716";

  private JsonMapper jsonMapper;
  private SmsSender smsSender;
  private PeeringClient peeringClient;

  public GCMIntentService() {
    super(SENDER_ID);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ObjectRegistry reg = ObjectRegistry.get(this);
    jsonMapper = reg.getJsonMapper();
    smsSender = reg.getSmsSender();
    peeringClient = reg.getPeeringClient();
  }

  @Override
  protected void onMessage(Context context, Intent intent) {
    Log.d(TAG, "onMessage called, intent=" + intent + ", extras=" + intent.getExtras());
    String action = intent.getStringExtra("action");
    String data = intent.getStringExtra("data");
    if ("SEND_SMS".equals(action)) {
      smsSender.send(intent.getStringExtra("phoneNumber"), intent.getStringExtra("message"));
    } else if ("CONFIRM_SESSION".equals(action)) {
      peeringClient.confirmSession(jsonMapper.fromJson(data, PeeringSession.class));
    } else if ("SESSION_CONFIRMED".equals(action)) {
      handleSessionConfirmation(data, true);
    } else if ("SESSION_DENIED".equals(action)) {
      handleSessionConfirmation(data, false);
    } else if ("LOCATION_UPDATE".equals(action)) {
      peeringClient.receivePeerLocation(jsonMapper.fromJson(data, UserLocation.class));
    }
  }

  private void handleSessionConfirmation(String data, boolean approved) {
    PeeringSession session = jsonMapper.fromJson(data, PeeringSession.class);
    peeringClient.handleSessionConfirmation(session, approved);
  }

  @Override
  protected void onError(Context context, String errorId) {
    Log.d(TAG, "onError called, errorId=" + errorId);
  }

  @Override
  protected boolean onRecoverableError(Context context, String errorId) {
    Log.d(TAG, "onRecoverableError called, errorId=" + errorId);
    return true;
  }

  @Override
  protected void onDeletedMessages(Context context, int total) {
    Log.d(TAG, "onDeletedMessages called, total=" + total);
  }

  @Override
  protected void onRegistered(Context context, String registrationId) {
    Log.d(TAG, "onRegistered called, registrationId=" + registrationId);
  }

  @Override
  protected void onUnregistered(Context context, String registrationId) {
    Log.d(TAG, "onUnregistered called, registrationId=" + registrationId);
  }


}
