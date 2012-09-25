package no.koredu.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import no.koredu.android.auth.AccountList;
import no.koredu.android.database.DatabaseManager;

import java.util.List;

public class MainActivity extends SherlockMapActivity {

  private static final String TAG = MainActivity.class.getName();

  public static final String ACTION_INVITE_PEER = "ACTION_INVITE_PEER";
  public static final String ACTION_APPROVE_PEER = "ACTION_APPROVE_PEER";
  public static final String ACTION_SHOW_PEER = "ACTION_SHOW_PEER";

  public static final String EXTRA_INVITE_TOKEN = "EXTRA_INVITE_TOKEN";
  public static final String EXTRA_DISPLAY_NAME = "EXTRA_DISPLAY_NAME";
  public static final String EXTRA_PHONE_NUMBER = "EXTRA_PHONE_NUMBER";
  public static final String EXTRA_LATITUDE_E6 = "EXTRA_LATITUDE_E6";
  public static final String EXTRA_LONGITUDE_E6 = "EXTRA_LONGITUDE_E6";
  public static final String EXTRA_PEER_ID = "EXTRA_PEER_ID";
  public static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";

  private static final int MENU_ITEM_ADD_PEER = Menu.FIRST;
  private static final int MENU_ITEM_CLEAR_MAP = Menu.FIRST + 1;
  private static final int MENU_ITEM_ADD_DUMMY_DATA = Menu.FIRST + 2;
  private static final int MENU_ITEM_STOP_LOCATION_PUBLISHING = Menu.FIRST + 3;
  private static final int MENU_ITEM_SWITCH_ACCOUNT = Menu.FIRST + 4;

  private static final int DIALOG_APPROVE_PEER = 0;

  private MapView mapView;
  private CharSequence approveText;
  private CharSequence denyText;
  private PeerLocationOverlay mPeerLocationOverlay;
  private DatabaseManager db;
  private Bus bus;
  private PeeringClient peeringClient;
  private LocationPublisher locationPublisher;
  private UserInteraction userInteraction;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ObjectRegistry reg = ObjectRegistry.get(this);
    db = reg.getDatabaseManager();
    bus = reg.getBus();
    peeringClient = reg.getPeeringClient();
    locationPublisher = reg.getLocationPublisher();
    userInteraction = reg.getUserInteraction();
    setContentView(R.layout.main);
    mapView = (MapView) findViewById(R.id.mapview);
    mapView.setBuiltInZoomControls(true);
    List<Overlay> mapOverlays = mapView.getOverlays();
    final MyLocationOverlay myLocationOverlay = new CenteringMyLocationOverlay(this, mapView);
    myLocationOverlay.enableMyLocation();
    mapOverlays.add(myLocationOverlay);
    Drawable locationMarker = this.getResources().getDrawable(R.drawable.placemarker_red);
    mPeerLocationOverlay = new PeerLocationOverlay(locationMarker);
    mapOverlays.add(mPeerLocationOverlay);
    mapView.getController().setZoom(14);

    approveText = getResources().getText(R.string.peer_approval_dialog_approve);
    denyText = getResources().getText(R.string.peer_approval_dialog_deny);

    Intent intent = getIntent();
    String action = intent.getAction();
    Bundle extras = intent.getExtras();
    if (ACTION_INVITE_PEER.equals(action)) {
      PeeringService.sendInvite(this, extras.getInt(EXTRA_PEER_ID));
    } else if (ACTION_APPROVE_PEER.equals(action)) {
      showDialog(DIALOG_APPROVE_PEER, extras);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    bus.register(this);
    userInteraction.setActivityVisible(true);
    mapView.invalidate();
  }

  @Override
  protected void onPause() {
    bus.unregister(this);
    userInteraction.setActivityVisible(false);
    super.onPause();
  }

  @Subscribe
  public void locationUpdated(LocationUpdateEvent event) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mapView.invalidate();
      }
    });
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MENU_ITEM_ADD_PEER, Menu.CATEGORY_SYSTEM, "Ask where someone is")
        .setIcon(R.drawable.add_peer)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    menu.add(Menu.NONE, MENU_ITEM_ADD_DUMMY_DATA, Menu.CATEGORY_SYSTEM, "Add dummy data")
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    menu.add(Menu.NONE, MENU_ITEM_CLEAR_MAP, Menu.CATEGORY_SYSTEM, "Clear map")
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    menu.add(Menu.NONE, MENU_ITEM_STOP_LOCATION_PUBLISHING, Menu.CATEGORY_SYSTEM, "Stop location publishing")
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    menu.add(Menu.NONE, MENU_ITEM_SWITCH_ACCOUNT, Menu.CATEGORY_SYSTEM, "Switch account")
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ITEM_ADD_PEER:
        pickPeer();
        return true;
      case MENU_ITEM_ADD_DUMMY_DATA:
        addDummyData();
        return true;
      case MENU_ITEM_CLEAR_MAP:
        clearMap();
        return true;
      case MENU_ITEM_STOP_LOCATION_PUBLISHING:
        locationPublisher.stop();
      case MENU_ITEM_SWITCH_ACCOUNT:
        switchAccount();
      default:
        return false;
    }
  }

  private void clearMap() {
    db.deleteAllPeers();
    mapView.invalidate();
  }

  public void pickPeer() {
    startActivity(new Intent(this, PickPeerActivity.class));
  }

  public void addDummyData() {
    db.putPeer(new Peer("Thomas Oldervoll", "+4748193450", 0, 0, 0.0f, System.currentTimeMillis(), Long.MAX_VALUE));
    db.putPeer(new Peer("Android 2.2", "+5556", 0, 0, 0.0f, System.currentTimeMillis(), Long.MAX_VALUE));
    db.putPeer(new Peer("Android 4.0", "+5554", 0, 0, 0.0f, System.currentTimeMillis(), Long.MAX_VALUE));
  }

  private void switchAccount() {
    startActivity(new Intent(this, AccountList.class));
  }

  @Override
  public Dialog onCreateDialog(int id, Bundle args) {
    if (id == DIALOG_APPROVE_PEER) {
      AlertDialog alertDialog = new AlertDialog.Builder(this).create();
      alertDialog.setTitle("Someone Unknown wants to know where you are");
      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, approveText, (DialogInterface.OnClickListener) null);
      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, denyText, (DialogInterface.OnClickListener) null);
      return alertDialog;
    } else {
      return null;
    }
  }

  @Override
  public void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    if (id == DIALOG_APPROVE_PEER) {
      final String displayName = args.getString(EXTRA_DISPLAY_NAME);
      final long sessionId = args.getLong(EXTRA_SESSION_ID);
      final String title = displayName + " wants to know where your are";
      final AlertDialog alertDialog = (AlertDialog) dialog;
      alertDialog.setTitle(title);
      alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, approveText,
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              Log.d(TAG, "Session " + sessionId + " approved");
              peeringClient.approveSession(sessionId, true);
            }
          });
      alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, denyText,
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
              Log.d(TAG, "Session " + sessionId + " denied");
              peeringClient.approveSession(sessionId, false);
            }
          });
    }
  }

}
