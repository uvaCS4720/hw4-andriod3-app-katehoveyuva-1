package edu.nd.pmcburne.hello.data

import retrofit2.http.GET

interface LocationService {
    @GET("-wxt4gm/placemarks.json")
    suspend fun getLocations(): List<LocationResponse>
}