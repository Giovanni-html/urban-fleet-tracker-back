package com.urbanfleet.tracker.config

import com.urbanfleet.tracker.model.Neighborhood
import com.urbanfleet.tracker.repository.NeighborhoodRepository
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource

@Configuration
class NeighborhoodInitializer {
    
    @Bean
    @Profile("!test")
    fun initNeighborhoods(repository: NeighborhoodRepository, objectMapper: ObjectMapper) = CommandLineRunner {
        if (repository.count() == 0L) {
            try {
                val resource = ClassPathResource("bairros.topo.json")
                if (resource.exists()) {
                    val topoJson = objectMapper.readTree(resource.inputStream)
                    val neighborhoods = parseTopoJson(topoJson, objectMapper)
                    repository.saveAll(neighborhoods)
                    println("✅ Loaded ${neighborhoods.size} Curitiba neighborhoods into database")
                } else {
                    println("⚠️ bairros.topo.json not found - skipping neighborhood initialization")
                }
            } catch (e: Exception) {
                println("⚠️ Error loading neighborhoods: ${e.message}")
            }
        }
    }
    
    private fun parseTopoJson(topoJson: JsonNode, objectMapper: ObjectMapper): List<Neighborhood> {
        val neighborhoods = mutableListOf<Neighborhood>()
        
        // TopoJSON structure: objects.bairros.geometries
        val objects = topoJson.get("objects")
        if (objects == null) {
            println("⚠️ No 'objects' found in TopoJSON")
            return neighborhoods
        }
        
        // Find the first object (usually 'bairros' or similar)
        val firstObjectName = objects.fieldNames().asSequence().firstOrNull()
        if (firstObjectName == null) {
            println("⚠️ No geometry collections found in TopoJSON")
            return neighborhoods
        }
        
        val geometries = objects.get(firstObjectName)?.get("geometries")
        if (geometries == null || !geometries.isArray) {
            println("⚠️ No geometries array found")
            return neighborhoods
        }
        
        // Get transform for coordinate conversion
        val transform = topoJson.get("transform")
        val scale = transform?.get("scale")?.let { 
            doubleArrayOf(it.get(0).asDouble(), it.get(1).asDouble())
        } ?: doubleArrayOf(1.0, 1.0)
        val translate = transform?.get("translate")?.let {
            doubleArrayOf(it.get(0).asDouble(), it.get(1).asDouble())
        } ?: doubleArrayOf(0.0, 0.0)
        
        // Get arcs for geometry reconstruction
        val arcs = topoJson.get("arcs")
        
        for (geometry in geometries) {
            val properties = geometry.get("properties")
            val name = properties?.get("NOME")?.asText() 
                ?: properties?.get("nome")?.asText()
                ?: properties?.get("NM_BAIRRO")?.asText()
                ?: "Unknown"
            
            val regionName = properties?.get("REGIONAL")?.asText()
                ?: properties?.get("regional")?.asText()
                ?: ""
            
            // Convert TopoJSON geometry to GeoJSON
            val geoJsonGeometry = convertToGeoJson(geometry, arcs, scale, translate, objectMapper)
            
            neighborhoods.add(Neighborhood(
                id = name.uppercase().replace(" ", "_"),
                geometry = objectMapper.writeValueAsString(geoJsonGeometry),
                regionName = regionName
            ))
        }
        
        return neighborhoods
    }
    
    private fun convertToGeoJson(
        geometry: JsonNode, 
        arcs: JsonNode?, 
        scale: DoubleArray, 
        translate: DoubleArray,
        objectMapper: ObjectMapper
    ): Map<String, Any> {
        val type = geometry.get("type")?.asText() ?: "Polygon"
        val arcsNode = geometry.get("arcs")
        
        if (arcs == null || arcsNode == null) {
            return mapOf("type" to type, "coordinates" to emptyList<Any>())
        }
        
        val coordinates = when (type) {
            "Polygon" -> {
                arcsNode.map { ring ->
                    decodeRing(ring, arcs, scale, translate)
                }
            }
            "MultiPolygon" -> {
                arcsNode.map { polygon ->
                    polygon.map { ring ->
                        decodeRing(ring, arcs, scale, translate)
                    }
                }
            }
            else -> emptyList()
        }
        
        return mapOf("type" to type, "coordinates" to coordinates)
    }
    
    private fun decodeRing(ring: JsonNode, arcs: JsonNode, scale: DoubleArray, translate: DoubleArray): List<List<Double>> {
        val points = mutableListOf<List<Double>>()
        
        for (arcIndex in ring) {
            val index = arcIndex.asInt()
            val arcData = if (index >= 0) arcs.get(index) else arcs.get(index.inv())
            val reverse = index < 0
            
            // Decode this arc - each arc has its own delta encoding
            val arcPoints = mutableListOf<List<Double>>()
            var x = 0.0
            var y = 0.0
            
            for (point in arcData) {
                x += point.get(0).asDouble()
                y += point.get(1).asDouble()
                val lng = x * scale[0] + translate[0]
                val lat = y * scale[1] + translate[1]
                arcPoints.add(listOf(lng, lat))
            }
            
            // Reverse the arc points if index was negative
            val orderedArcPoints = if (reverse) arcPoints.reversed() else arcPoints
            
            // Skip first point if not first arc (to avoid duplicates at connection points)
            val startIndex = if (points.isNotEmpty()) 1 else 0
            points.addAll(orderedArcPoints.drop(startIndex))
        }
        
        return points
    }
}
