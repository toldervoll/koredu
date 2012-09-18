package no.koredu.android;

import no.koredu.android.database.DatabaseManager;
import no.koredu.common.Verification;

import java.util.List;

public class PhoneNumberVerifier {

  private final DatabaseManager db;
  private final DeviceIdProvider deviceIdProvider;
  private final ObjectSender objectSender;

  public PhoneNumberVerifier(DatabaseManager db, DeviceIdProvider deviceIdProvider, ObjectSender objectSender) {
    this.db = db;
    this.deviceIdProvider = deviceIdProvider;
    this.objectSender = objectSender;
  }

  public void verify(Verification verification) {
    Verification previousVerification = db.getVerification(verification.getId());
    if (previousVerification == null) {
      // store token and wait for second verification
      db.putVerification(verification);
    } else {
      Verification mergedVerification = verification.mergeWith(previousVerification);
      if ((mergedVerification.getPeerId() != null) && (mergedVerification.getPhoneNumber() != null)) {
        Peer peer = db.getPeer(mergedVerification.getPeerId());
        if (peer.getPhoneNumber().equals(mergedVerification.getPhoneNumber())) {
          mergedVerification.setDeviceId(deviceIdProvider.get());
          objectSender.send("/reportVerified", mergedVerification);
        } else {
          // TODO: report to server
          throw new RuntimeException("verification phone number mismatch for token " + mergedVerification.getId()
              + ", SMS came from " + mergedVerification.getPhoneNumber() + " but invite was sent to "
              + peer.getPhoneNumber());
        }
      }
    }
  }

  public void reportPhoneNumber(String token, String phoneNumber) {
    Verification verification = new Verification(token, phoneNumber);
    verification.setDeviceId(deviceIdProvider.get());
    objectSender.send("/reportPhoneNumber", verification);
  }
}
