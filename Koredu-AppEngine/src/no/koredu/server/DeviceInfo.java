package no.koredu.server;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DeviceInfo {
  @Id
  private String deviceRegistrationID;

  public String getDeviceRegistrationID() {
    return deviceRegistrationID;
  }

  public void setDeviceRegistrationID(String deviceRegistrationID) {
    this.deviceRegistrationID = deviceRegistrationID;
  }

}
