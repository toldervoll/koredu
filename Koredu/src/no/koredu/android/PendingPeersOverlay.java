package no.koredu.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import javax.persistence.TableGenerator;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PendingPeersOverlay extends Overlay {

  private static final String TAG = PendingPeersOverlay.class.getName();

  @Override
  public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    super.draw(canvas, mapView, shadow);

    /* Find the width and height of the title*/
    TextPaint paintText = new TextPaint();
    Paint paintRect = new Paint();

    Rect rect = new Rect();
    paintText.setTextSize(ItemizedWithTitlesOverlay.FONT_SIZE);
    String text = "PendingPeersOverlay";
    paintText.getTextBounds(text, 0, text.length(), rect);
    rect.inset(-ItemizedWithTitlesOverlay.TITLE_MARGIN, -ItemizedWithTitlesOverlay.TITLE_MARGIN);
    rect.offsetTo(0, canvas.getHeight() - rect.height());

    paintText.setTextAlign(Paint.Align.CENTER);
    paintText.setTextSize(ItemizedWithTitlesOverlay.FONT_SIZE);
    paintRect.setARGB(130, 255, 255, 255);

    canvas.drawRoundRect(new RectF(rect), 2, 2, paintRect);

    paintText.setARGB(255, 0, 0, 0);
    int x = rect.left + rect.width() / 2;
    int y = rect.bottom - ItemizedWithTitlesOverlay.TITLE_MARGIN;
    Log.d(TAG, text + " at (" + x + ", " + y + ")");
    canvas.drawText(text, x, y, paintText);


//    Paint paint = new Paint();
//    paint.setColor(android.R.color.black);
//    paint.setTextSize(20);
//    canvas.drawText("Some Text", 10, 25, paint);

  }
}
