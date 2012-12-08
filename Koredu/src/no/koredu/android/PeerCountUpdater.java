package no.koredu.android;

import com.squareup.otto.Bus;
import no.koredu.android.database.DatabaseManager;

import java.util.TimerTask;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PeerCountUpdater extends TimerTask {

  private final DatabaseManager db;
  private final Bus bus;

  public PeerCountUpdater(DatabaseManager db, Bus bus) {
    this.db = db;
    this.bus = bus;
  }

  @Override
  public void run() {
    int peerCount = db.getPeers().size();
    bus.post(new PeerCountUpdateEvent(peerCount));
  }

}
