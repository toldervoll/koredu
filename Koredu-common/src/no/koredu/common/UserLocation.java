package no.koredu.common;

import com.googlecode.objectify.annotation.NotSaved;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import javax.persistence.Id;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
@DatabaseTable
public class UserLocation implements Sanitizable {

  @Id
  private Long id; // unique id on the server-side, userId on the server-side

  private Long userId;

  @DatabaseField
  @NotSaved
  private String displayName;
  @DatabaseField
  private String deviceId;

  @DatabaseField
  private String provider;
  @DatabaseField
  private long time;
  @DatabaseField
  private double latitude;
  @DatabaseField
  private double longitude;
  @DatabaseField
  private Float accuracy;
  @DatabaseField
  private Double altitude;
  @DatabaseField
  private Float bearing;
  @DatabaseField
  private Float speed;

  UserLocation() {
    // needed for Objectify and Jackson
  }

  public UserLocation(String deviceId, String provider, long time, double latitude, double longitude, Float accuracy,
                      Double altitude, Float bearing, Float speed) {
    this.deviceId = deviceId;
    this.provider = provider;
    this.time = time;
    this.latitude = latitude;
    this.longitude = longitude;
    this.accuracy = accuracy;
    this.altitude = altitude;
    this.bearing = bearing;
    this.speed = speed;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Long getUserId() {
    return userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getProvider() {
    return provider;
  }

  public long getTime() {
    return time;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public Float getAccuracy() {
    return accuracy;
  }

  public Double getAltitude() {
    return altitude;
  }

  public Float getBearing() {
    return bearing;
  }

  public Float getSpeed() {
    return speed;
  }

  @Override
  public void sanitize() {
    deviceId = null;
  }

}

