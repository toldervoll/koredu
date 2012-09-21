package no.koredu.common;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
public class InviteReply {

  private final String token;
  private final String inviteeDeviceId;

  public InviteReply(String token, String inviteeDeviceId) {
    this.token = token;
    this.inviteeDeviceId = inviteeDeviceId;
  }

  public String getToken() {
    return token;
  }

  public String getInviteeDeviceId() {
    return inviteeDeviceId;
  }

  @Override
  public String toString() {
    return "InviteReply{" +
        "token='" + token + '\'' +
        ", inviteeDeviceId='" + inviteeDeviceId + '\'' +
        '}';
  }
}
