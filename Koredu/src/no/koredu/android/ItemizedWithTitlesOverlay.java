package no.koredu.android;

import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class ItemizedWithTitlesOverlay extends ItemizedOverlay<OverlayItem> {

  public static final int FONT_SIZE = 28;
  public static final int TITLE_MARGIN = 3;

  private static final String TAG = ItemizedWithTitlesOverlay.class.getName();

  private int markerHeight;
  private ArrayList<OverlayItem> mOverlayItems = new ArrayList<OverlayItem>();

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

    int unknownCounter = 0;
    int unknownListYOffset = canvas.getHeight() - 70;

    // go through all OverlayItems and draw title for each of them
    for (OverlayItem item : mOverlayItems) {

      GeoPoint point = item.getPoint();
      boolean hasLocation = (point.getLatitudeE6() != 0) && (point.getLongitudeE6() != 0);

      Rect rect = new Rect();
      TextPaint textPaint = new TextPaint();
      textPaint.setTextSize(FONT_SIZE);
      String text = item.getTitle() + (hasLocation ? "" : "?");
      textPaint.getTextBounds(text, 0, text.length(), rect);
      rect.inset(-TITLE_MARGIN, -TITLE_MARGIN);

      /* Converts latitude & longitude of this overlay item to coordinates on screen.
      * As we have called boundCenterBottom() in constructor, so these coordinates
      * will be of the bottom center position of the displayed marker.
      */
      Point markerBottomCenterCoords = new Point();
      if (!hasLocation) {
        markerBottomCenterCoords.set(rect.centerX() + TITLE_MARGIN, unknownListYOffset);
        unknownListYOffset -= (rect.height() + 2*TITLE_MARGIN);
      } else {
        mapView.getProjection().toPixels(point, markerBottomCenterCoords);
      }
      Log.v(TAG, text + " markerBottomCenterCoords is " + markerBottomCenterCoords);

      /* Find the width and height of the title*/
      Paint paintRect = new Paint();

      rect.offsetTo(markerBottomCenterCoords.x - rect.width() / 2,
          markerBottomCenterCoords.y - markerHeight / 2 - rect.height());

      textPaint.setTextAlign(Paint.Align.CENTER);
      textPaint.setTextSize(FONT_SIZE);
      paintRect.setARGB(130, 255, 255, 255);

      canvas.drawRoundRect(new RectF(rect), 2, 2, paintRect);

      textPaint.setARGB(255, 0, 0, 0);
      int x = rect.left + rect.width() / 2;
      int y = rect.bottom - TITLE_MARGIN;
      Log.d(TAG, "Drawing '" + text + "' at (" + x + ", " + y + ")");
      canvas.drawText(text, x, y, textPaint);
//      for (int i = 0; i < 1000; i = i + 100 ) {
//        canvas.drawText("(" + i + ", " + i + ")", i, i, textPaint);
//      }
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

}
