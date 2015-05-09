package com.ninehournerd.locosense;

/**
 * Created by katelyn on 4/26/15.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class LocationsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_LOCATION};

    public LocationsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Location createLocation(String location) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_LOCATION, location);
        long insertId = database.insert(MySQLiteHelper.TABLE_LOCATIONS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Location newLocation = cursorToLocation(cursor);
        cursor.close();
        return newLocation;
    }

    public void deleteLocation(Location location) {
        long id = location.getId();
        System.out.println("Location deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_LOCATIONS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<Location>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Location location = cursorToLocation(cursor);
            locations.add(location);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return locations;
    }

    private Location cursorToLocation(Cursor cursor) {
        Location location = new Location();
        location.setId(cursor.getLong(0));
        location.setLocation(cursor.getString(1));
        return location;
    }
}
