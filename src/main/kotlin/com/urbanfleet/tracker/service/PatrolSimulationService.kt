package com.urbanfleet.tracker.service

import com.urbanfleet.tracker.model.UnitStatus
import com.urbanfleet.tracker.model.UnitType
import com.urbanfleet.tracker.repository.UnitRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class SimulationState(
    val unitId: String,
    var routeIndex: Int,
    var waypointIndex: Int,
    var progress: Double, // 0.0 to 1.0 along current segment
    var speed: Double, // km/h (was progress per tick)
    var currentLat: Double,
    var currentLng: Double,
    var bearing: Double,
    var stopUntil: Long = 0 // Timestamp to resume movement
)

// Simple coordinate class
data class Point(val lat: Double, val lng: Double)

@Service
class PatrolSimulationService(
    private val unitRepository: UnitRepository,
    private val template: SimpMessagingTemplate,
    private val mapboxService: MapboxService
) {
    private val logger = LoggerFactory.getLogger(PatrolSimulationService::class.java)
    private val simulationStates = ConcurrentHashMap<String, SimulationState>()
    private val R_EARTH = 6371000.0 // Earth radius in meters
    
    // Store generated routes per unit to reuse references
    private val unitRoutes = ConcurrentHashMap<String, List<Point>>()

    @PostConstruct
    fun init() {
        logger.info("Initializing Patrol Simulation Service with Dynamic Mapbox Routes...")
        val units = unitRepository.findAll()
        
        units.forEach { unit ->
            // DYNAMIC ROUTE GENERATION
            // Generate 3 random checkpoints around the unit's base (~500m-800m radius)
            // to create a realistic patrol loop for THIS neighborhood.
            val center = Point(unit.lat, unit.lng)
            val checkpoints = mutableListOf<Point>()
            checkpoints.add(center) // Start
            
            for (i in 1..3) {
                // Random offset: roughly 0.005 degrees is ~500m
                val latOffset = (Math.random() - 0.5) * 0.010 
                val lngOffset = (Math.random() - 0.5) * 0.010
                checkpoints.add(Point(center.lat + latOffset, center.lng + lngOffset))
            }
            checkpoints.add(center) // Loop back to start
            
            // Fetch REAL street path from Mapbox
            logger.info("Fetching Mapbox route for unit ${unit.id}...")
            val realRoute = mapboxService.getRoute(checkpoints)
            unitRoutes[unit.id] = realRoute
            
            if (realRoute.isEmpty()) {
                logger.error("Failed to generate route for ${unit.id}")
                return@forEach
            }
            
            // Assign realistic speed (km/h)
            val isEmergency = unit.status == UnitStatus.EMERGENCY
            val minSpeed = if (isEmergency) 60.0 else 30.0
            val maxSpeed = if (isEmergency) 80.0 else 50.0
            val initialSpeed = minSpeed + (Math.random() * (maxSpeed - minSpeed))
            
            // Random start position within the real route
            val startWaypointIdx = (Math.random() * (realRoute.size - 1)).toInt()
            
            // Initial Pos
            val currentWp = realRoute[startWaypointIdx]
            
            simulationStates[unit.id] = SimulationState(
                unitId = unit.id,
                routeIndex = 0, // Not used anymore, we use unitRoutes map
                waypointIndex = startWaypointIdx,
                progress = 0.0,
                speed = initialSpeed, 
                currentLat = currentWp.lat,
                currentLng = currentWp.lng,
                bearing = 0.0,
                stopUntil = 0L
            )
        }
        logger.info("Initialized simulation for ${simulationStates.size} units with real street data")
    }
    
    @Scheduled(fixedRate = 50)
    fun updatePositions() {
        if (simulationStates.isEmpty()) return
        
        val currentTime = System.currentTimeMillis()
        val tickSeconds = 0.050 
        
        val updates = mutableListOf<Map<String, Any>>()
        
        simulationStates.values.forEach { state ->
            // Traffic Light / Stop Logic
            if (state.stopUntil > currentTime) {
                updates.add(mapOf(
                    "id" to state.unitId,
                    "lat" to state.currentLat,
                    "lng" to state.currentLng,
                    "bearing" to state.bearing
                ))
                return@forEach
            }
        
            val route = unitRoutes[state.unitId] ?: return@forEach
            val currentWaypoint = route[state.waypointIndex]
            val nextIndex = (state.waypointIndex + 1) % route.size
            val nextWaypoint = route[nextIndex]
            
            val segmentDistMeters = calculateDistanceMeters(currentWaypoint, nextWaypoint)
            val speedMps = state.speed / 3.6
            val distanceTraveled = speedMps * tickSeconds
            
            // If segment is tiny (Mapbox often returns points 1m apart), jump it
            val progressIncrement = if (segmentDistMeters > 0.5) distanceTraveled / segmentDistMeters else 1.0
            
            state.progress += progressIncrement
            
            if (state.progress >= 1.0) {
                state.progress = 0.0
                state.waypointIndex = nextIndex
                
                // Traffic Simulation
                // Mapbox routes have MANY nodes. Only stop if angle changes significantly (Corner) 
                // or very rare random chance.
                
                // Calculate turn angle
                val prevIndex = if (state.waypointIndex == 0) route.size - 1 else state.waypointIndex - 1
                val prevWp = route[prevIndex]
                val nextWp2 = route[(state.waypointIndex + 1) % route.size]
                
                // Heuristic: Chance to stop is lower because points are dense
                // But we want to stop at "intersections".
                // Simple random for now, reduced chance
                if (Math.random() < 0.02) { // 2% chance per node (since nodes are frequent)
                    val stopDurationMs = (5000 + Math.random() * 8000).toLong()
                    state.stopUntil = currentTime + stopDurationMs
                }
            }
            
            // Interpolate
            val latDiff = nextWaypoint.lat - currentWaypoint.lat
            val lngDiff = nextWaypoint.lng - currentWaypoint.lng
            
            val newLat = currentWaypoint.lat + (latDiff * state.progress)
            val newLng = currentWaypoint.lng + (lngDiff * state.progress)
            
            // Bearing
            val angleRad = atan2(lngDiff, latDiff)
            var bearing = Math.toDegrees(angleRad)
            if (bearing < 0) bearing += 360.0
            
            state.currentLat = newLat
            state.currentLng = newLng
            state.bearing = bearing
            
            updates.add(mapOf(
                "id" to state.unitId,
                "lat" to newLat,
                "lng" to newLng,
                "bearing" to bearing
            ))
        }
        
        template.convertAndSend("/topic/vehicles", updates)
    }
    
    private fun calculateDistanceMeters(p1: Point, p2: Point): Double {
        val lat1Rad = Math.toRadians(p1.lat)
        val lat2Rad = Math.toRadians(p2.lat)
        val deltaLat = Math.toRadians(p2.lat - p1.lat)
        val deltaLng = Math.toRadians(p2.lng - p1.lng)
        
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)
                
        val c = 2 * atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return R_EARTH * c
    }

    fun refreshUnits() {
        init()
    }
}
