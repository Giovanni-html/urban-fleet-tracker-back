package com.urbanfleet.tracker.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Enable a simple memory-based message broker to carry the greeting messages back to the client on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic")
        // Designate the "/app" prefix for messages that are bound for methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // Register the "/ws" endpoint, enabling the SockJS protocol options so that we can use it on the client side
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // Allow all origins for development
            .withSockJS()
    }
}
