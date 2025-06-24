package com.nextspringkart.gatewayservice.config

import com.nextspringkart.gatewayservice.filter.AuthenticationFilter
import com.nextspringkart.gatewayservice.filter.LoggingFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig(
    private val authenticationFilter: AuthenticationFilter,
    private val loggingFilter: LoggingFilter,

    @Value("\${auth.secret}")
    private val gatewaySecret: String,
) {

    @Value("\${services.user-service.url}")
    private lateinit var userServiceUrl: String

    @Value("\${services.product-service.url:http://localhost:8082}")
    private lateinit var productServiceUrl: String

    @Value("\${services.order-service.url:http://localhost:8083}")
    private lateinit var orderServiceUrl: String

    @Value("\${services.payment-service.url:http://localhost:8084}")
    private lateinit var paymentServiceUrl: String

    @Value("\${services.inventory-service.url:http://localhost:8085}")
    private lateinit var inventoryServiceUrl: String

    @Value("\${services.notification-service.url:http://localhost:8086}")
    private lateinit var notificationServiceUrl: String


    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // User Service Routes - Public endpoints don't need auth filter
            .route("public-routes") { r ->
                r.path(
                    "/api/*/health",
                    "/api/users/register",
                    "/api/users/login"
                )
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(userServiceUrl)
            }
            // User Service Routes - Protected endpoints need auth filter
            .route("user-service") { r ->
                r.path("/api/users/**")
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(authenticationFilter.apply(AuthenticationFilter.Config()))
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(userServiceUrl)
            }

            // Product Service Routes
            .route("product-service") { r ->
                r.path("/api/products/**", "/api/categories/**")
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(productServiceUrl)
            }

            // Order Service Routes
            .route("order-service") { r ->
                r.path("/api/orders/**", "/api/cart/**")
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(authenticationFilter.apply(AuthenticationFilter.Config()))
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(orderServiceUrl)
            }

            // Payment Service Routes
            .route("payment-service") { r ->
                r.path("/api/payments/**")
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(authenticationFilter.apply(AuthenticationFilter.Config()))
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(paymentServiceUrl)
            }

            // Inventory Service Routes
            .route("inventory-service") { r ->
                r.path("/api/inventory/**")
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(inventoryServiceUrl)
            }

            // Notification Service Routes
            .route("notification-service") { r ->
                r.path("/api/notifications/**")
                    .filters { f ->
                        f.addRequestHeader("X-Gateway-Auth", gatewaySecret)
                        f.filter(authenticationFilter.apply(AuthenticationFilter.Config()))
                        f.filter(loggingFilter.apply(LoggingFilter.Config()))
                        f.stripPrefix(0)
                    }
                    .uri(notificationServiceUrl)
            }
            .build()
    }
}