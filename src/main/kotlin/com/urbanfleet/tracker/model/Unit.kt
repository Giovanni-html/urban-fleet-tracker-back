package com.urbanfleet.tracker.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank

@Entity
@Table(name = "units")
data class Unit(
    @Id
    @Column(length = 20)
    val id: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: UnitType,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UnitStatus = UnitStatus.ACTIVE,
    
    @NotBlank
    @Column(nullable = false)
    var location: String,
    
    @Column(nullable = false)
    var lat: Double,
    
    @Column(nullable = false)
    var lng: Double
)

enum class UnitType {
    PATROL, MEDIC
}

enum class UnitStatus {
    ACTIVE, EMERGENCY, IDLE
}
