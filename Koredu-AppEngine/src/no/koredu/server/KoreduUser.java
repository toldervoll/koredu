package no.koredu.server;

import javax.persistence.Id;

public class KoreduUser {

  @Id
  private Long id;
  private String deviceId;
  private long registrationTime;

  KoreduUser() {
    // needed for Objectify
  }

  public KoreduUser(String deviceId) {
    this.deviceId = deviceId;
    registrationTime = System.currentTimeMillis();
  }

  public Long getId() {
    return id;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public long getRegistrationTime() {
    return registrationTime;
  }
}
