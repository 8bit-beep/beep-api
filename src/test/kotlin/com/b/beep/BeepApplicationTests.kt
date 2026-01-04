package com.b.beep

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@SpringBootTest
@Testcontainers
class BeepApplicationTests {

    companion object {
        @Container
        @ServiceConnection
        val mysqlContainer = MySQLContainer("mysql:8.0")
            .withDatabaseName("beep_test")
            .withUsername("test")
            .withPassword("test")

        @Container
        @ServiceConnection
        val rabbitmqContainer = RabbitMQContainer("rabbitmq:3.12-management")

        @Container
        val redisContainer = GenericContainer("redis:7-alpine")
            .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
            registry.add("spring.data.redis.password") { "" }
        }
    }

    // S3는 Mock으로 처리 (실제 서비스 불필요)
    @MockitoBean
    private lateinit var s3Client: S3Client

    @MockitoBean
    private lateinit var s3Presigner: S3Presigner

    @Test
    fun contextLoads() {
        // Spring Context가 정상적으로 로드되는지 확인
    }

}
