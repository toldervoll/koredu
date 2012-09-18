package no.koredu.android;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class ItemizedWithTitlesOverlay extends ItemizedOverlay<OverlayItem> {

  public ItemizedWithTitlesOverlay(Drawable defaultMarker) {
    /*Calling boundCenterBottom() on defaultMarker makes the marker image connect
at its Bottom center to the latitude and longitude of this Overlay Item*/
    super(boundCenterBottom(defaultMarker));

    markerHeight = ((BitmapDrawable) defaultMarker).getBitmap().getHeight();

    /* This call to populate is important. Although this does not appear in the MapView tutorial
    * on Google's Android developer site, the mapview some times crashes without this call.
    */
    populate();
  }

  @Override
  protected OverlayItem createItem(int i) {
    return mOverlayItems.get(i);
  }

  @Override
  public int size() {
    return mOverlayItems.size();
  }

  @Override
  public void draw(android.graphics.Canvas canvas, MapView mapView,
                   boolean shadow) {
    super.draw(canvas, mapView, shadow);

    // go through all OverlayItems and draw title for each of them
    for (OverlayItem item : mOverlayItems) {
      /* Converts latitude & longitude of this overlay item to coordinates on screen.
      * As we have called boundCenterBottom() in constructor, so these coordinates
      * will be of the bottom center position of the displayed marker.
      */
      GeoPoint point = item.getPoint();
      Point markerBottomCenterCoords = new Point();
      mapView.getProjection().toPixels(point, markerBottomCenterCoords);

      /* Find the width and height of the title*/
      TextPaint paintText = new TextPaint();
      Paint paintRect = new Paint();

      Rect rect = new Rect();
      paintText.setTextSize(FONT_SIZE);
      paintText.getTextBounds(item.getTitle(), 0, item.getTitle().length(), rect);
      rect.inset(-TITLE_MARGIN, -TITLE_MARGIN);
      rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2, markerBottomCenterCoords.y - markerHeight/2 - rect.height());

      paintText.setTextAlign(Paint.Align.CENTER);
      paintText.setTextSize(FONT_SIZE);
      paintRect.setARGB(130, 255, 255, 255);

      canvas.drawRoundRect(new RectF(rect), 2, 2, paintRect);

      paintText.setARGB(255, 0, 0, 0);
      canvas.drawText(item.getTitle(), rect.left + rect.width() / 2,
          rect.bottom - TITLE_MARGIN, paintText);

    }
  }

  public void addOverlayItem(int latitude, int longitude, String title, String snippet) {
    OverlayItem item;
    GeoPoint geopoint = new GeoPoint(latitude, longitude);
    item = new OverlayItem(geopoint, title, snippet);
    mOverlayItems.add(item);

  }

  public void addOverlayItem(OverlayItem overlayItem) {
    mOverlayItems.add(overlayItem);
  }
  
  public void clearOverlayItems() {
    mOverlayItems.clear();
  }

  private int markerHeight;
  private ArrayList<OverlayItem> mOverlayItems = new ArrayList<OverlayItem>();

  private static final int FONT_SIZE = 36;
  private static final int TITLE_MARGIN = 3;

}
