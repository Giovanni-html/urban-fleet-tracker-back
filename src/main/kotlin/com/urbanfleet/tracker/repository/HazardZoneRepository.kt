package com.urbanfleet.tracker.repository

import com.urbanfleet.tracker.model.HazardZone
import com.urbanfleet.tracker.model.HazardLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HazardZoneRepository : JpaRepository<HazardZone, UUID> {
    fun findByActiveTrue(): List<HazardZone>
    fun findByLevel(level: HazardLevel): List<HazardZone>
    fun findByNeighborhoodId(neighborhoodId: String): HazardZone?
    fun existsByNeighborhoodIdAndActiveTrue(neighborhoodId: String): Boolean
}
