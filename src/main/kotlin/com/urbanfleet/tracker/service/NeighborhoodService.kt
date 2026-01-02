package com.urbanfleet.tracker.service

import com.urbanfleet.tracker.model.Neighborhood
import com.urbanfleet.tracker.repository.NeighborhoodRepository
import org.springframework.stereotype.Service

@Service
class NeighborhoodService(private val repository: NeighborhoodRepository) {
    
    fun getAllNeighborhoods(): List<Neighborhood> = repository.findAll()
    
    fun getNeighborhoodById(id: String): Neighborhood? = repository.findById(id).orElse(null)
    
    fun getNeighborhoodsByRegion(regionName: String): List<Neighborhood> = 
        repository.findByRegionName(regionName)
    
    fun saveNeighborhood(neighborhood: Neighborhood): Neighborhood = 
        repository.save(neighborhood)
    
    fun saveAllNeighborhoods(neighborhoods: List<Neighborhood>): List<Neighborhood> = 
        repository.saveAll(neighborhoods)
    
    fun count(): Long = repository.count()
}
