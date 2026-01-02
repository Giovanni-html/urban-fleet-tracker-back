package com.urbanfleet.tracker.controller

import com.urbanfleet.tracker.model.Unit
import com.urbanfleet.tracker.model.UnitStatus
import com.urbanfleet.tracker.model.UnitType
import com.urbanfleet.tracker.service.UnitService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/units")
@CrossOrigin(origins = ["http://localhost:5173", "http://localhost:3000"])
class UnitController(private val unitService: UnitService) {
    
    @GetMapping
    fun getAllUnits(): List<Unit> = unitService.getAllUnits()
    
    @GetMapping("/{id}")
    fun getUnitById(@PathVariable id: String): ResponseEntity<Unit> {
        return unitService.getUnitById(id)?.let { 
            ResponseEntity.ok(it) 
        } ?: ResponseEntity.notFound().build()
    }
    
    @GetMapping("/type/{type}")
    fun getUnitsByType(@PathVariable type: UnitType): List<Unit> = 
        unitService.getUnitsByType(type)
    
    @GetMapping("/status/{status}")
    fun getUnitsByStatus(@PathVariable status: UnitStatus): List<Unit> = 
        unitService.getUnitsByStatus(status)
    
    @GetMapping("/statistics")
    fun getStatistics(): Map<String, Any> = unitService.getStatistics()
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUnit(@RequestBody unit: Unit): Unit = unitService.createUnit(unit)
    
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @RequestBody request: StatusUpdateRequest
    ): ResponseEntity<Unit> {
        return unitService.updateUnitStatus(id, request.status)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }
    
    @PatchMapping("/{id}/location")
    fun updateLocation(
        @PathVariable id: String,
        @RequestBody request: LocationUpdateRequest
    ): ResponseEntity<Unit> {
        return unitService.updateUnitLocation(id, request.lat, request.lng, request.location)?.let {
            ResponseEntity.ok(it)
        } ?: ResponseEntity.notFound().build()
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUnit(@PathVariable id: String) = unitService.deleteUnit(id)
}

data class StatusUpdateRequest(val status: UnitStatus)
data class LocationUpdateRequest(val lat: Double, val lng: Double, val location: String)
