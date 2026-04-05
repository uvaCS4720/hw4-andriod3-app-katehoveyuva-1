package edu.nd.pmcburne.hello

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "uva_locations.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE locations (id INTEGER PRIMARY KEY, name TEXT, description TEXT, lat REAL, lng REAL, tags TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {}

    fun insertLocations(locations: List<Location>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            locations.forEach { loc ->
                val values = ContentValues().apply {
                    put("id", loc.id)
                    put("name", loc.name)
                    put("description", loc.description)
                    put("lat", loc.latitude)
                    put("lng", loc.longitude)
                    put("tags", loc.tags)
                }
                // CONFLICT_REPLACE ensures "no duplicate entries" [cite: 61, 82]
                db.insertWithOnConflict("locations", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllLocations(): List<Location> {
        val list = mutableListOf<Location>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM locations", null)
        while (cursor.moveToNext()) {
            list.add(Location(
                id = cursor.getInt(0),
                name = cursor.getString(1),
                description = cursor.getString(2),
                latitude = cursor.getDouble(3),
                longitude = cursor.getDouble(4),
                tags = cursor.getString(5)
            ))
        }
        cursor.close()
        return list
    }
}