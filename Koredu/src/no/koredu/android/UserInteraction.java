package no.koredu.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.jakewharton.notificationcompat2.NotificationCompat2;
import no.koredu.common.PeeringSession;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class UserInteraction {

  private final Context context;
  private final DisplayNameResolver displayNameResolver;


  public UserInteraction(Context context, DisplayNameResolver displayNameResolver) {
    this.context = context;
    this.displayNameResolver = displayNameResolver;
  }

  public void askWhetherToAllowSession(PeeringSession session) {
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
    int icon = R.drawable.ic_launcher;

    // TODO: displayName from Google Profile?
    String displayName = displayNameResolver.getDisplayName(session.getInviterPhoneNumber());

    Intent notificationIntent = new Intent(context, MainActivity.class);
    notificationIntent
        .putExtra(MainActivity.EXTRA_SESSION_ID, session.getId())
        .putExtra(MainActivity.EXTRA_DISPLAY_NAME, displayName);
    notificationIntent.setAction(MainActivity.ACTION_APPROVE_PEER);
    String text = displayName + " wants to know where you are for 1 hour";
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

    NotificationCompat2.Builder builder = new NotificationCompat2.Builder(context);
    Notification notification = builder
        .setContentTitle("Koredu")
        .setContentText(text)
        .setTicker(text)
        .setContentIntent(contentIntent)
        .setSmallIcon(icon)
        .setAutoCancel(true)
        .build();
    mNotificationManager.notify(0, notification);
  }

  public void showSessionConformation(PeeringSession session, boolean approved) {
    String decision = approved ? "accepted" : "denied";
    String displayName = displayNameResolver.getDisplayName(session.getInviterPhoneNumber());
    String message = displayName + " " + decision + " your request to exchange locations";
    NotificationManager mNotificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    int icon = R.drawable.ic_launcher;
    PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
    NotificationCompat2.Builder builder = new NotificationCompat2.Builder(context);
    Notification notification = builder
        .setContentTitle("Koredu")
        .setContentText(message)
        .setTicker(message)
        .setContentIntent(intent)
        .setSmallIcon(icon)
        .setAutoCancel(true)
        .build();
    mNotificationManager.notify(0, notification);
  }

}
