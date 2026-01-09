package com.urbanfleet.tracker.config

import com.urbanfleet.tracker.model.Unit
import com.urbanfleet.tracker.model.UnitStatus
import com.urbanfleet.tracker.model.UnitType
import com.urbanfleet.tracker.repository.UnitRepository
import com.urbanfleet.tracker.service.PatrolSimulationService
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class DataInitializer {
    
    @Bean
    @Profile("!test")
    fun initDatabase(repository: UnitRepository, patrolService: PatrolSimulationService) = CommandLineRunner {
        if (repository.count() == 0L) {
            val units = listOf(
                Unit("VTR-01", UnitType.PATROL, UnitStatus.ACTIVE, "Centro Cívico", -25.4190, -49.2680),
                Unit("VTR-05", UnitType.PATROL, UnitStatus.ACTIVE, "Batel", -25.4390, -49.2820),
                Unit("AMB-10", UnitType.MEDIC, UnitStatus.EMERGENCY, "Água Verde", -25.4480, -49.2770),
                Unit("VTR-03", UnitType.PATROL, UnitStatus.ACTIVE, "Rebouças", -25.4420, -49.2650),
                Unit("VTR-08", UnitType.PATROL, UnitStatus.ACTIVE, "Jardim Botânico", -25.4430, -49.2400),
                Unit("AMB-12", UnitType.MEDIC, UnitStatus.EMERGENCY, "Bigorrilho", -25.4320, -49.2950),
                Unit("VTR-11", UnitType.PATROL, UnitStatus.ACTIVE, "Cabral", -25.4080, -49.2540),
                Unit("VTR-14", UnitType.PATROL, UnitStatus.ACTIVE, "Alto da Glória", -25.4210, -49.2590),
                Unit("AMB-15", UnitType.MEDIC, UnitStatus.IDLE, "Cristo Rei", -25.4290, -49.2500),
                Unit("VTR-18", UnitType.PATROL, UnitStatus.ACTIVE, "Mercês", -25.4250, -49.2890),
                Unit("VTR-22", UnitType.PATROL, UnitStatus.ACTIVE, "Prado Velho", -25.4520, -49.2520),
                Unit("VTR-25", UnitType.PATROL, UnitStatus.ACTIVE, "Juvevê", -25.4150, -49.2620),
                Unit("AMB-30", UnitType.MEDIC, UnitStatus.ACTIVE, "Portão", -25.4680, -49.2880),
                Unit("VTR-29", UnitType.PATROL, UnitStatus.IDLE, "Ahú", -25.3990, -49.2720),
                Unit("VTR-33", UnitType.PATROL, UnitStatus.ACTIVE, "Bom Retiro", -25.4120, -49.2780)
            )
            repository.saveAll(units)
            println("✅ Loaded ${units.size} mock units into database")
            
            // Trigger simulation update
            patrolService.refreshUnits()
            println("✅ Initialized patrol simulation")
        }
    }
}
