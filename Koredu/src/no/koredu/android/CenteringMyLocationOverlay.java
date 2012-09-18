package no.koredu.android;

import android.content.Context;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

/**
* @author thomas@zenior.no (Thomas Oldervoll)
*/
public class CenteringMyLocationOverlay extends MyLocationOverlay {

  private final MapView mapView;

  public CenteringMyLocationOverlay(Context context, final MapView mapView) {
    super(context, mapView);
    this.mapView = mapView;
    runOnFirstFix(new Runnable() {
      @Override
      public void run() {
        GeoPoint myLocation = getMyLocation();
        mapView.getController().setCenter(myLocation);
      }
    });
  }

}
