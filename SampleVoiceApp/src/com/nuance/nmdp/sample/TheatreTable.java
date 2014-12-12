package com.nuance.nmdp.sample;

import android.os.Parcel;
import android.os.Parcelable;

public class TheatreTable {
	private long id;
	private String name;
	private String streetName;
	private String city;

	  public long getId() {
	    return id;
	  }

	  public void setId(long id) {
	    this.id = id;
	  }

	  public String getTheatreName() {
	    return name;
	  }

	  public void setTheatreName(String theatre) {
	    this.name = theatre;
	  }

	  public String getTheatreStreet() {
	    return streetName;
	  }

	  public void setTheatreStreet(String street) {
	    this.streetName = street;
	  }
		  
	  public String getTheatreCity() {
	    return city;
	  }

	  public void setTheatreCity(String city) {
	    this.city = city;
	  }
	  
	  @Override
	  public String toString() {
	    return name + "\n Address : " + streetName + ", " + city;
	  }
	  
}
