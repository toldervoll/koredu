package no.koredu.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.common.base.Charsets;
import no.koredu.android.HttpService;
import no.koredu.android.ObjectRegistry;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.net.URI;
import java.net.URLEncoder;

public class AuthenticationService extends IntentService {

  private static final String ACTION_GET_COOKIE = "ACTION_GET_COOKIE";
  private static final String EXTRA_AUTH_TOKEN = "EXTRA_AUTH_TOKEN";

  private static final String TAG = AuthenticationService.class.getName();
  private static final String AUTH_COOKIE_NAME = "SACSID";

  private final DefaultHttpClient httpClient = new DefaultHttpClient();
  private AndroidAccountProvider accountProvider;


  public AuthenticationService() {
    super("AuthenticationService");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ObjectRegistry reg = ObjectRegistry.get(this);
    accountProvider = (AndroidAccountProvider) reg.getAccountProvider();
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    String action = intent.getAction();
    Log.v(TAG, "Handling action=" + action + " on thread " + Thread.currentThread().getName());
    if (ACTION_GET_COOKIE.equals(action)) {
      getAuthCookie(intent);
    } else {
      getAuthToken(intent);
    }
  }

  private void getAuthToken(Intent intent) {
    AccountManager accountManager = AccountManager.get(getApplicationContext());
    Account account = (Account) intent.getExtras().get("account");
    accountManager.getAuthToken(account, "ah", null, true, new GetAuthTokenCallback(), null);
  }

  private void getAuthCookie(Intent intent) {
    String authToken = intent.getStringExtra(EXTRA_AUTH_TOKEN);
    String authCookie = getAuthCookie(authToken);
    Log.v(TAG, "Got authCookie=" + authCookie);
    accountProvider.setAuthenticationCookie(authCookie);
  }

  private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
    public void run(AccountManagerFuture<Bundle> result) {
      try {
        Bundle bundle = result.getResult();
        String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
        Log.v(TAG, "Got authToken=" + authToken);
        Intent intent = new Intent(AuthenticationService.this, AuthenticationService.class);
        intent.setAction(ACTION_GET_COOKIE);
        intent.putExtra(EXTRA_AUTH_TOKEN, authToken);
        startService(intent);
      } catch (Exception e) {
        throw new RuntimeException("Authentication failed", e);
      }
    }
  }

  /**
   * Retrieves the authorization cookie associated with the given token. This
   * method should only be used when running against a production appengine
   * backend (as opposed to a dev mode server).
   */
  private String getAuthCookie(String authToken) {
    try {
      String continueURL = URLEncoder.encode(HttpService.PROD_BASE_URL, Charsets.UTF_8.name());
      URI uri = new URI(HttpService.PROD_BASE_URL + "/_ah/login?continue=" + continueURL + "&auth=" + authToken);
      HttpGet method = new HttpGet(uri);
      final HttpParams getParams = new BasicHttpParams();
      HttpClientParams.setRedirecting(getParams, false);
      method.setParams(getParams);

      HttpResponse res = httpClient.execute(method);
      Header[] headers = res.getHeaders("Set-Cookie");
      if (res.getStatusLine().getStatusCode() != 302) {
        Log.v(TAG, "Unexpected status code when getting auth cookie: " + res.getStatusLine().getStatusCode());
        return null;
      }
      if (headers.length == 0) {
        Log.v(TAG, "No headers received when getting auth cookie");
        return null;
      }

      for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
        if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
          return AUTH_COOKIE_NAME + "=" + cookie.getValue();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to get authCookie from token " + authToken, e);
    }

    Log.v(TAG, "No auth cookie received");
    return null;
  }

}