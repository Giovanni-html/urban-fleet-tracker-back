package com.urbanfleet.tracker.controller

import com.urbanfleet.tracker.model.Neighborhood
import com.urbanfleet.tracker.service.NeighborhoodService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/neighborhoods")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000"])
class NeighborhoodController(private val neighborhoodService: NeighborhoodService) {
    
    @GetMapping
    fun getAllNeighborhoods(): List<Neighborhood> = neighborhoodService.getAllNeighborhoods()
    
    @GetMapping("/{id}")
    fun getNeighborhoodById(@PathVariable id: String): ResponseEntity<Neighborhood> {
        return neighborhoodService.getNeighborhoodById(id)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }
    
    @GetMapping("/region/{regionName}")
    fun getNeighborhoodsByRegion(@PathVariable regionName: String): List<Neighborhood> =
        neighborhoodService.getNeighborhoodsByRegion(regionName)
    
    @GetMapping("/count")
    fun getCount(): Map<String, Long> = mapOf("count" to neighborhoodService.count())
}
