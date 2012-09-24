package no.koredu.testing;

import no.koredu.android.DeviceIdProvider;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class FakeDeviceIdProvider implements DeviceIdProvider {

  private final String deviceId;

  public FakeDeviceIdProvider(String deviceId) {
    this.deviceId = deviceId;
  }

  @Override
  public String get() {
    return deviceId;
  }

}
