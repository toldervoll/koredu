package no.koredu.server;

import javax.persistence.Id;

public class KoreduUser {

  @Id
  private Long id;
  private String gaiaId;
  private String name;
  private String deviceId;
  private long registrationTime;

  KoreduUser() {
    // needed for Objectify
  }

  public KoreduUser(String gaiaId, String name, String deviceId) {
    this.deviceId = deviceId;
    registrationTime = System.currentTimeMillis();
  }

  public Long getId() {
    return id;
  }

  public String getGaiaId() {
    return gaiaId;
  }

  public String getName() {
    return name;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public long getRegistrationTime() {
    return registrationTime;
  }
}
