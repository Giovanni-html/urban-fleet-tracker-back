package com.urbanfleet.tracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class UrbanFleetTrackerApplication

fun main(args: Array<String>) {
    runApplication<UrbanFleetTrackerApplication>(*args)
}
