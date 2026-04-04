package edu.nd.pmcburne.hello.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: Int, // Use the API's "id" to prevent duplicates [cite: 44, 61]
    val name: String,        // [cite: 45]
    val description: String, // [cite: 52]
    val latitude: Double,    // Extracted from visual_center [cite: 54]
    val longitude: Double,   // Extracted from visual_center [cite: 55]
    val tags: String         // Store as comma-separated string (e.g. "core, academic") [cite: 47-50]
)