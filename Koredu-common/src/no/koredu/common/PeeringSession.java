package no.koredu.common;

import javax.persistence.Id;

public class PeeringSession implements Sanitizable {

  public enum State {
    CREATED,
    REQUESTED,
    APPROVED,
    DENIED,
    EXPIRED
  }

  @Id
  private Long id;

  private Long inviterId;
  private String inviterName;
  private String inviterPhoneNumber;
  private String inviterDeviceId;

  private Long inviteeId;
  private String inviteeName;
  private String inviteePhoneNumber;
  private String inviteeDeviceId;

  private int durationMinutes;
  private long inviteTime;
  private long startTime;
  private State state;

  PeeringSession() {
    // needed for Objectify and Jackson
  }

  // Constructor used by the client
  public PeeringSession(String inviterDeviceId, String inviteePhoneNumber, int durationMinutes) {
    this.inviterDeviceId = inviterDeviceId;
    this.inviteePhoneNumber = inviteePhoneNumber;
    this.inviteTime = System.currentTimeMillis();
    this.durationMinutes = durationMinutes;
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

  public String getInviterName() {
    return inviterName;
  }

  public void setInviterName(String inviterName) {
    this.inviterName = inviterName;
  }

  public String getInviterPhoneNumber() {
    return inviterPhoneNumber;
  }

  public void setInviterPhoneNumber(String inviterPhoneNumber) {
    this.inviterPhoneNumber = inviterPhoneNumber;
  }

  public String getInviterDeviceId() {
    return inviterDeviceId;
  }

  public Long getInviteeId() {
    return inviteeId;
  }

  public void setInviteeId(Long inviteeId) {
    this.inviteeId = inviteeId;
  }

  public String getInviteeName() {
    return inviteeName;
  }

  public void setInviteeName(String inviteeName) {
    this.inviteeName = inviteeName;
  }

  public String getInviteePhoneNumber() {
    return inviteePhoneNumber;
  }

  public String getInviteeDeviceId() {
    return inviteeDeviceId;
  }

  public void setInviteeDeviceId(String inviteeDeviceId) {
    this.inviteeDeviceId = inviteeDeviceId;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(int durationMinutes) {
    this.durationMinutes = durationMinutes;
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

  @Override
  public void sanitize() {
    inviterDeviceId = null;
    inviteeDeviceId = null;
  }

  @Override
  public String toString() {
    return "PeeringSession{" +
        "id=" + id +
        ", inviterId=" + inviterId +
        ", inviterName='" + inviterName + '\'' +
        ", inviterPhoneNumber='" + inviterPhoneNumber + '\'' +
        ", inviterDeviceId='" + inviterDeviceId + '\'' +
        ", inviteeId=" + inviteeId +
        ", inviteeName='" + inviteeName + '\'' +
        ", inviteePhoneNumber='" + inviteePhoneNumber + '\'' +
        ", inviteeDeviceId='" + inviteeDeviceId + '\'' +
        ", durationMinutes=" + durationMinutes +
        ", inviteTime=" + inviteTime +
        ", startTime=" + startTime +
        ", state=" + state +
        '}';
  }
}
