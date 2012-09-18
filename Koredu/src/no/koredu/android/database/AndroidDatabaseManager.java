package no.koredu.android.database;

import java.sql.SQLException;
import java.util.List;

import no.koredu.android.Peer;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;
import android.content.Context;
import android.database.Cursor;

import com.j256.ormlite.android.AndroidCompiledStatement;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class AndroidDatabaseManager implements DatabaseManager {

  private final DatabaseHelper helper;

  public AndroidDatabaseManager(Context context) {
    helper = new DatabaseHelper(context);
  }

  private DatabaseHelper getHelper() {
    return helper;
  }

  @Override
  public Peer getPeer(int id) {
    try {
      return getHelper().getPeerDao().queryForId(id);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Peer getPeerByPhoneNumber(String phoneNumber) {
    try {
      List<Peer> peers = getHelper().getPeerDao().queryForEq("phoneNumber", phoneNumber);
      return peers.isEmpty() ? null : peers.get(0);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Peer getPeerByUserId(Long userId) {
    try {
      List<Peer> peers = getHelper().getPeerDao().queryForEq("userId", userId);
      return peers.isEmpty() ? null : peers.get(0);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Peer> getPeers() {
    long now = System.currentTimeMillis();
    try {
      return getHelper().getPeerDao().queryBuilder()
          //.where()
          //.gt("validUntil", now)
          .query();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Peer putPeer(Peer peer) {
    try {
      getHelper().getPeerDao().create(peer);
      return peer;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deleteAllPeers() {
    try {
      Dao<Peer, Integer> peerDao = getHelper().getPeerDao();
      peerDao.delete(peerDao.deleteBuilder().prepare());          
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Cursor getPeersCursor() {
    try {
      PreparedQuery<Peer> query = getHelper().getPeerDao().queryBuilder()
//          .orderBy("usageCount", false)
          .prepare();
      DatabaseConnection databaseConnection = getHelper().getConnectionSource().getReadOnlyConnection();
      AndroidCompiledStatement compiledStatement =
          (AndroidCompiledStatement)query.compile(databaseConnection, StatementBuilder.StatementType.SELECT);
      return compiledStatement.getCursor();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }    
  }
  
  @Override
  public void putVerification(Verification token) {
    try {
      getHelper().getVerificationDao().create(token);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }        
  }
  
  @Override
  public Verification getVerification(String token) {
    try {
      return getHelper().getVerificationDao().queryForId(token);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }        
  }

  @Override
  public List<Verification> getAllVerifications() {
    try {
      return getHelper().getVerificationDao().queryForAll();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void putLocation(UserLocation userLocation) {
    try {
      getHelper().getLocationDao().create(userLocation);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}

