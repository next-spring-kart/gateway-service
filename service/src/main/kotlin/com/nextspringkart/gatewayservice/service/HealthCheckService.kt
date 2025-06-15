package com.nextspringkart.gatewayservice.service

import org.springframework.stereotype.Service

@Service
class HealthCheckService {
    fun getApplicationHealth(): String {
        return "Gateway service is running ok"
    }
}
