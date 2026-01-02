package com.urbanfleet.tracker.service

import com.urbanfleet.tracker.model.HazardZone
import com.urbanfleet.tracker.model.HazardLevel
import com.urbanfleet.tracker.repository.HazardZoneRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HazardZoneService(private val repository: HazardZoneRepository) {
    
    fun getAllHazardZones(): List<HazardZone> = repository.findAll()
    
    fun getActiveHazardZones(): List<HazardZone> = repository.findByActiveTrue()
    
    fun getHazardZoneById(id: UUID): HazardZone? = repository.findById(id).orElse(null)
    
    fun getHazardZonesByLevel(level: HazardLevel): List<HazardZone> = 
        repository.findByLevel(level)
    
    fun getHazardZoneByNeighborhood(neighborhoodId: String): HazardZone? = 
        repository.findByNeighborhoodId(neighborhoodId)
    
    fun createHazardZone(hazardZone: HazardZone): HazardZone {
        // Check if neighborhood already has a hazard zone
        val existing = repository.findByNeighborhoodId(hazardZone.neighborhoodId)
        if (existing != null) {
            // Update existing zone
            return repository.save(existing.copy(
                level = hazardZone.level,
                reason = hazardZone.reason,
                active = true
            ))
        }
        return repository.save(hazardZone)
    }
    
    fun updateHazardZone(id: UUID, hazardZone: HazardZone): HazardZone? {
        return repository.findById(id).map { existing ->
            repository.save(existing.copy(
                level = hazardZone.level,
                reason = hazardZone.reason,
                active = hazardZone.active
            ))
        }.orElse(null)
    }
    
    fun deactivateHazardZone(id: UUID): Boolean {
        return repository.findById(id).map { zone ->
            zone.active = false
            repository.save(zone)
            true
        }.orElse(false)
    }
    
    fun deleteHazardZone(id: UUID) = repository.deleteById(id)
    
    fun isNeighborhoodHazardZone(neighborhoodId: String): Boolean = 
        repository.existsByNeighborhoodIdAndActiveTrue(neighborhoodId)
}
