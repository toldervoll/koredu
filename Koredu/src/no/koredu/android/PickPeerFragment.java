package no.koredu.android;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.widget.SearchView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import no.koredu.android.database.DatabaseManager;
import no.koredu.android.database.SimpleCursorLoader;
import no.koredu.android.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * TODO: Use Search widget instead of Search dialog for Android 3.0+.
 * See http://developer.android.com/guide/topics/search/search-dialog.html#UsingSearchWidget
 *
 * @author thomas
 */
public class PickPeerFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
  
  private final static String TAG = PickPeerFragment.class.getName();
  private static final int MENU_ITEM_SEARCH = 0;
  private SimpleCursorAdapter mCursorAdapter;
  private SearchView searchView;
  private DatabaseManager db;
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SherlockFragmentActivity activity = getSherlockActivity();
    db = ObjectRegistry.get(activity).getDatabaseManager();
    if (supportsSearchWidget()) {
      searchView = new SearchView(getActivity());
    }
    setHasOptionsMenu(true);
    mCursorAdapter = new SimpleCursorAdapter(activity, android.R.layout.two_line_list_item, null,
        new String[] { "displayName", "phoneNumber" },
        new int[] { android.R.id.text1, android.R.id.text2}, 0);
    setListAdapter(mCursorAdapter);
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new SimpleCursorLoader(getSherlockActivity()) {
      @Override
      public Cursor loadInBackground() {
        return db.getPeersCursor();
      }
    };
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(TAG, "loaded " + data.getCount() + " peers");
    mCursorAdapter.swapCursor(data);
    mCursorAdapter.notifyDataSetChanged();
    if (data.isAfterLast()) {
      showSearch();
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> arg0) {
    mCursorAdapter.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    final Activity activity = getSherlockActivity();
    Peer pickedPeer = db.getPeer((int) id);
    Log.d(TAG, "picked " + pickedPeer.getDisplayName());    
    Intent intent = new Intent(MainActivity.ACTION_INVITE_PEER, null, activity, MainActivity.class);
    intent.putExtra(MainActivity.EXTRA_PEER_ID, (int) id);      
    activity.startActivity(intent);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    MenuItem searchMenuItem = menu.add(Menu.NONE, MENU_ITEM_SEARCH, Menu.CATEGORY_SYSTEM, "Search")
        .setIcon(R.drawable.search);
    searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

    if (supportsSearchWidget()) {
      // Get the SearchView and set the searchable configuration
      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
      searchView.setIconifiedByDefault(true);
      searchMenuItem.setActionView(searchView);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == MENU_ITEM_SEARCH) {
      getActivity().onSearchRequested();
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  private void showSearch() {
    if (supportsSearchWidget()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          searchView.setIconified(false);
          ActivityCompat.invalidateOptionsMenu(getSherlockActivity());
        }
      });
    } else {
      getActivity().onSearchRequested();
    }
  }

  private boolean supportsSearchWidget() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
  }
  
  
  
}
