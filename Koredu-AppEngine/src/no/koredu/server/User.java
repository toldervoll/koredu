package no.koredu.server;

import javax.persistence.Id;

public class User {

  @Id
  private Long id;
  private String gaiaId;
  private String displayName;
  private String phoneNumber;
  private String deviceId;
  private long registrationTime;

  User() {
    // needed for Objectify
  }

  public User(String deviceId) {
    this.deviceId = deviceId;
    registrationTime = System.currentTimeMillis();
  }

  public Long getId() {
    return id;
  }

  public String getGaiaId() {
    return gaiaId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getDeviceId() {
    return deviceId;
  }


}
