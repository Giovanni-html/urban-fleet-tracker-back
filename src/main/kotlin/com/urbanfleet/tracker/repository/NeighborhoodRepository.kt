package com.urbanfleet.tracker.repository

import com.urbanfleet.tracker.model.Neighborhood
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NeighborhoodRepository : JpaRepository<Neighborhood, String> {
    fun findByRegionName(regionName: String): List<Neighborhood>
}
