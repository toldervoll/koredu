package no.koredu.android.database;

import android.database.Cursor;
import no.koredu.android.Peer;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;

import java.util.List;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public interface DatabaseManager {
  Peer getPeer(int id);

  Peer getPeerByPhoneNumber(String phoneNumber);

  Peer getPeerByUserId(Long userId);

  List<Peer> getPeers();

  Peer putPeer(Peer peer);

  void deleteAllPeers();

  Cursor getPeersCursor();

  void putVerification(Verification token);

  Verification getVerification(String token);

  List<Verification> getAllVerifications();

  void putLocation(UserLocation userLocation);
}
