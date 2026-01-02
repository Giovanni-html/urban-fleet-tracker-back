package com.urbanfleet.tracker.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@Entity
@Table(name = "hazard_zones")
data class HazardZone(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    
    @Column(nullable = false)
    @NotBlank
    val neighborhoodId: String, // Referência ao bairro
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val level: HazardLevel = HazardLevel.LOW,
    
    @Column(length = 500)
    val reason: String = "", // Descrição do motivo
    
    @Column(nullable = false)
    var active: Boolean = true
)

enum class HazardLevel {
    HIGH,   // Vermelho 🔴
    MEDIUM, // Laranja 🟠
    LOW     // Amarelo 🟡
}
