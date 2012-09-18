package no.koredu.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class DisplayNameResolver {

  private final Context context;

  public DisplayNameResolver(Context context) {
    this.context = context;
  }

  public String getDisplayName(String phoneNumber) {
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
    ContentResolver resolver = context.getContentResolver();
    Cursor cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
    if (cursor.moveToFirst()) {
      return cursor.getString(0);
    } else {
      return null;
    }
  }
}