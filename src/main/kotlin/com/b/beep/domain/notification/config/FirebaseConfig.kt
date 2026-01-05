package com.b.beep.domain.notification.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

@Configuration
class FirebaseConfig(
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucketName: String,
) {
    @PostConstruct
    fun init() {
        val serviceAccount = s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key("firebase/service-account-key.json")
                .build()
        )

        val options = FirebaseOptions
            .builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        FirebaseApp.initializeApp(options)
    }
}
