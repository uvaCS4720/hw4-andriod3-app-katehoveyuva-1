package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import edu.nd.pmcburne.hello.data.AppDatabase
import edu.nd.pmcburne.hello.data.LocationEntity
import edu.nd.pmcburne.hello.data.LocationService // Added import
import edu.nd.pmcburne.hello.data.LocationResponse // Added import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 1. The state definition that the UI will observe
data class MainUIState(
    val locations: List<LocationEntity> = emptyList(),
    val allTags: List<String> = emptyList(),
    val selectedTag: String = "core" // Requirement: Startup tag defaults to "core" [cite: 66, 84]
)

// 2. The ViewModel managing the data and database connection
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Initialize the Room Database
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "uva-locations-db"
    ).build()

    private val locationDao = db.locationDao()

    // UI State Flow
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    // Retrofit Setup
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.cs.virginia.edu/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Explicitly typed to avoid inference errors
    private val locationService: LocationService = retrofit.create(LocationService::class.java)

    init {
        // Run sync on startup [cite: 59]
        syncData()
    }

    // Function to update the filter when a user picks a new tag from the dropdown [cite: 68, 85]
    fun updateSelectedTag(newTag: String) {
        _uiState.update { it.copy(selectedTag = newTag) }
    }

    // Pulls data from SQLite and calculates the unique tag list for the dropdown [cite: 60, 65]
    private fun loadDataFromDatabase() {
        viewModelScope.launch {
            val locations = locationDao.getAllLocations()

            // Extract unique tags, split them, and sort alphabetically [cite: 67, 83]
            val uniqueTags = locations.flatMap { entity ->
                entity.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }.distinct().sorted()

            _uiState.update { it.copy(
                locations = locations,
                allTags = uniqueTags
            )}
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            try {
                // 1. Fetch from API [cite: 59]
                val apiResults = locationService.getLocations()

                // 2. Convert API objects to Database Entities (flattening visual_center) [cite: 53-55]
                val entities = apiResults.map { apiLoc ->
                    LocationEntity(
                        id = apiLoc.id,
                        name = apiLoc.name,
                        description = apiLoc.description,
                        latitude = apiLoc.visual_center.latitude,
                        longitude = apiLoc.visual_center.longitude,
                        tags = apiLoc.tags.joinToString(", ")
                    )
                }

                // 3. Save to DB (Room handles the "no duplicates" via REPLACE) [cite: 61, 82]
                locationDao.insertLocations(entities)

                // 4. Refresh the UI state from the DB
                loadDataFromDatabase()
            } catch (e: Exception) {
                // Fallback to local data if network fails [cite: 60]
                loadDataFromDatabase()
            }
        }
    }
}