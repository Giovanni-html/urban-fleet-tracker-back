package com.urbanfleet.tracker.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import java.util.ArrayList

@Service
class MapboxService {

    @Value("\${mapbox.token}")
    private lateinit var mapboxToken: String

    private val restTemplate = RestTemplate()
    private val logger = LoggerFactory.getLogger(MapboxService::class.java)

    fun getRoute(points: List<Point>): List<Point> {
        if (points.size < 2) return points

        // Format coordinates as "lng,lat;lng,lat"
        val coordinates = points.joinToString(";") { "${it.lng},${it.lat}" }
        
        // Use Optimized Trips API for round-trip patrol routes
        // This ensures we get a LOOP that follows real streets
        val url = "https://api.mapbox.com/optimized-trips/v1/mapbox/driving/$coordinates?roundtrip=true&source=first&destination=last&geometries=geojson&overview=full&access_token=$mapboxToken"

        try {
            val response = restTemplate.getForObject(url, MapboxOptimizedResponse::class.java)
            if (response != null && response.trips.isNotEmpty()) {
                val geometry = response.trips[0].geometry
                logger.info("Mapbox returned ${geometry.coordinates.size} coordinate points for the route")
                return geometry.coordinates.map { Point(it[1], it[0]) } // Mapbox is [lng, lat], we want Point(lat, lng)
            } else {
                logger.warn("Mapbox returned empty trips array")
            }
        } catch (e: Exception) {
            logger.error("Error fetching route from Mapbox: ${e.message}", e)
        }
        
        // Fallback: return linear path if API fails
        logger.warn("Using linear fallback path due to API failure")
        return points
    }
}

// Data Classes for JSON Parsing (Optimized Trips API)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MapboxOptimizedResponse(
    val trips: List<MapboxTrip> = emptyList(),
    val code: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapboxTrip(
    val geometry: MapboxGeometry
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapboxGeometry(
    val coordinates: List<List<Double>> // [[lng, lat], [lng, lat]]
)
