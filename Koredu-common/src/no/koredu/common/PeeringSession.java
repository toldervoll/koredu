package no.koredu.common;

import javax.persistence.Id;

public class PeeringSession {

  public enum State {
    CREATED,
    REQUESTED,
    PENDING_VERIFICATION,
    APPROVED,
    DENIED,
    EXPIRED
  }

  @Id private Long id;

  private Long inviterId;
  private String inviterDeviceId;
  private String inviterPhoneNumber;
  private Integer inviterPeerId;

  private Long inviteeId;
  private String inviteeDeviceId;
  private String inviteePhoneNumber;
  private Integer inviteePeerId;
  private String inviteToken;

  private int durationMinutes;
  private long inviteTime;
  private long startTime;
  private State state;
  private String debug;

  PeeringSession() {
    // needed for Objectify
  }

  public PeeringSession(String inviterDeviceId, Integer inviterPeerId, Long inviteeId, String inviteePhoneNumber,
                        int durationMinutes) {
    this.inviterDeviceId = inviterDeviceId;
    this.inviterPeerId = inviterPeerId;
    this.inviteeId = inviteeId;
    this.inviteePhoneNumber = inviteePhoneNumber;
    this.inviteTime = System.currentTimeMillis();
    this.durationMinutes = durationMinutes;
  }

  // always used by invitee
  public static PeeringSession fromToken(String inviteToken, String inviterPhoneNumber, Integer inviteePeerId,
                                         String inviteeDeviceId) {
    PeeringSession session = new PeeringSession();
    session.inviteToken = inviteToken;
    session.inviterPhoneNumber = inviterPhoneNumber;
    session.inviteePeerId = inviteePeerId;
    session.inviteeDeviceId = inviteeDeviceId;
    return session;
  }

  public Long getId() {
    return id;
  }

  public Long getInviterId() {
    return inviterId;
  }

  public void setInviterId(Long inviterId) {
    this.inviterId = inviterId;
  }

  public String getInviterDeviceId() {
    return inviterDeviceId;
  }

  public void setInviterDeviceId(String inviterDeviceId) {
    this.inviterDeviceId = inviterDeviceId;
  }

  public String getInviterPhoneNumber() {
    return inviterPhoneNumber;
  }

  public void setInviterPhoneNumber(String inviterPhoneNumber) {
    this.inviterPhoneNumber = inviterPhoneNumber;
  }

  public Integer getInviterPeerId() {
    return inviterPeerId;
  }

  public Long getInviteeId() {
    return inviteeId;
  }

  public void setInviteeId(Long inviteeId) {
    this.inviteeId = inviteeId;
  }

  public String getInviteeDeviceId() {
    return inviteeDeviceId;
  }

  public void setInviteeDeviceId(String inviteeDeviceId) {
    this.inviteeDeviceId = inviteeDeviceId;
  }

  public String getInviteePhoneNumber() {
    return inviteePhoneNumber;
  }

  public void setInviteePhoneNumber(String inviteePhoneNumber) {
    this.inviteePhoneNumber = inviteePhoneNumber;
  }

  public Integer getInviteePeerId() {
    return inviteePeerId;
  }

  public void setInviteePeerId(Integer inviteePeerId) {
    this.inviteePeerId = inviteePeerId;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(int durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public String getInviteToken() {
    return inviteToken;
  }

  public void setInviteToken(String inviteToken) {
    this.inviteToken = inviteToken;
  }

  public long getInviteTime() {
    return inviteTime;
  }

  public void setInviteTime(long inviteTime) {
    this.inviteTime = inviteTime;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String getDebug() {
    return debug;
  }

  public void setDebug(String debug) {
    this.debug = debug;
  }
}
