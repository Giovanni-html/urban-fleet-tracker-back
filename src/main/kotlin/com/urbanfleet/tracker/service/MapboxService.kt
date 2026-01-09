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
        val url = "https://api.mapbox.com/directions/v5/mapbox/driving/$coordinates?geometries=geojson&access_token=$mapboxToken"

        try {
            val response = restTemplate.getForObject(url, MapboxResponse::class.java)
            if (response != null && response.routes.isNotEmpty()) {
                val geometry = response.routes[0].geometry
                return geometry.coordinates.map { Point(it[1], it[0]) } // Mapbox is [lng, lat], we want Point(lat, lng)
            }
        } catch (e: Exception) {
            logger.error("Error fetching route from Mapbox: ${e.message}")
        }
        
        // Fallback: return linear path if API fails
        logger.warn("Using linear fallback path due to API failure")
        return points
    }
}

// Data Classes for JSON Parsing
@JsonIgnoreProperties(ignoreUnknown = true)
data class MapboxResponse(
    val routes: List<MapboxRoute> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapboxRoute(
    val geometry: MapboxGeometry
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MapboxGeometry(
    val coordinates: List<List<Double>> // [[lng, lat], [lng, lat]]
)
