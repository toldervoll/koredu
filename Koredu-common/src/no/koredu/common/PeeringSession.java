package no.koredu.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Id;

public class PeeringSession {

  public enum State {
    CREATED,
    REQUESTED,
    APPROVED_BY_INVITEE,
    APPROVED,
    DENIED,
    EXPIRED
  }

  @Id
  private Long id;

  private Long inviterId;
  private String inviterName;
  private String inviterDeviceId;

  private String inviteePhoneNumber;
  private Long inviteeId;
  private String inviteeName;
  private String inviteeDeviceId;

  private int durationMinutes;
  private long inviteTime;
  private long startTime;
  private State state;

  PeeringSession() {
    // needed for Objectify
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

  @JsonIgnore
  public String getInviterDeviceId() {
    return inviterDeviceId;
  }

  public String getInviteePhoneNumber() {
    return inviteePhoneNumber;
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

  @JsonIgnore
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
  public String toString() {
    return "PeeringSession{" +
        "id=" + id +
        ", inviterId=" + inviterId +
        ", inviterName='" + inviterName + '\'' +
        ", inviterDeviceId='" + inviterDeviceId + '\'' +
        ", inviteePhoneNumber='" + inviteePhoneNumber + '\'' +
        ", inviteeId=" + inviteeId +
        ", inviteeName='" + inviteeName + '\'' +
        ", inviteeDeviceId='" + inviteeDeviceId + '\'' +
        ", durationMinutes=" + durationMinutes +
        ", inviteTime=" + inviteTime +
        ", startTime=" + startTime +
        ", state=" + state +
        '}';
  }
}
