package no.koredu.android;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author thomas@zenior.no (Thomas Oldervoll)
 */
@DatabaseTable
public class Peer {

  @DatabaseField(generatedId = true)
  private int _id;

  @DatabaseField
  private Long userId;
  
  @DatabaseField(canBeNull = false)
  private String displayName;

  @DatabaseField(index = true)
  private String phoneNumber;

  /**
   * latitude in micro-degrees (see {@code GeoPoint})
   */
  @DatabaseField
  private int latitudeE6;

  /**
   * longitude in micro-degrees (see {@code GeoPoint})
   */
  @DatabaseField
  private int longitudeE6;

  @DatabaseField
  private float accuracy;

  @DatabaseField
  private long time;

  @DatabaseField
  private long validUntil;

  private GeoPoint geoPoint;

  Peer() {
    // needed by ORMLite
  }

  public Peer(String displayName, String phoneNumber, int latitudeE6, int longitudeE6, float accuracy, long time, long validUntil) {
    this(displayName, phoneNumber);
    this.latitudeE6 = latitudeE6;
    this.longitudeE6 = longitudeE6;
    this.accuracy = accuracy;
    this.time = time;
    this.validUntil = validUntil;
  }

  public Peer(String displayName, String phoneNumber) {
    this.displayName = displayName;
    this.phoneNumber = phoneNumber;
  }

  public int getId() {
    return _id;
  }

  public void setId(int _id) {
    this._id = _id;
  }
    
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

    
  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public int getLatitudeE6() {
    return latitudeE6;
  }

  public void setLatitudeE6(int latitudeE6) {
    this.latitudeE6 = latitudeE6;
    setGeoPoint();
  }

  public int getLongitudeE6() {
    return longitudeE6;
  }

  public void setLongitudeE6(int longitudeE6) {
    this.longitudeE6 = longitudeE6;
    setGeoPoint();
  }

  public float getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(float accuracy) {
    this.accuracy = accuracy;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public long getValidUntil() {
    return validUntil;
  }

  public void setValidUntil(long validUntil) {
    this.validUntil = validUntil;
  }

  public synchronized GeoPoint getGeoPoint() {
    if (geoPoint == null) {
      setGeoPoint();
    }
    return geoPoint;
  }

  private synchronized void setGeoPoint() {
    this.geoPoint = new GeoPoint(latitudeE6, longitudeE6);
  }
  
  public boolean hasLocation() {
    return (latitudeE6 != 0) && (longitudeE6 != 0); 
  }
}
