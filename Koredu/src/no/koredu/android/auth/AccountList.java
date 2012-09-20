package no.koredu.android.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class AccountList extends ListActivity {

  protected AccountManager accountManager;
  protected Intent intent;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accountManager = AccountManager.get(getApplicationContext());
    Account[] accounts = accountManager.getAccountsByType("com.google");
    AccountWrapper[] accountWrappers = new AccountWrapper[accounts.length];
    for (int i = 0; i < accounts.length; i++) {
      accountWrappers[i] = new AccountWrapper(accounts[i]);
    }
    this.setListAdapter(new ArrayAdapter<AccountWrapper>(this, android.R.layout.simple_list_item_1, accountWrappers));
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    Account account = (Account) getListView().getItemAtPosition(position);
    Intent intent = new Intent(this, AuthenticationService.class);
    intent.putExtra("account", account);
    startService(intent);
  }

  private static class AccountWrapper extends Account {

    public AccountWrapper(Account account) {
      super(account.name, account.type);
    }

    @Override
    public String toString() {
      return name;
    }
  }

}