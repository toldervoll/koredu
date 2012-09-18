package no.koredu.android;

import java.util.List;

import no.koredu.android.database.DatabaseManager;
import android.graphics.drawable.Drawable;

import com.google.android.maps.OverlayItem;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PeerLocationOverlay extends ItemizedWithTitlesOverlay {

  private final DatabaseManager db;

  public PeerLocationOverlay(Drawable defaultMarker) {
    super(boundCenterBottom(defaultMarker));
    db = ObjectRegistry.get().getDatabaseManager();
    refresh();
  }

  public int refresh() {
    clearOverlayItems();
    List<Peer> peerLocations = db.getPeers();
    for (Peer location : peerLocations) {
      addOverlayItem(new OverlayItem(location.getGeoPoint(), location.getDisplayName(), location.getDisplayName()));
    }
    populate();
    return peerLocations.size();
  }  
  
}
