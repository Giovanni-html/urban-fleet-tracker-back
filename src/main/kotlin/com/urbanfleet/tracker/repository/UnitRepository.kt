package com.urbanfleet.tracker.repository

import com.urbanfleet.tracker.model.Unit
import com.urbanfleet.tracker.model.UnitStatus
import com.urbanfleet.tracker.model.UnitType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UnitRepository : JpaRepository<Unit, String> {
    fun findByType(type: UnitType): List<Unit>
    fun findByStatus(status: UnitStatus): List<Unit>
    fun findByTypeAndStatus(type: UnitType, status: UnitStatus): List<Unit>
}
