package no.koredu.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.jakewharton.notificationcompat2.NotificationCompat2;
import no.koredu.common.PeeringSession;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class UserInteraction {

  private static final int ALLOW_NOTIFICATION = 0;
  private static final int CONFIRMED_NOTIFICATION = 1;
  private static final int ACTIVE_SESSIONS_NOTIFICATION = 2;

  private final Context context;
  private final DisplayNameResolver displayNameResolver;
  private final NotificationManager notificationManager;
  private boolean activityVisible = false;

  public UserInteraction(Context context, DisplayNameResolver displayNameResolver) {
    this.context = context;
    this.displayNameResolver = displayNameResolver;
    this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public void askWhetherToAllowSession(PeeringSession session) {
    // TODO: displayName from Google Profile?
    String displayName = displayNameResolver.getDisplayName(session.getInviterPhoneNumber());
    Intent approveIntent = createApproveIntent(session, displayName);
    if (activityVisible) {
      context.startActivity(approveIntent);
    } else {
      String text = displayName + " wants to know where you are for 1 hour";
      showNotification(ALLOW_NOTIFICATION, text, 0, approveIntent, false);
    }
  }

  public void showSessionConformation(PeeringSession session, boolean approved) {
    String decision = approved ? "accepted" : "denied";
    String displayName = displayNameResolver.getDisplayName(session.getInviteePhoneNumber());
    String message = displayName + " " + decision + " your request to exchange locations";
    if (activityVisible) {
      Toast.makeText(context, message, Toast.LENGTH_LONG);
    } else {
      showNotification(CONFIRMED_NOTIFICATION, message, 0, new Intent(context, MainActivity.class), false);
    }
  }

  public void showActiveSessionsNotification(int peerCount) {
    if (peerCount == 0) {
      notificationManager.cancel(ACTIVE_SESSIONS_NOTIFICATION);
    } else {
      String message = peerCount + ((peerCount == 1) ? " person" : " people") + " can see where you are";
      showNotification(ACTIVE_SESSIONS_NOTIFICATION, message, peerCount, new Intent(context, MainActivity.class), true);
    }
  }

  public boolean isActivityVisible() {
    return activityVisible;
  }

  public void setActivityVisible(boolean activityVisible) {
    this.activityVisible = activityVisible;
  }

  private Intent createApproveIntent(PeeringSession session, String displayName) {
    return new Intent(context, MainActivity.class)
        .setAction(MainActivity.ACTION_APPROVE_PEER)
        .putExtra(MainActivity.EXTRA_SESSION_ID, session.getId())
        .putExtra(MainActivity.EXTRA_DISPLAY_NAME, displayName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  private void showNotification(int id, String message, int numberOfItems, Intent intent, boolean ongoing) {
    int icon = R.drawable.ic_launcher;
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
    NotificationCompat2.Builder builder = new NotificationCompat2.Builder(context)
        .setContentTitle("Koredu")
        .setContentText(message)
        .setTicker(message)
        .setContentIntent(pendingIntent)
        .setSmallIcon(icon);
    if (ongoing) {
      builder.setOngoing(true);
    } else {
      builder.setAutoCancel(true);
    }
    if (numberOfItems > 0) {
      builder.setNumber(numberOfItems);
    }
    notificationManager.notify(id, builder.build());
  }

}
