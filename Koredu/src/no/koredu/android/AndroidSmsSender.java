package no.koredu.android;

import android.telephony.SmsManager;
import android.util.Log;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class AndroidSmsSender implements SmsSender {

  private final static String TAG = AndroidSmsSender.class.getName();

  @Override
  public void send(String phoneNumber, String message) {
    Log.d(TAG, "Sending sms to " + phoneNumber + ": " + message);
    SmsManager sms = SmsManager.getDefault();
    sms.sendTextMessage(phoneNumber, null, message, null, null);
  }

}
