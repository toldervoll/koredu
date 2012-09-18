package no.koredu.testing;

import android.database.Cursor;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import no.koredu.android.Peer;
import no.koredu.android.database.DatabaseManager;
import no.koredu.common.UserLocation;
import no.koredu.common.Verification;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class FakeDatabaseManager implements DatabaseManager {

  private SortedMap<Integer, Peer> peers = Maps.newTreeMap();
  private SortedMap<Integer, Verification> verifications = Maps.newTreeMap();
  private Map<Long, UserLocation> locations = Maps.newHashMap();

  @Override
  public Peer getPeer(int id) {
    return peers.get(id);
  }

  @Override
  public Peer getPeerByPhoneNumber(final String phoneNumber) {
    return firstOrNull(Iterables.filter(peers.values(), new Predicate<Peer>() {
      @Override
      public boolean apply(Peer peer) {
        return phoneNumber.equals(peer.getPhoneNumber());
      }
    }));
  }

  @Override
  public Peer getPeerByUserId(final Long userId) {
    return firstOrNull(Iterables.filter(peers.values(), new Predicate<Peer>() {
      @Override
      public boolean apply(Peer peer) {
        return userId.equals(peer.getUserId());
      }
    }));
  }

  @Override
  public List<Peer> getPeers() {
    return Lists.newArrayList(peers.values());
  }

  @Override
  public Peer putPeer(Peer peer) {
    int id = peers.lastKey() + 1;
    peer.setId(id);
    peers.put(id, peer);
    return peer;
  }

  @Override
  public void deleteAllPeers() {
    peers.clear();
  }

  @Override
  public Cursor getPeersCursor() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putVerification(Verification verification) {
    int id = verifications.lastKey() + 1;
    verifications.put(id, verification);
  }

  @Override
  public Verification getVerification(final String token) {
    return firstOrNull(Iterables.filter(verifications.values(), new Predicate<Verification>() {
      @Override
      public boolean apply(Verification verification) {
        return token.equals(verification.getId());
      }
    }));
  }

  @Override
  public List<Verification> getAllVerifications() {
    return Lists.newArrayList(verifications.values());
  }

  @Override
  public void putLocation(UserLocation userLocation) {
    locations.put(userLocation.getUserId(), userLocation);
  }

  private <T> T firstOrNull(Iterable<T> iterable) {
    if (iterable.iterator().hasNext()) {
      return iterable.iterator().next();
    } else {
      return null;
    }
  }
}
