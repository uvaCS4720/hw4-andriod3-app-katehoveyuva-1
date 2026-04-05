package edu.nd.pmcburne.hello

// JSON response mapping [cite: 40-41]
data class LocationResponse(
    val id: Int,
    val name: String,
    val description: String,
    val tags: List<String>,
    val visual_center: VisualCenter
)

data class VisualCenter(val latitude: Double, val longitude: Double)

// Internal data class for the App
data class Location(
    val id: Int,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val tags: String // Flattened string for SQLite
)