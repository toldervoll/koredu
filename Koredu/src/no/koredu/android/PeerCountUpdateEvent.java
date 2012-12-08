package no.koredu.android;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class PeerCountUpdateEvent {

  private final int peerCount;

  public PeerCountUpdateEvent(int peerCount) {
    this.peerCount = peerCount;
  }

  public int getPeerCount() {
    return peerCount;
  }
}
