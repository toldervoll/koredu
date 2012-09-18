package no.koredu.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    SmsProcessor smsProcessor = ObjectRegistry.get(context).getSmsProcessor();
    Bundle bundle = intent.getExtras();
    if (bundle != null) {
      Object[] pdus = (Object[]) bundle.get("pdus");
      for (int i = 0; i < pdus.length; i++) {
        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
        smsProcessor.processMessage(sms.getOriginatingAddress(), sms.getMessageBody());
      }
    }
  }

}
