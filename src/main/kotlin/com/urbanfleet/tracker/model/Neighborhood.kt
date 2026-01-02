package com.urbanfleet.tracker.model

import jakarta.persistence.*

@Entity
@Table(name = "neighborhoods")
data class Neighborhood(
    @Id
    @Column(length = 100)
    val id: String, // Nome do bairro (ex: "Centro", "Batel")
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val geometry: String, // GeoJSON polygon coordinates
    
    @Column(nullable = false)
    val regionName: String = "" // Nome da regional (ex: "Matriz", "Portão")
)
