package edu.nd.pmcburne.hello

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface LocationApi {
    @GET("-wxt4gm/placemarks.json")
    suspend fun getLocations(): List<LocationResponse>
}

data class MainUIState(
    val locations: List<Location> = emptyList(),
    val allTags: List<String> = emptyList(),
    val selectedTag: String = "core" // Default requirement [cite: 66, 84]
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState = _uiState.asStateFlow()

    private val api = Retrofit.Builder()
        .baseUrl("https://www.cs.virginia.edu/")
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(LocationApi::class.java)

    init {
        syncData()
    }

    fun updateSelectedTag(tag: String) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    private fun syncData() {
        viewModelScope.launch {
            try {
                // Fetch and Sync
                val response = api.getLocations()
                val entities = response.map {
                    Location(
                        it.id, it.name, it.description,
                        it.visual_center.latitude, it.visual_center.longitude,
                        it.tags.joinToString(",")
                    )
                }
                dbHelper.insertLocations(entities)
            } catch (e: Exception) {
                // Network fail, fallback to DB [cite: 60]
            }

            val savedLocs = dbHelper.getAllLocations()
            // Alphabetical, unique tags [cite: 67, 83]
            val tags = savedLocs.flatMap { it.tags.split(",") }
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()

            _uiState.update { it.copy(locations = savedLocs, allTags = tags) }
        }
    }
}