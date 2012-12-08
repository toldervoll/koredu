package no.koredu.android;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import no.koredu.android.database.DatabaseManager;
import android.graphics.drawable.Drawable;

import com.google.android.maps.OverlayItem;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PeerLocationOverlay extends ItemizedWithTitlesOverlay {

  private static final GeoPoint NULL_POINT = new GeoPoint(0, 0);
  private final MapView mapView;
  private final DatabaseManager db;

  public PeerLocationOverlay(Drawable defaultMarker, MapView mapView) {
    super(boundCenterBottom(defaultMarker));
    this.mapView = mapView;
    db = ObjectRegistry.get().getDatabaseManager();
    refresh();
  }

  public int refresh() {
    clearOverlayItems();
    GeoPoint center = mapView.getMapCenter();
    int latSpan = mapView.getLatitudeSpan();
    int longSpan = mapView.getLongitudeSpan();
    int currentMinLat = center.getLatitudeE6() - (latSpan / 2);
    int currentMaxLat = currentMinLat + latSpan;
    int currentMinLong = center.getLongitudeE6() - (longSpan / 2);
    int currentMaxLong = currentMinLong + longSpan;
    int minLat = Integer.MAX_VALUE;
    int minLong = Integer.MAX_VALUE;
    int maxLat = Integer.MIN_VALUE;
    int maxLong = Integer.MIN_VALUE;
    List<Peer> peerLocations = db.getPeers();
    for (Peer location : peerLocations) {
      GeoPoint geoPoint = location.getGeoPoint();
      if (!geoPoint.equals(NULL_POINT)) {
        minLat = Math.min(geoPoint.getLatitudeE6(), minLat);
        minLong = Math.min(geoPoint.getLongitudeE6(), minLong);
        maxLat  = Math.max(geoPoint.getLatitudeE6(), maxLat);
        maxLong = Math.max(geoPoint.getLongitudeE6(), maxLong);
      }
      addOverlayItem(new OverlayItem(geoPoint, location.getDisplayName(), location.getDisplayName()));
    }
//    addOverlayItem(new OverlayItem(NULL_POINT, "Per Pending", "Peer Peending"));

    if ((minLat < currentMinLat) || (maxLat > currentMaxLat)
        || (minLong < currentMinLong) || (maxLong > currentMaxLong)) {
      // must zoom out to fit all locations
      int newMinLat = Math.min(minLat, currentMinLat);
      int newMaxLat = Math.max(maxLat, currentMaxLat);
      int newMinLong = Math.min(minLong, currentMinLong);
      int newMaxLong = Math.max(maxLong, currentMaxLong);
      mapView.getController().zoomToSpan(newMaxLat - newMinLat, newMaxLong - newMinLong);
    }
    populate();
    return peerLocations.size();
  }  
  
}
