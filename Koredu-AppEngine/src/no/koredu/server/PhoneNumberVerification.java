package no.koredu.server;

import javax.persistence.Id;

public class PhoneNumberVerification {

  @Id private Long id;
  private Long userId;
  private String phoneNumber;
  private String deviceId;
  private Long reportingUserId;
  private String reportingDeviceId;
  private Integer reportingPeerId;
  private String token;
  private boolean verified = false;

  PhoneNumberVerification() {
    // needed for Objectify
  }

  public PhoneNumberVerification(Long userId, String phoneNumber, Integer reportingPeerId, String reportingDeviceId) {
    super();
    this.userId = userId;
    this.phoneNumber = phoneNumber;
    this.reportingPeerId = reportingPeerId;
    this.reportingDeviceId = reportingDeviceId;
  }
  
  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
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

  public String getReportingDeviceId() {
    return reportingDeviceId;
  }

  public Long getReportingUserId() {
    return reportingUserId;
  }

  public void setReportingUserId(Long reportingUserId) {
    this.reportingUserId = reportingUserId;
  }
    
  public Integer getReportingPeerId() {
    return reportingPeerId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setVerified() {
    this.verified = true;
  }

  public boolean isVerified() {
    return verified;
  }
  
}
