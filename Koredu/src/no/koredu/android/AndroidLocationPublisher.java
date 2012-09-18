package no.koredu.android;

import android.content.Context;
import android.content.Intent;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class AndroidLocationPublisher implements LocationPublisher {

  private final Context context;
  private boolean publishing = false;
  private Intent sendLastKnownIntent;
  private Intent runIntent;
  private Intent shutdownIntent;

  public AndroidLocationPublisher(Context context) {
    this.context = context;
    sendLastKnownIntent = new Intent(context, LocationPublishingService.class);
    sendLastKnownIntent.setAction(LocationPublishingService.ACTION_SEND_LAST_KNOWN);
    runIntent = new Intent(context, LocationPublishingService.class);
    runIntent.setAction(Intent.ACTION_RUN);
    shutdownIntent = new Intent(context, LocationPublishingService.class);
    shutdownIntent.setAction(Intent.ACTION_SHUTDOWN);
  }

  @Override
  public synchronized void start() {
    context.startService(sendLastKnownIntent);
    if (!publishing) {
      publishing = true;
      context.startService(runIntent);
    }
  }

  @Override
  public synchronized void stop() {
    publishing = false;
    context.startService(shutdownIntent);
  }

}
