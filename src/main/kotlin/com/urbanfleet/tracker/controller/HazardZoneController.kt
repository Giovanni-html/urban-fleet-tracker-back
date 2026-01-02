package com.urbanfleet.tracker.controller

import com.urbanfleet.tracker.model.HazardZone
import com.urbanfleet.tracker.model.HazardLevel
import com.urbanfleet.tracker.service.HazardZoneService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/hazard-zones")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000"])
class HazardZoneController(private val hazardZoneService: HazardZoneService) {
    
    @GetMapping
    fun getAllHazardZones(): List<HazardZone> = hazardZoneService.getAllHazardZones()
    
    @GetMapping("/active")
    fun getActiveHazardZones(): List<HazardZone> = hazardZoneService.getActiveHazardZones()
    
    @GetMapping("/{id}")
    fun getHazardZoneById(@PathVariable id: UUID): ResponseEntity<HazardZone> {
        return hazardZoneService.getHazardZoneById(id)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }
    
    @GetMapping("/level/{level}")
    fun getHazardZonesByLevel(@PathVariable level: HazardLevel): List<HazardZone> =
        hazardZoneService.getHazardZonesByLevel(level)
    
    @GetMapping("/neighborhood/{neighborhoodId}")
    fun getHazardZoneByNeighborhood(@PathVariable neighborhoodId: String): ResponseEntity<HazardZone> {
        return hazardZoneService.getHazardZoneByNeighborhood(neighborhoodId)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createHazardZone(@RequestBody request: CreateHazardZoneRequest): HazardZone {
        val hazardZone = HazardZone(
            neighborhoodId = request.neighborhoodId,
            level = request.level,
            reason = request.reason ?: "",
            active = true
        )
        return hazardZoneService.createHazardZone(hazardZone)
    }
    
    @PutMapping("/{id}")
    fun updateHazardZone(
        @PathVariable id: UUID,
        @RequestBody request: UpdateHazardZoneRequest
    ): ResponseEntity<HazardZone> {
        val hazardZone = HazardZone(
            neighborhoodId = request.neighborhoodId,
            level = request.level,
            reason = request.reason ?: "",
            active = request.active
        )
        return hazardZoneService.updateHazardZone(id, hazardZone)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }
    
    @PatchMapping("/{id}/deactivate")
    fun deactivateHazardZone(@PathVariable id: UUID): ResponseEntity<Map<String, Boolean>> {
        val success = hazardZoneService.deactivateHazardZone(id)
        return if (success) {
            ResponseEntity.ok(mapOf("success" to true))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteHazardZone(@PathVariable id: UUID) = hazardZoneService.deleteHazardZone(id)
}

data class CreateHazardZoneRequest(
    val neighborhoodId: String,
    val level: HazardLevel,
    val reason: String? = null
)

data class UpdateHazardZoneRequest(
    val neighborhoodId: String,
    val level: HazardLevel,
    val reason: String? = null,
    val active: Boolean = true
)
