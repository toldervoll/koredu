package no.koredu.common;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class InviteReply {

  private String token;
  private String inviterPhoneNumber;
  private String inviteeDeviceId;

  InviteReply() {
    // needed for Jackson
  }

  public InviteReply(String token, String inviterPhoneNumber, String inviteeDeviceId) {
    this.token = token;
    this.inviterPhoneNumber = inviterPhoneNumber;
    this.inviteeDeviceId = inviteeDeviceId;
  }

  public String getToken() {
    return token;
  }

  public String getInviterPhoneNumber() {
    return inviterPhoneNumber;
  }

  public String getInviteeDeviceId() {
    return inviteeDeviceId;
  }

  @Override
  public String toString() {
    return "InviteReply{" +
        "token='" + token + '\'' +
        ", inviterPhoneNumber='" + inviterPhoneNumber + '\'' +
        ", inviteeDeviceId='" + inviteeDeviceId + '\'' +
        '}';
  }
}
