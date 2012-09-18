package no.koredu.common;

import javax.persistence.Id;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class Invite {

  @Id
  private Long id;
  private String inviteToken;
  private String phoneNumber;
  private Long sessionId;

  Invite() {
    // needed for Objectify
  }

  public Invite(String inviteToken, String phoneNumber, Long sessionId) {
    this.inviteToken = inviteToken;
    this.phoneNumber = phoneNumber;
    this.sessionId = sessionId;
  }

  public Long getId() {
    return id;
  }

  public String getInviteToken() {
    return inviteToken;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public Long getSessionId() {
    return sessionId;
  }
}
