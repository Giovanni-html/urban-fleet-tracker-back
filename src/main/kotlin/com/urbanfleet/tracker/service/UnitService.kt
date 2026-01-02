package com.urbanfleet.tracker.service

import com.urbanfleet.tracker.model.Unit
import com.urbanfleet.tracker.model.UnitStatus
import com.urbanfleet.tracker.model.UnitType
import com.urbanfleet.tracker.repository.UnitRepository
import org.springframework.stereotype.Service

@Service
class UnitService(private val unitRepository: UnitRepository) {
    
    fun getAllUnits(): List<Unit> = unitRepository.findAll()
    
    fun getUnitById(id: String): Unit? = unitRepository.findById(id).orElse(null)
    
    fun getUnitsByType(type: UnitType): List<Unit> = unitRepository.findByType(type)
    
    fun getUnitsByStatus(status: UnitStatus): List<Unit> = unitRepository.findByStatus(status)
    
    fun createUnit(unit: Unit): Unit = unitRepository.save(unit)
    
    fun updateUnitStatus(id: String, status: UnitStatus): Unit? {
        return unitRepository.findById(id).map { unit ->
            unit.status = status
            unitRepository.save(unit)
        }.orElse(null)
    }
    
    fun updateUnitLocation(id: String, lat: Double, lng: Double, location: String): Unit? {
        return unitRepository.findById(id).map { unit ->
            unit.lat = lat
            unit.lng = lng
            unit.location = location
            unitRepository.save(unit)
        }.orElse(null)
    }
    
    fun deleteUnit(id: String) = unitRepository.deleteById(id)
    
    fun getStatistics(): Map<String, Any> {
        val units = unitRepository.findAll()
        return mapOf(
            "totalUnits" to units.size,
            "activeUnits" to units.count { it.status == UnitStatus.ACTIVE },
            "emergencyUnits" to units.count { it.status == UnitStatus.EMERGENCY },
            "idleUnits" to units.count { it.status == UnitStatus.IDLE },
            "patrolUnits" to units.count { it.type == UnitType.PATROL },
            "medicUnits" to units.count { it.type == UnitType.MEDIC }
        )
    }
}
