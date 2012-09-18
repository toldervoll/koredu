package no.koredu.android;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class LocationSubsciption {

  @DatabaseField(generatedId = true)
  private int _id;

  @DatabaseField
  private boolean processed = false;
  
  @DatabaseField
  private boolean approved = false;

  public int get_id() {
    return _id;
  }

  public void set_id(int _id) {
    this._id = _id;
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  public boolean isApproved() {
    return approved;
  }

  public void setApproved(boolean approved) {
    this.approved = approved;
  }
    
}

