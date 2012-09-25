package no.koredu.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.google.common.base.Charsets;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpService extends IntentService {

  public static final String LOCAL_BASE_URL = "http://10.0.2.2:8888";
  public static final String PROD_BASE_URL = "https://koreduno.appspot.com";
  public static final boolean USE_LOCAL_SERVER = true;
  public static final String EXTRA_PATH = "EXTRA_PATH";
  public static final String EXTRA_DATA = "EXTRA_DATA";

  private static final String SERVICE_NAME = "HttpService";
  private static final String TAG = HttpService.class.getName();

  public HttpService() {
    super(SERVICE_NAME);
    disableConnectionReuseIfNecessary();
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  public static void post(Context context, String path, String data) {
    context.startService(new Intent(context, HttpService.class)
        .setAction(Intent.ACTION_RUN)
        .putExtra(EXTRA_PATH, path)
        .putExtra(EXTRA_DATA, data));
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String path = intent.getStringExtra(EXTRA_PATH);
    String data = intent.getStringExtra(EXTRA_DATA);
    Log.d(TAG, "Posting to " + path + ": " + data);
    sendToServer(path, data);
  }

  private void disableConnectionReuseIfNecessary() {
    // HTTP connection reuse which was buggy pre-froyo
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
      System.setProperty("http.keepAlive", "false");
    }
  }

  public static String sendToServer(String path, String data) {
    // TODO: retry with exponential backoff
    byte[] payloadBytes = data.getBytes(Charsets.UTF_8);
    URL url = null;
    HttpURLConnection urlConnection = null;
    InputStream in = null;
    OutputStream out = null;
    String response = null;
    try {
      url = new URL(getBaseUrl() + path);
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setDoOutput(true);
      urlConnection.setFixedLengthStreamingMode(payloadBytes.length);
      urlConnection.setRequestProperty("Content-Type", "application/json");
      out = new BufferedOutputStream(urlConnection.getOutputStream());
      out.write(payloadBytes);
      out.close();
      in = new BufferedInputStream(urlConnection.getInputStream());
      int responseCode = urlConnection.getResponseCode();
      String responseMessage = urlConnection.getResponseMessage();
      Log.v(TAG, "response code from " + url + ": " + responseCode + " " + responseMessage);
      if ((responseCode / 100) == 2) {
        byte[] responseData = readStream(in);
        response = new String(responseData, Charsets.UTF_8);
        Log.d(TAG, "response from " + url + ": " + response);
      } else {
        // throw and let caller retry
        throw new RuntimeException("Non-ok response from " + url + ": " + responseCode + " " + responseMessage);
      }
    } catch (IOException e) {
      Log.e(TAG, "failed to post to " + url);
      throw new RuntimeException("Failed to send payload to " + url, e);
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          Log.w(TAG, "Failed to close OutputStream", e);
        }
      }
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          Log.w(TAG, "Failed to close InputStream", e);
        }
      }
    }
    return response;
  }

  private static String getBaseUrl() {
    if (USE_LOCAL_SERVER && "google_sdk".equals(Build.PRODUCT)) {
      return LOCAL_BASE_URL;
    } else {
      return PROD_BASE_URL;
    }
  }

  private static byte[] readStream(InputStream in)
      throws IOException {
    byte[] buf = new byte[1024];
    int count = 0;
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    while ((count = in.read(buf)) != -1)
      out.write(buf, 0, count);
    return out.toByteArray();
  }

}
