package edu.nd.pmcburne.hello.data

// This matches the JSON structure exactly
data class LocationResponse(
    val id: Int,
    val name: String,
    val description: String,
    val tags: List<String>,
    val visual_center: VisualCenter
)

data class VisualCenter(
    val latitude: Double,
    val longitude: Double
)