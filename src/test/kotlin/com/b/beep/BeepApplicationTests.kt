package com.b.beep

import com.b.beep.global.config.FirebaseConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:beep-context;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "discord.webhook-url=http://localhost/test-webhook",
    ]
)
@ActiveProfiles("test")
class BeepApplicationTests {

    @MockitoBean
    private lateinit var s3Client: S3Client

    @MockitoBean
    private lateinit var s3Presigner: S3Presigner

    @MockitoBean
    private lateinit var firebaseConfig: FirebaseConfig

    @Test
    fun contextLoads() {
    }
}
