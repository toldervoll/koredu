package no.koredu.common;

import javax.persistence.Id;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class Invite {

  @Id
  private Long id;
  private String token;
  private String phoneNumber;
  private Long sessionId;

  Invite() {
    // needed for Objectify
  }

  public Invite(String phoneNumber, Long sessionId) {
    this.phoneNumber = phoneNumber;
    this.sessionId = sessionId;
  }

  public Long getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public Long getSessionId() {
    return sessionId;
  }

  @Override
  public String toString() {
    return "Invite{" +
        "id=" + id +
        ", token='" + token + '\'' +
        ", phoneNumber='" + phoneNumber + '\'' +
        ", sessionId=" + sessionId +
        '}';
  }
}
