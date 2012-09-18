package no.koredu.android.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import no.koredu.android.Peer;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

  private static final String DATABASE_NAME = "KoreduDB.sqlite";
  private static final int DATABASE_VERSION = 1;

  private Dao<Peer, Integer> peerDao;
  private Dao<Verification, String> verificationDao;
  private Dao<UserLocation, Long> locationDao;

  public DatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
    try {
      TableUtils.createTable(connectionSource, Peer.class);
      TableUtils.createTable(connectionSource, Verification.class);
      TableUtils.createTable(connectionSource, UserLocation.class);
    } catch (SQLException e) {
      Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
      throw new RuntimeException(e);
    } catch (java.sql.SQLException e) {
      Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onUpgrade(SQLiteDatabase db,ConnectionSource connectionSource, int oldVersion, int newVersion) {
    try {
//      List<String> allSql = new ArrayList<String>();
//      switch(oldVersion)
//      {
//        case 1:
//          //allSql.add("alter table AdData add column `new_col` VARCHAR");
//          //allSql.add("alter table AdData add column `new_col2` VARCHAR");
//      }
//      for (String sql : allSql) {
//        db.execSQL(sql);
//      }
      TableUtils.dropTable(connectionSource, Peer.class, false);
      TableUtils.dropTable(connectionSource, Verification.class, false);
      TableUtils.dropTable(connectionSource, UserLocation.class, false);
      onCreate(db, connectionSource);
    } catch (SQLException e) {
      Log.e(DatabaseHelper.class.getName(), "exception during onUpgrade", e);
      throw new RuntimeException(e);
    } catch (java.sql.SQLException e) {
      Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
      throw new RuntimeException(e);
    }
  }

  public Dao<Peer, Integer> getPeerDao() {
    if (null == peerDao) {
      try {
        peerDao = getDao(Peer.class);
      } catch (java.sql.SQLException e) {
        throw new RuntimeException(e);
      }
    }
    return peerDao;
  }

  public Dao<Verification, String> getVerificationDao() {
    if (null == verificationDao) {
      try {
        verificationDao = getDao(Verification.class);
      } catch (java.sql.SQLException e) {
        throw new RuntimeException(e);
      }
    }
    return verificationDao;
  }

  public Dao<UserLocation, Long> getLocationDao() {
    if (null == locationDao) {
      try {
        locationDao = getDao(UserLocation.class);
      } catch (java.sql.SQLException e) {
        throw new RuntimeException(e);
      }
    }
    return locationDao;
  }
}

