package com.ninehournerd.locosense;

/**
 * Created by katelyn on 4/26/15.
 */
public class Location {
    private long id;
    private String location;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return location;
    }
}
