package no.koredu.common;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Verification {

  @DatabaseField(id = true)
  private String id;

  @DatabaseField
  private Integer peerId;
  @DatabaseField
  private String phoneNumber;
  @DatabaseField
  private String deviceId;

  Verification() {
    // needed by ORMLite and Jackson
  }

  public Verification(String token, Integer peerId) {
    this.id = token;
    this.peerId = peerId;
  }

  public Verification(String token, String phoneNumber) {
    this.id = token;
    this.phoneNumber = phoneNumber;
  }

  public Verification mergeWith(Verification other) {
    Verification merged = new Verification();
    merged.id = this.id;
    merged.peerId = this.peerId != null ? this.peerId : other.peerId;
    merged.phoneNumber = this.phoneNumber != null ? this.phoneNumber : other.phoneNumber;
    return merged;
  }

  public String getId() {
    return id;
  }

  public Integer getPeerId() {
    return peerId;
  }

  public void setPeerId(Integer peerId) {
    this.peerId = peerId;
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

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

}
