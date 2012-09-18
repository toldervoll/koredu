package no.koredu.android;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import no.koredu.android.database.DatabaseManager;
import no.koredu.android.database.SimpleCursorLoader;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * TODO: show search button on search result page to let the user do another search
 * TODO: show "Search results for 'query' at the top - especially needed for voice search
 */
public class ContactSearchFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {


  private final static String TAG = ContactSearchFragment.class.getName();

  private SimpleCursorAdapter mCursorAdapter;
  private DatabaseManager db;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SherlockFragmentActivity activity = getSherlockActivity();
    db = ObjectRegistry.get(activity).getDatabaseManager();
    Intent intent = activity.getIntent();
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      doSearch(intent.getExtras());
    }
  }

  private void doSearch(Bundle args) {
    Log.d(TAG, "Searching for " + args.getString(SearchManager.QUERY));
    mCursorAdapter = new SimpleCursorAdapter(getSherlockActivity(), android.R.layout.two_line_list_item, null,
        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
        new int[]{android.R.id.text1, android.R.id.text2}, 0);
    setListAdapter(mCursorAdapter);
    getLoaderManager().initLoader(0, args, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
    final Context context = getSherlockActivity();
    return new SimpleCursorLoader(context) {
      @Override
      public Cursor loadInBackground() {
        String query = args.getString(SearchManager.QUERY);
        Uri queryUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, query);
        return context.getContentResolver().query(queryUri, null, null, null, null);
      }
    };
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCursorAdapter.swapCursor(data);
    mCursorAdapter.notifyDataSetChanged();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    mCursorAdapter.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    final Activity activity = getSherlockActivity();
    Uri lookupUri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, Long.toString(id));
    Cursor cursor = activity.getContentResolver().query(lookupUri, null, null, null, null);
    if (cursor.moveToFirst()) {
      String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
      String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
      Peer pickedPeer = db.getPeerByPhoneNumber(phoneNumber);
      if (pickedPeer == null) {
        pickedPeer = new Peer(displayName, phoneNumber);
        db.putPeer(pickedPeer);
      }
      Log.d(TAG, "picked " + pickedPeer.getDisplayName() + " from search");
      Intent intent = new Intent(MainActivity.ACTION_INVITE_PEER, null, activity, MainActivity.class);
      intent.putExtra(MainActivity.EXTRA_PEER_ID, pickedPeer.getId());
      activity.startActivity(intent);
    }
  }

}
