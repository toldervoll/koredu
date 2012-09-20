package no.koredu.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.common.util.concurrent.SettableFuture;
import no.koredu.android.AccountProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class AndroidAccountProvider implements AccountProvider {

  private static final String TAG = AndroidAccountProvider.class.getName();

  private static final String AUTH_PREFS = "AUTH_PREFS";
  private static final String AUTH_PREF_COOKIE = "AUTH_PREF_COOKIE";

  private final Context context;
  private final String accountName;
  private AtomicReference<SettableFuture<String>> authCookieFutureRef;

  public AndroidAccountProvider(Context context) {
    this.context = context;
    AccountManager manager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    Account[] accounts = manager.getAccounts();
    Account account = pickAccount(accounts);
    accountName = account.name;
    Log.v(TAG, "Account name is " + accountName);
    authCookieFutureRef = new AtomicReference<SettableFuture<String>>(SettableFuture.<String>create());
    String authCookie = loadAutenticationCookie();
    if (authCookie != null) {
      Log.d(TAG, "authCookie available when constructing AndroidAccountProvider");
      authCookieFutureRef.get().set(authCookie);
    } else {
      Log.d(TAG, "authCookie not available when constructing AndroidAccountProvider, " +
          "starting AuthenticationService from thread " + Thread.currentThread().getName());
      Intent intent = new Intent(context, AuthenticationService.class);
      intent.putExtra("account", account);
      context.startService(intent);
    }
  }

  private Account pickAccount(Account[] accounts) {
    List<Account> googleAccounts = new ArrayList<Account>();
    for (Account candidateAccount : accounts) {
      if ("com.google".equalsIgnoreCase(candidateAccount.type)) {
        googleAccounts.add(candidateAccount);
      }
    }
    if (googleAccounts.size() == 0) {
      throw new RuntimeException("No Google accounts on device");
    }
    if (googleAccounts.size() > 1) {
      // pick the first Gmail account if there are multiple Google accounts
      for (Account googleAccount : googleAccounts) {
        if (googleAccount.name.endsWith("@gmail.com")) {
          return googleAccount;
        }
      }
    }
    return googleAccounts.get(0);
  }

  @Override
  public String getAccountName() {
    return accountName;
  }

  @Override
  public String getAuthenticationCookie() {
    Log.v(TAG, "Getting auth cookie. Is done? " + authCookieFutureRef.get().isDone()
        + ", thread=" + Thread.currentThread().getName());
    try {
      return authCookieFutureRef.get().get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private String loadAutenticationCookie() {
    return context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE).getString(AUTH_PREF_COOKIE, null);
  }

  public void setAuthenticationCookie(String authCookie) {
    SharedPreferences.Editor editor = context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE).edit();
    editor.putString(AUTH_PREF_COOKIE, authCookie);
    editor.apply();
    SettableFuture<String> newAuthCookieFuture = SettableFuture.create();
    newAuthCookieFuture.set(authCookie);
    authCookieFutureRef.set(newAuthCookieFuture);
  }

}
