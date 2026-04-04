package edu.nd.pmcburne.hello.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    // This handles the "no duplicates" rule by replacing existing IDs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    // We'll use this to get all data from the database [cite: 60]
    @Query("SELECT * FROM locations")
    suspend fun getAllLocations(): List<LocationEntity>
}